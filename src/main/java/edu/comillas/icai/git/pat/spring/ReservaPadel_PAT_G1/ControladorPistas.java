package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class ControladorPistas {

    private final Map<String, Pista> pistas = new ConcurrentHashMap<>();
    private final Map<String, Map<LocalDate, ArrayList<Boolean>>> disponibilidades = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(ControladorPistas.class);

    @PostMapping("/pistaPadel/courts")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public Pista creaPista(@Valid @RequestBody Pista pistaNueva,
                           BindingResult bindingResult) {

        log.info("Solicitud creación pista id={}", pistaNueva.idPista());
        log.debug("Datos recibidos para creación: {}", pistaNueva);

        if (pistas.containsKey(pistaNueva.idPista())) {
            log.warn("Intento de crear pista ya existente id={}", pistaNueva.idPista());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una pista con ese ID");
        }

        if (bindingResult.hasErrors()) {
            log.warn("Errores de validación al crear pista id={}", pistaNueva.idPista());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datos inválidos");
        }

        pistas.put(pistaNueva.idPista(), pistaNueva);
        disponibilidades.put(pistaNueva.idPista(), new ConcurrentHashMap<>());

        log.info("Pista creada correctamente id={}", pistaNueva.idPista());
        return pistaNueva;
    }

    @GetMapping("/pistaPadel/courts")
    public Map<String, Pista> listarPistas(
            @RequestParam(required = false) Boolean filtro) {

        log.info("Solicitud listado pistas");
        log.debug("Filtro aplicado: {}", filtro);

        if (pistas.isEmpty()) {
            log.warn("Listado solicitado pero no existen pistas");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existen pistas");
        }

        if (filtro != null) {
            Map<String, Pista> pistasFiltro = new ConcurrentHashMap<>();

            for (Pista pista : pistas.values()) {
                if (pista.activa().equals(filtro)) {
                    pistasFiltro.put(pista.idPista(), pista);
                }
            }

            log.debug("Número de pistas filtradas: {}", pistasFiltro.size());
            return pistasFiltro;
        }

        log.debug("Número total de pistas: {}", pistas.size());
        return pistas;
    }

    @GetMapping("/pistaPadel/courts/{courtId}")
    public Pista verDetalle(@PathVariable String courtId) {

        log.info("Solicitud detalle pista id={}", courtId);

        Pista pista = pistas.get(courtId);

        if (pista == null) {
            log.warn("Detalle solicitado para pista inexistente id={}", courtId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existen pistas con ese ID");
        }

        log.debug("Detalle pista encontrado: {}", pista);
        return pista;
    }

    @PatchMapping("/pistaPadel/courts/{courtId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Pista actualizar(@PathVariable String courtId,
                            @RequestBody CourtUpdate newCourt) {

        log.info("Solicitud actualización pista id={}", courtId);
        log.debug("Datos actualización recibidos: {}", newCourt);

        Pista antigua = pistas.get(courtId);

        if (antigua == null) {
            log.warn("Intento de actualizar pista inexistente id={}", courtId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe ninguna pista con ese ID");
        }

        if (newCourt.isEmpty()) {
            log.warn("Actualización sin cambios para pista id={}", courtId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se han introducido cambios");
        }

        Pista nueva = new Pista(
                antigua.idPista(),
                newCourt.nombre() != null ? newCourt.nombre() : antigua.nombre(),
                newCourt.ubicacion() != null ? newCourt.ubicacion() : antigua.ubicacion(),
                newCourt.precio() != null ? newCourt.precio() : antigua.precioHora(),
                newCourt.activa() != null ? newCourt.activa() : antigua.activa(),
                antigua.fechaAlta()
        );

        pistas.put(courtId, nueva);

        log.info("Pista actualizada correctamente id={}", courtId);
        log.debug("Nueva información pista: {}", nueva);

        return nueva;
    }

    @DeleteMapping("/pistaPadel/courts/{courtId}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable String courtId) {

        log.info("Solicitud eliminación pista id={}", courtId);

        if (!pistas.containsKey(courtId)) {
            log.warn("Intento de eliminar pista inexistente id={}", courtId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existen pistas con ese ID");
        }

        pistas.remove(courtId);
        disponibilidades.remove(courtId);

        log.info("Pista eliminada correctamente id={}", courtId);
    }

    @GetMapping("/pistaPadel/availability")
    public Map<String, ArrayList<Boolean>> checkAvailability(@RequestParam String date, @RequestParam(required = false) String courtId) {

        log.info("Solicitud disponibilidad fecha={} courtId={}", date, courtId);

        LocalDate fecha = LocalDate.parse(date);

        if (fecha.isBefore(LocalDate.now())) {
            log.warn("Consulta disponibilidad en fecha pasada fecha={}", fecha);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes buscar reservas de días pasados");
        }

        Map<String, ArrayList<Boolean>> respuesta = new ConcurrentHashMap<>();

        if (courtId != null) {
            if (!pistas.containsKey(courtId)) {
                log.warn("Consulta disponibilidad para pista inexistente id={}", courtId);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existen pistas con ese ID");
            }
            respuesta.put(courtId, obtenerDisponibilidadDia(courtId, fecha));
        } else {
            for (String id : pistas.keySet()) {
                respuesta.put(id, obtenerDisponibilidadDia(id, fecha));
            }
        }

        log.debug("Disponibilidad calculada para fecha={} totalPistas={}", fecha, respuesta.size());
        return respuesta;
    }

    @GetMapping("/pistaPadel/courts/{courtId}/availability")
    public ArrayList<Boolean> checkDispPista(@RequestParam String date, @PathVariable String courtId) {

        log.info("Solicitud disponibilidad pista id={} fecha={}", courtId, date);

        if (!pistas.containsKey(courtId)) {
            log.warn("Consulta disponibilidad para pista inexistente id={}", courtId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existen pistas con ese ID");
        }

        LocalDate fecha = LocalDate.parse(date);

        if (fecha.isBefore(LocalDate.now())) {
            log.warn("Consulta disponibilidad en fecha pasada fecha={}", fecha);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes buscar reservas de días pasados");
        }

        ArrayList<Boolean> disponibilidad = obtenerDisponibilidadDia(courtId, fecha);

        log.debug("Disponibilidad pista id={} fecha={} obtenida", courtId, fecha);
        return disponibilidad;
    }

    private ArrayList<Boolean> obtenerDisponibilidadDia(String courtId, LocalDate fecha) {

        log.debug("Obteniendo disponibilidad para pista id={} fecha={}", courtId, fecha);

        Map<LocalDate, ArrayList<Boolean>> mapaDias = disponibilidades.get(courtId);
        mapaDias.putIfAbsent(fecha, new ArrayList<>(Collections.nCopies(28, false)));

        log.debug("Disponibilidad preparada para pista id={} fecha={}", courtId, fecha);
        return mapaDias.get(fecha);
    }
}
