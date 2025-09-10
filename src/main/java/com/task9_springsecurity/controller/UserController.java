package com.task9_springsecurity.controller;

import com.task9_springsecurity.repository.RoleRepository;
import com.task9_springsecurity.service.UserService;
import com.task9_springsecurity.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/api")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("/me")
    public User getMe(Authentication auth) {
        return userService.findByEmail(auth.getName()).orElseThrow();
    }

}




