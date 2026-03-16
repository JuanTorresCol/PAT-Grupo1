package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.repositories;

import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain.Pista;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.ArrayList;

public interface PistaRepository extends CrudRepository<Pista, Long> {
    Pista findByIdPista(Long idPista);
    @Query(value = "SELECT * FROM Pista", nativeQuery = true)
    ArrayList<Pista> selectAll();
    Boolean existsByNombre(String nombre);
    Pista findByNombre(String nombre);
}
