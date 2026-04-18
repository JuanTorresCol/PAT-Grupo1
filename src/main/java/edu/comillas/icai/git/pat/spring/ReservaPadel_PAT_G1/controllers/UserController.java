package edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.controllers;

import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain.Rol;
import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain.User;
import edu.comillas.icai.git.pat.spring.ReservaPadel_PAT_G1.domain.UserPatchRequest;
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

    @GetMapping
    public List<User> getAllUsers(@RequestHeader("Authorization") String authHeader) {
        User user = userService.getUserFromHeader(authHeader);
        userService.esAdmin(user);
        return userService.getAllUsers();
    }

    // GET /pistaPadel/users/{userId}

    @GetMapping("/{userId}")
    public User getUserById(@PathVariable Long userId, @RequestHeader("Authorization") String authHeader) {
        User user = userService.getUserFromHeader(authHeader);
        userService.esAdmin(user);

        try {
            return userService.getUserById(userId);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    //PATCH /pistaPadel/users/{userId}

    @PatchMapping("/{userId}")
    public User updateUser(@PathVariable Long userId,
                           @RequestBody UserPatchRequest req, @RequestHeader("Authorization") String authHeader) {

        User user = userService.getUserFromHeader(authHeader);
        userService.esAdmin(user);

        try {
            return userService.updateUser(userId, req);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
            //409 conflicto, email duplicado
        }
    }

    //DELETE /pistaPadel/users/{userId}

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId, @RequestHeader("Authorization") String authHeader) {

        User user = userService.getUserFromHeader(authHeader);
        userService.esAdmin(user);

        try {
            userService.deleteUser(userId);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
            //404 no encontrado
        }
    }


}
