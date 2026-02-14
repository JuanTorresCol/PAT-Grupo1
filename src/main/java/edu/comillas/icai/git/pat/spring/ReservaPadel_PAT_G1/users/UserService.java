package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.users;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    //obtener todos los usuarios (solo ADMIN puede hacerlo)
    public List<User> getAllUsers() {
        logger.info("Obteniendo listado de todos los usuarios");
        return userRepository.findAll();
    }

    //obtener usuario por ID
    public User getUserById(Long id) {
        logger.info("Buscando usuario con ID: {}", id);

        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    //crear usuario
    public User createUser(User user) {

        logger.info("Intentando crear usuario con email: {}", user.getEmail());

        if (userRepository.existsByEmail(user.getEmail())) {
            logger.error("El email ya existe: {}", user.getEmail());
            throw new RuntimeException("El email ya está registrado");
            //siempre que se de logger.error hay que hacer exception

        }

        return userRepository.save(user);
    }

    //actualizar usuario
    public User updateUser(Long id, User updatedUser) {

        User existingUser = getUserById(id);

        if (!existingUser.getEmail().equals(updatedUser.getEmail()) &&
                userRepository.existsByEmail(updatedUser.getEmail())) {

            logger.error("Intento de actualización con email duplicado: {}", updatedUser.getEmail());
            throw new RuntimeException("El email ya está en uso");

            //si el email del usuario actual no es igual al que se quiere cambiar
            //y ya existe en la base de datos debe dar error -> email único!
        }

        existingUser.setNombre(updatedUser.getNombre());
        existingUser.setApellidos(updatedUser.getApellidos());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setTelefono(updatedUser.getTelefono());

        logger.info("Usuario actualizado con ID: {}", id);

        return userRepository.save(existingUser);
    }

    // eliminar usuario
    public void deleteUser(Long id) {

        User user = getUserById(id);

        userRepository.delete(user);

        logger.info("Usuario eliminado con ID: {}", id);
    }
}
