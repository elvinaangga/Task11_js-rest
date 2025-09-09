package com.task9_springsecurity.controller;

import com.task9_springsecurity.model.Role;
import com.task9_springsecurity.repository.RoleRepository;
import com.task9_springsecurity.service.UserService;
import com.task9_springsecurity.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final RoleRepository roleRepository;

    @Autowired
    public UserController(UserService userService, RoleRepository roleRepository) {
        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    @GetMapping
    public String userHome(Model model, Authentication auth) {
        User loggedUser = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        model.addAttribute("pageTitle", "User Information Page");
        model.addAttribute("users", List.of(loggedUser));

        // Selalu kirim loginRoles dan loginUserId agar JS bisa akses
        List<String> roles = loggedUser.getRoles().stream()
                .map(Role::getName)
                .toList(); // List<String>
        model.addAttribute("loginRoles", String.join(",", roles));

        model.addAttribute("loginUserId", loggedUser.getId());

        // Hanya untuk modal Add User jika admin
        if (roles.contains("ROLE_ADMIN")) {
            model.addAttribute("user", new User());
            model.addAttribute("allRoles", roleRepository.findAll());
        }

        return "users/list";
    }

    @GetMapping("/api/me")
    @ResponseBody
    public User getMe(Authentication auth) {
        return userService.findByEmail(auth.getName()).orElseThrow();
    }

}




