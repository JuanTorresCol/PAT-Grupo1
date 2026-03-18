package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.services;

import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain.LoginResponse;
import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain.User;
import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registrar(User user) {
        //encripto password
        String passwordEncriptada = passwordEncoder.encode(user.getPassword());
        user.setPassword(passwordEncriptada);

        //nos aseguramos de que hay fecha de registro
        if (user.getFechaRegistro() == null) {
            user.setFechaRegistro(java.time.LocalDateTime.now());
        }

        return userRepository.save(user);
    }

    public LoginResponse login (String email, String passwordSinValidar) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User no encontrado"));

        //verifico password
        if (!passwordEncoder.matches(passwordSinValidar, user.getPassword())) {
            throw new RuntimeException("Password incorrecta");
        }

        //genero token
        String info = user.getEmail() + "|" + user.getRol();
        String token = java.util.Base64.getEncoder().encodeToString(info.getBytes());

        return new LoginResponse(token, user.getIdUsuario(), user.getNombre(), user.getRol());
    }

}













