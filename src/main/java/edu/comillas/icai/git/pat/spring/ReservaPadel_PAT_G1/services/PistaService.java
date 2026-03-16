package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.services;

import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.controllers.ControladorPistas;
import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain.CourtUpdate;
import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain.Pista;
import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.repositories.PistaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PistaService {

    @Autowired
    PistaRepository pistaRepository;

    public final Map<String, Map<LocalDate, ArrayList<Boolean>>> disponibilidades = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(ControladorPistas.class);

    public Pista creaPista(Pista pista, BindingResult  bindingResult) {
        log.info("Solicitud creación pista id={}", pista.getIdPista());
        log.debug("Datos pista recibida: nombre={}, ubicacion={}, precio={}", pista.getNombre(), pista.getUbicacion(), pista.getPrecioHora());

        if (pistaRepository.findByIdPista(pista.getIdPista())!=null) {
            log.warn("Intento de crear pista con id existente id={}", pista.getIdPista());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una pista con ese ID");
        }

        if (bindingResult.hasErrors()) {
            log.warn("Errores de validación al crear pista id={}", pista.getIdPista());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datos inválidos");
        }

        pistaRepository.save(pista);
        disponibilidades.put(pista.getNombre(), new ConcurrentHashMap<>());

        log.info("Pista creada correctamente id={}", pista.getIdPista());

        return pista;
    }

    public ArrayList<Pista> listarPistas(Boolean filtro){
        log.info("Solicitud listado de pistas");
        log.debug("Filtro recibido={}", filtro);

        if (pistaRepository.selectAll() != null) {
            log.warn("No existen pistas en el repositorio");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existen pistas");
        }

        ArrayList<Pista> pistas = pistaRepository.selectAll();
        log.debug("Número de pistas recuperadas={}", pistas.size());

        if (filtro != null) {
            ArrayList<Pista> pistasFiltro = new ArrayList<>();
            for (Pista pista : pistas) {
                if (pista.getActiva().equals(filtro)) {
                    pistasFiltro.add(pista);
                }
            }
            log.debug("Número de pistas tras aplicar filtro={}", pistasFiltro.size());
            return pistasFiltro;
        }
        return pistas;
    }

    public Pista verDetalle(String nombre){
        log.info("Solicitud detalle pista nombre={}", nombre);

        if(pistaRepository.existsByNombre(nombre)){
            log.debug("Pista encontrada nombre={}", nombre);
            return pistaRepository.findByNombre(nombre);
        }else{
            log.warn("Pista no encontrada nombre={}", nombre);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no encontrada");
        }
    }

    public Pista actuPista(String nombre, CourtUpdate newCourt){
        log.info("Solicitud actualización pista nombre={}", nombre);
        log.debug("Datos actualización recibidos={}", newCourt);

        Pista antigua = pistaRepository.findByNombre(nombre);

        if (antigua == null) {
            log.warn("Intento de actualizar pista inexistente nombre={}", nombre);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe ninguna pista con ese ID");
        }
        if (newCourt.isEmpty()) {
            log.warn("Actualización sin cambios para pista nombre={}", nombre);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se han introducido cambios");
        }

        Pista nueva = new Pista(
                antigua.getIdPista(),
                newCourt.nombre() != null ? newCourt.nombre() : antigua.getNombre(),
                newCourt.ubicacion() != null ? newCourt.ubicacion() : antigua.getUbicacion(),
                newCourt.precio() != null ? newCourt.precio() : antigua.getPrecioHora(),
                newCourt.activa() != null ? newCourt.activa() : antigua.getActiva(),
                antigua.getFechaAlta()
        );

        pistaRepository.save(nueva);
        log.info("Pista actualizada correctamente id={}", nueva.getIdPista());
        log.debug("Nueva configuración pista nombre={}, ubicacion={}, precio={}, activa={}", nueva.getNombre(), nueva.getUbicacion(), nueva.getPrecioHora(), nueva.getActiva());

        return nueva;
    }

    public void deletePista(String nombre){
        log.info("Solicitud eliminación pista nombre={}", nombre);

        if (!pistaRepository.existsByNombre(nombre)) {
            log.warn("Intento de eliminar pista inexistente nombre={}", nombre);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existen pistas con ese nombre");
        }

        pistaRepository.delete(pistaRepository.findByNombre(nombre));
        disponibilidades.remove(pistaRepository.findByNombre(nombre).getNombre());

        log.info("Pista eliminada correctamente nombre={}", nombre);
    }

    public Map<Long, ArrayList<Boolean>> checkAvailability(String date, String nombre){

        log.info("Solicitud consulta disponibilidad fecha={} pista={}", date, nombre);

        LocalDate fecha = LocalDate.parse(date);

        if (fecha.isBefore(LocalDate.now())) {
            log.warn("Consulta de disponibilidad para fecha pasada fecha={}", fecha);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes buscar reservas de días pasados");
        }

        Map<Long, ArrayList<Boolean>> respuesta = new ConcurrentHashMap<>();

        if (nombre != null) {
            if (!pistaRepository.existsByNombre(nombre)) {
                log.warn("Consulta disponibilidad pista inexistente nombre={}", nombre);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existen pistas con ese ID");
            }

            Pista pistaSelec = pistaRepository.findByNombre(nombre);
            log.debug("Consultando disponibilidad pista id={} fecha={}", pistaSelec.getIdPista(), fecha);

            respuesta.put(pistaSelec.getIdPista(), obtenerDisponibilidadDia(pistaSelec.getNombre(), fecha));

        } else {
            log.debug("Consulta disponibilidad para todas las pistas fecha={}", fecha);
            for (Pista pi : pistaRepository.selectAll()) {
                respuesta.put(pi.getIdPista(), obtenerDisponibilidadDia(pi.getNombre(), fecha));
            }
        }

        return respuesta;
    }

    public ArrayList<Boolean> checkDispPista(String date, String nombre){
        log.info("Solicitud disponibilidad pista concreta nombre={} fecha={}", nombre, date);

        if (!pistaRepository.existsByNombre(nombre)) {
            log.warn("Consulta disponibilidad pista inexistente nombre={}", nombre);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existen pistas con ese ID");
        }

        LocalDate fecha = LocalDate.parse(date);

        if (fecha.isBefore(LocalDate.now())) {
            log.warn("Consulta de disponibilidad para fecha pasada fecha={}", fecha);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes buscar reservas de días pasados");
        }

        log.debug("Obteniendo disponibilidad pista nombre={} fecha={}", nombre, fecha);
        return obtenerDisponibilidadDia(pistaRepository.findByNombre(nombre).getNombre(), fecha);
    }

    public ArrayList<Boolean> obtenerDisponibilidadDia(String courtId, LocalDate fecha) {
        log.debug("Acceso disponibilidad interna pista={} fecha={}", courtId, fecha);

        Map<LocalDate, ArrayList<Boolean>> mapaDias = disponibilidades.get(courtId);
        mapaDias.putIfAbsent(fecha, new ArrayList<>(Collections.nCopies(28, false)));

        log.debug("Disponibilidad generada/recuperada para pista={} fecha={}", courtId, fecha);

        return mapaDias.get(fecha);
    }

}