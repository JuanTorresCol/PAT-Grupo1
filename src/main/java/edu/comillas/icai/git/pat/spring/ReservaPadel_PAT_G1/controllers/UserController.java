package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.controllers;

import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain.User;
import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/pistaPadel/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    //GET /pistaPadel/users
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    // GET /pistaPadel/users/{userId}
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{userId}")
    public User getUserById(@PathVariable Long userId) {
        try {
            return userService.getUserById(userId);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    //POST /pistaPadel/users
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@RequestBody User user) {
        try {
            return userService.createUser(user);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
            //409 conflicto, email duplicado
        }
    }


    //PATCH /pistaPadel/users/{userId}
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{userId}")
    public User updateUser(@PathVariable Long userId,
                           @RequestBody User user) {
        try {
            return userService.updateUser(userId, user);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
            //409 conflicto, email duplicado
        }
    }

    //DELETE /pistaPadel/users/{userId}
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        try {
            userService.deleteUser(userId);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
            //404 no encontrado
        }
    }
}
