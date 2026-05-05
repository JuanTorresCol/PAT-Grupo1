package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.services;


import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.controllers.ControladorPistas;
import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain.CourtUpdate;
import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain.Pista;
import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain.Reserva;
import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain.ReservaStatus;
import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.repositories.PistaRepository;
import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.repositories.ReservaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

//clase compartida entre pistas y reservas para el control gestion de reservas
@Service
public class PistaService {

    @Autowired
    PistaRepository pistaRepository;

    @Autowired
    ReservaRepository reservaRepository;

    public final Map<Long, Map<LocalDate, ArrayList<Boolean>>> disponibilidades = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(ControladorPistas.class);


    public Pista creaPista(Pista pista, BindingResult  bindingResult) {
        log.info("Solicitud creación pista id={}", pista.getIdPista());

        if (pistaRepository.findByIdPista(pista.getIdPista())!=null||pistaRepository.existsByNombre(pista.getNombre())==true) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una pista con ese ID");
        }

        if (bindingResult.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datos inválidos");
        }

        pistaRepository.save(pista);
        disponibilidades.put(pista.getIdPista(), new ConcurrentHashMap<>());

        log.info("Pista creada correctamente id={}", pista.getIdPista());

        return pista;
    }

    public ArrayList<Pista> listarPistas(Boolean filtro){
        log.info("Solicitud listado de pistas");
        if (pistaRepository.selectAll() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existen pistas");
        }
        ArrayList<Pista> pistas = pistaRepository.selectAll();
        if (filtro != null) {
            ArrayList<Pista> pistasFiltro = new ArrayList<>();
            for (Pista pista : pistas) {
                if (pista.getActiva().equals(filtro)) {
                    pistasFiltro.add(pista);
                }
            }
            return pistasFiltro;
        }
        return pistas;
    }

    public Pista verDetalle(String nombre){
        if(pistaRepository.existsByNombre(nombre)){
            return pistaRepository.findByNombre(nombre);
        }else{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pista no encontrada");
        }
    }

    public Pista actuPista(String nombre, CourtUpdate newCourt){
        Pista antigua = pistaRepository.findByNombre(nombre);

        if (antigua == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe ninguna pista con ese ID");
        }
        if (newCourt.isEmpty()) {
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
        return nueva;
    }

    public void deletePista(String nombre){
        if (!pistaRepository.existsByNombre(nombre)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existen pistas con ese nombre");
        }

        Pista pista = pistaRepository.findByNombre(nombre);
        pistaRepository.delete(pista);
        disponibilidades.remove(pista.getIdPista());
    }

    public Map<Long, ArrayList<Boolean>> checkAvailability(String date, String nombre){

        LocalDate fecha = LocalDate.parse(date);

        if (fecha.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes buscar reservas de días pasados");
        }

        Map<Long, ArrayList<Boolean>> respuesta = new ConcurrentHashMap<>();

        if (nombre != null) {
            if (!pistaRepository.existsByNombre(nombre)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existen pistas con ese ID");
            }

            Pista pistaSelec = pistaRepository.findByNombre(nombre);
            respuesta.put(pistaSelec.getIdPista(), checkDispPista(date, nombre));
        } else {
            for (Pista pi : pistaRepository.selectAll()) {
                respuesta.put(pi.getIdPista(), checkDispPista(date, pi.getNombre()));
            }
        }

        return respuesta;
    }

    //sirve para decirle al frontend qué slots pintar como ocupados
    public ArrayList<Boolean> checkDispPista(String date, String nombre){
        if (!pistaRepository.existsByNombre(nombre)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existen pistas con ese ID");
        }

        LocalDate fecha = LocalDate.parse(date);

        if (fecha.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes buscar reservas de días pasados");
        }

        Pista pista = pistaRepository.findByNombre(nombre);

        ArrayList<Boolean> slots = new ArrayList<>(Collections.nCopies(28, false));

        List<Reserva> reservas = reservaRepository.findByPistaIdPistaAndDate(pista.getIdPista(), fecha);

        for (Reserva r : reservas) {
            if (r.getEstado() == ReservaStatus.CANCELADA) {
                continue;
            }

            int slotStart = (int) Duration.between(ReservaService.APERTURA, r.getStartTime()).toMinutes() / 30;
            int slotEnd = (int) Duration.between(ReservaService.APERTURA, r.getEndTime()).toMinutes() / 30;

            for (int i = slotStart; i < slotEnd; i++) {
                if (i >= 0 && i < slots.size()) {
                    slots.set(i, true);
                }
            }
        }

        return slots;
    }

    public ArrayList<Boolean> obtenerDisponibilidadDia(Long courtId, LocalDate fecha) {
        disponibilidades.putIfAbsent(courtId, new HashMap<>());
        Map<LocalDate, ArrayList<Boolean>> mapaDias = disponibilidades.get(courtId);
        mapaDias.putIfAbsent(fecha, new ArrayList<>(Collections.nCopies(28, false)));
        return mapaDias.get(fecha);
    }

}
