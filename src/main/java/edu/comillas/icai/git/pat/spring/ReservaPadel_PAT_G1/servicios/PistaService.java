package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.servicios;


import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain.Pista;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

//clase compartida entre pistas y reservas para el control gestion de reservas
@Service
public class PistaService {

    public final Map<String, Pista> pistas = new ConcurrentHashMap<>();
    public final Map<String, Map<LocalDate, ArrayList<Boolean>>> disponibilidades = new ConcurrentHashMap<>();

    public Pista getPista(String courtId) {
        return pistas.get(courtId);
    }

    public ArrayList<Boolean> obtenerDisponibilidadDia(String courtId, LocalDate fecha) {
        Map<LocalDate, ArrayList<Boolean>> mapaDias = disponibilidades.get(courtId);
        mapaDias.putIfAbsent(fecha, new ArrayList<>(Collections.nCopies(28, false)));
        return mapaDias.get(fecha);
    }



}
