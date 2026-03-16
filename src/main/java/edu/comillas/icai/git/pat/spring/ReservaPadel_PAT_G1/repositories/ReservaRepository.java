package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.repositories;

import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain.Reserva;

import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;


public interface ReservaRepository extends CrudRepository<Reserva, Long> {
    List<Reserva> findByUsernameEmail(String email);
    List<Reserva> findByPistaIdPistaAndDate(Long pistaId, LocalDate date);
}

