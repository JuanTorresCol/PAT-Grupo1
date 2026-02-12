package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ControladorPistas {

    private final Map<String, Pista> pistas = new ConcurrentHashMap<>();

    @PostMapping("/pistaPadel/courts")
    @ResponseStatus(HttpStatus.CREATED)
    public Pista creaPista(@RequestBody Pista pistaNueva){
        // compruebo que no existan pistas con ese idPista
        if (pistas.get(pistaNueva.idPista())!= null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        pistas.put(pistaNueva.idPista(),pistaNueva);
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
    @GetMapping("/pistaPadel/courts/{courtId}/availability")
}
