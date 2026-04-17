package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.services;

import com.fasterxml.classmate.AnnotationOverrides;
import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain.*;
import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    AnnotationOverrides Jwts;


    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void esAdmin(User user){
        if (!user.getRol().equals(Rol.ADMIN)){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autorizado");
        }


    }

    //obtener todos los usuarios (solo ADMIN puede hacerlo)
    public List<User> getAllUsers() {
        logger.info("Obteniendo listado de todos los usuarios");
        Iterable<User> todas = userRepository.findAll(); //CRUD devuelve iterable no List

        List<User> resultado = new ArrayList<>();
        todas.forEach(resultado::add);

        return resultado;
    }

    //obtener usuario por ID
    public User getUserById(Long id) {
        logger.info("Buscando usuario con ID: {}", id);

        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    //crear usuario
    public User registrar(UserCreateRequest user) {

        logger.info("Intentando crear usuario con email: {}", user.getEmail());

        if (userRepository.findByEmail(user.getEmail())!=null) {
            logger.error("El email ya existe: {}", user.getEmail());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese email");


        }


        User usuarioNuevo = new User();
        usuarioNuevo.setNombre(user.getNombre());
        usuarioNuevo.setApellidos(user.getApellidos());
        usuarioNuevo.setEmail(user.getEmail());
        usuarioNuevo.setPassword(user.getPassword());
        usuarioNuevo.setTelefono(user.getTelefono());
        usuarioNuevo.setActivo(true);
        usuarioNuevo.setRol(Rol.USER);
        usuarioNuevo.setFechaRegistro(LocalDate.now());
        return userRepository.save(usuarioNuevo);
    }


    public User autentica(String token){


        if (token == null || token.isEmpty() || userRepository.findByEmail(token)==null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Token no enviado");
        }



        return userRepository.findByEmail(token);


    }

    //actualizar usuario
    public User updateUser(Long id, UserPatchRequest updatedUser) {

        User existingUser = getUserById(id);

        if (!existingUser.getEmail().equals(updatedUser.email()) &&
                userRepository.existsByEmail(updatedUser.email())) {

            logger.error("Intento de actualización con email duplicado: {}", updatedUser.email());
            throw new RuntimeException("El email ya está en uso");

            //si el email del usuario actual no es igual al que se quiere cambiar
            //y ya existe en la base de datos debe dar error -> email único!
        }

        existingUser.setNombre(updatedUser.nombre());
        existingUser.setApellidos(updatedUser.apellidos());
        existingUser.setEmail(updatedUser.email());
        existingUser.setTelefono(updatedUser.telefono());

        logger.info("Usuario actualizado con ID: {}", id);

        return userRepository.save(existingUser);
    }

    // eliminar usuario
    public void deleteUser(Long id) {

        User user = getUserById(id);

        userRepository.delete(user);

        logger.info("Usuario eliminado con ID: {}", id);
    }


    public String login (String username, String password) {
        User user = userRepository.findByEmail(username);
        if (user == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        //verifico password
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Password incorrecta");
        }

        /*//genero token
        String info = user.getEmail() + "|" + user.getRol();
        String token = java.util.Base64.getEncoder().encodeToString(info.getBytes());

        return new LoginResponse(token, user.getIdUsuario(), user.getNombre(), user.getRol());

         */

        String token =  user.getEmail();

        return token;
    }


    public void logout(String token){
        token = null;
    }



}
