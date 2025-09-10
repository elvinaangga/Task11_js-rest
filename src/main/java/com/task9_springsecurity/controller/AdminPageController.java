package com.task9_springsecurity.controller;

import com.task9_springsecurity.model.Role;
import com.task9_springsecurity.model.User;
import com.task9_springsecurity.repository.RoleRepository;
import com.task9_springsecurity.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminPageController {

    private final UserService userService;
    private final RoleRepository roleRepository;

    @Autowired
    public AdminPageController(UserService userService, RoleRepository roleRepository) {
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
                .map(Role::getName)
                .collect(Collectors.joining(",")));

        if (isAdmin) {
            List<User> users = userService.findAll();
            List<Role> allRoles = roleRepository.findAll();

            model.addAttribute("users", users);
            model.addAttribute("pageTitle", "Admin Panel");
            model.addAttribute("tableTitle", "All Users");
            model.addAttribute("user", new User());
            model.addAttribute("allRoles", allRoles);
        } else {
            model.addAttribute("users", List.of(loggedUser));
        }

        return "users/list";
    }
}

