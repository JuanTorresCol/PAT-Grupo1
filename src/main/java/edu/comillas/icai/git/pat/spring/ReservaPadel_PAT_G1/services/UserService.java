package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.services;

import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain.*;
import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.repositories.UserRepository;

import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;

import java.security.Key;
import java.util.Date;

@Service
public class UserService {

    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final ArrayList<String> blacklist = new ArrayList<>();

    private static final Key key = Keys.hmacShaKeyFor("MariaElisaRosarioCarlotaJuan1blablablablablablabla".getBytes());

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String generarToken(User user) {

        long ahora = System.currentTimeMillis();


        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("role", user.getRol().name())
                .setIssuedAt(new Date(ahora))
                .setExpiration(new Date(ahora + 1000 * 60 * 60))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public User getUserFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token no enviado");
        }

        String token = authHeader.substring(7);
        return autentica(token);
    }

    public void esAdmin(User user) {
        if (user.getRol() != Rol.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
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

        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token no enviado");
        }

        token = token.trim();

        if (token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }

        if (token.contains(" ")) {
            System.out.println("TOKEN CON ESPACIOS: [" + token + "]");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Formato de token inválido");
        }

        if (blacklist.contains(token)){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión expirada");
        }

        try {
            Jws<Claims> jws = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            Claims claims = jws.getBody();

            String email = claims.getSubject();
            String roleToken = claims.get("role", String.class);

            User user = userRepository.findByEmail(email);

            if (user == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no válido");
            }

            if (!user.getRol().name().equals(roleToken)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Rol inválido");
            }

            return user;

        } catch (ResponseStatusException e) {
            throw e;
        } catch (ExpiredJwtException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token expirado");
        } catch (JwtException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido");
        }
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


    public String login(String username, String password) {
        User user = userRepository.findByEmail(username);

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }

        if (!user.getPassword().equals(password)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Password incorrecta");
        }

        return generarToken(user);
    }


    public void logout(String token){
        blacklist.add(token);
    }
}
