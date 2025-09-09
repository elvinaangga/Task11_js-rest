package com.task9_springsecurity.controller;

import com.task9_springsecurity.model.Role;
import com.task9_springsecurity.model.User;
import com.task9_springsecurity.repository.RoleRepository;
import com.task9_springsecurity.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final RoleRepository roleRepository;

    @Autowired
    public AdminController(UserService userService, RoleRepository roleRepository) {
        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    @GetMapping
    public String listUsers(Model model, Authentication authentication) {
        User loggedUser = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        boolean isAdmin = loggedUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_ADMIN"));

        // Kirim login user info ke model
        model.addAttribute("loginUserId", loggedUser.getId());
        model.addAttribute("loginRoles", loggedUser.getRoles().stream()
                .map(Role::getName)   // pastikan format "ROLE_ADMIN"
                .collect(Collectors.joining(","))); // jadi "ROLE_ADMIN,ROLE_USER"

        if (isAdmin) {
            List<User> users = userService.findAll();
            List<Role> allRoles = roleRepository.findAll();

            model.addAttribute("users", users);
            model.addAttribute("pageTitle", "Admin Panel");
            model.addAttribute("tableTitle", "All Users");
            model.addAttribute("user", new User());  // untuk modal create
            model.addAttribute("allRoles", allRoles);
        } else {
            model.addAttribute("users", List.of(loggedUser)); // hanya user sendiri
        }

        return "users/list";
    }



    @GetMapping("/api/all")
    @ResponseBody
    public List<User> getAllUsers(Authentication auth) {
        return userService.findAll();
    }

    @PostMapping("/api")
    @ResponseBody
    @Transactional
    public User createUser(@RequestBody User user) {
        User saved = userService.save(user);
        saved.getRoles().size();
        return saved;
    }

    @PutMapping("/api/{id}")
    @ResponseBody
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        User existing = userService.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        existing.setFirstName(user.getFirstName());
        existing.setLastName(user.getLastName());
        existing.setEmail(user.getEmail());
        existing.setRoles(user.getRoles() != null ? user.getRoles() : new HashSet<>());
        userService.update(existing);
        return existing;
    }

    @DeleteMapping("/api/{id}")
    @ResponseBody
    public void deleteUser(@PathVariable Long id) {
        userService.delete(id);
    }

    @GetMapping("/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", user);
        model.addAttribute("allRoles", roleRepository.findAll());
        return "users/edit_form";
    }

    @PostMapping("/update")
    public String updateUserForm(@ModelAttribute("user") User user) {
        User existing = userService.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        existing.setFirstName(user.getFirstName());
        existing.setLastName(user.getLastName());
        existing.setEmail(user.getEmail());
        existing.setPassword(user.getPassword()); // update password juga
        existing.setRoles(user.getRoles());
        userService.update(existing);
        return "redirect:/admin";
    }
}