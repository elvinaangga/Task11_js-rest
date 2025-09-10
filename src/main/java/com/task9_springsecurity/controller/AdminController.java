package com.task9_springsecurity.controller;

import com.task9_springsecurity.model.User;
import com.task9_springsecurity.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/api")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    // =======================
    // GET all users
    // =======================
    @GetMapping("/all")
    public List<User> getAllUsers() {
        return userService.findAll();
    }

    // =======================
    // GET single user by id
    // =======================
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // =======================
    // CREATE new user
    // =======================
    @PostMapping
    public User createUser(@RequestBody User newUser) {
        if (newUser.getPassword() == null || newUser.getPassword().isEmpty()) {
            throw new RuntimeException("Password cannot be empty");
        }

        // Optional: cek email unik
        if (userService.findByEmail(newUser.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        return userService.save(newUser);
    }

    // =======================
    // UPDATE user
    // =======================
    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        User existing = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        existing.setFirstName(updatedUser.getFirstName());
        existing.setLastName(updatedUser.getLastName());
        existing.setEmail(updatedUser.getEmail());
        existing.setPassword(updatedUser.getPassword());
        existing.setRoles(updatedUser.getRoles());

        userService.update(existing);
        return existing;
    }


    // =======================
    // DELETE user
    // =======================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.ok().build();
    }
}
