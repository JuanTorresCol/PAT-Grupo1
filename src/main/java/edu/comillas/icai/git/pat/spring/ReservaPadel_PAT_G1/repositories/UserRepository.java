package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.repositories;

import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {

    User findByEmail(String email);

    boolean existsByEmail(String email);
}