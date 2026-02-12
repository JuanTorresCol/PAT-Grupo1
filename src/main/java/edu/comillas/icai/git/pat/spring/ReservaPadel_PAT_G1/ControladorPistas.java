package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ControladorPistas {

    private final Map<String, Pista> pistas = new ConcurrentHashMap<>();
    private final Map<String, Disponibilidad> disponibilidades = new ConcurrentHashMap<>();

    @PostMapping("/pistaPadel/courts")
    @ResponseStatus(HttpStatus.CREATED)
    public Pista creaPista(@RequestBody Pista pistaNueva){
        // compruebo que no existan pistas con ese idPista
        if (pistas.get(pistaNueva.idPista())!= null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        pistas.put(pistaNueva.idPista(),pistaNueva);
        disponibilidades.put(pistaNueva.idPista(),new Disponibilidad(pistaNueva.idPista(), new Date(), null));
        return pistaNueva;
    }

    @GetMapping("/pistaPadel/courts")
    public Map<String,Pista> listarPistas(@RequestBody Boolean filtro){
        if (pistas.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        if(filtro!=null){
            Map<String, Pista> pistasFiltro = new ConcurrentHashMap<>();
            for(Pista pista : pistas.values()){
                if (pista.activa()==filtro){
                    pistasFiltro.put(pista.idPista(),pista);
                }
            }
            return pistasFiltro;
        }
        else{
            return pistas;
        }
    }

    @GetMapping("/pistaPadel/courts/{courtId}")
    public Pista verDetalle(@PathVariable String courtId){
        // comprobamos que el input es correcto
        if (courtId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return pistas.get(courtId);
    }

    @PatchMapping("/pistaPadel/courts/{courtId}")
    public Pista verDetalle(@PathVariable String courtId, @RequestBody CourtUpdate newCourt){
        //comprobamos si han habido cambios
        if (newCourt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        Pista antigua = pistas.get(courtId);

        String nuevoNombre = (newCourt.nombre() != null) ? newCourt.nombre() : antigua.nombre();
        String nuevaUbi = (newCourt.ubicacion() != null) ? newCourt.ubicacion() : antigua.ubicacion();
        Double nuevoPrecio = (newCourt.precio() != null) ? newCourt.precio() : antigua.precioHora();
        Boolean nuevaActiva = (newCourt.activa() != null) ? newCourt.activa() : antigua.activa();

        Pista nueva = new Pista(
                antigua.idPista(),
                nuevoNombre,
                nuevaUbi,
                nuevoPrecio,
                nuevaActiva,
                antigua.fechaAlta()
        );

        pistas.put(courtId, nueva);
        return nueva;
    }

    @DeleteMapping("/pistaPadel/courts/{courtId}")
    public void delete(@PathVariable String courtId) {
        pistas.remove(courtId);
        return;
    }

    @GetMapping("/pistaPadel/availability")
    public Map<String, ArrayList<Boolean>> checkAvailability(@RequestParam String date, @RequestParam String courtId){
        // compruebo que la hora de busqueda no es anterior a hoy
        LocalDate localDate = LocalDate.parse(date);
        Date fecha = Date.from(
                localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date hoy = new Date();
        if(fecha.before(hoy)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        ArrayList<String> pistasReserva = new ArrayList<>();
        if(courtId != null){
            if(pistas.get(courtId) == null){
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
            for(Pista pista : pistas.values()){
                if(pista.idPista().equals(courtId)){
                    pistasReserva.add(pista.idPista());
                }
            }
        }
        else{
            for(Pista pista : pistas.values()){
                pistasReserva.add(pista.idPista());
            }
        }

        // Compruebo que haya pistas seleccionadas
        if(pistasReserva.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        Map<String, ArrayList<Boolean>> respuesta = new ConcurrentHashMap<>();

        for(String pistaId : pistasReserva){
            if(disponibilidades.containsKey(pistaId)){
                respuesta.put(pistaId, disponibilidades.get(pistaId).fechasDisponibles());
            }
        }

        return respuesta;
    }

    @GetMapping("/pistaPadel/courts/{courtId}/availability")
    public ArrayList<Boolean> checkDispPista(@RequestParam String date, @RequestBody String courtId){

        // compruebo que la pista existe
        if(pistas.get(courtId) == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        // compruebo que la hora de busqueda no es anterior a hoy
        LocalDate localDate = LocalDate.parse(date);
        Date fecha = Date.from(
                localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date hoy = new Date();
        if(fecha.before(hoy)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        return disponibilidades.get(courtId).fechasDisponibles();
    }
}
