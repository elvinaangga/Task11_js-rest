package com.task9_springsecurity.service;


import com.task9_springsecurity.model.Role;
import com.task9_springsecurity.repository.RoleRepository;
import com.task9_springsecurity.repository.UserRepository;
import com.task9_springsecurity.model.User;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final Validator validator;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, Validator validator, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.validator = validator;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional
    public User save(User user) {
        validateUser(user);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // ambil roles dari DB
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            Set<Role> rolesFromDb = user.getRoles().stream()
                    .map(r -> roleRepository.findById(r.getId())
                            .orElseThrow(() -> new RuntimeException("Role not found: " + r.getId())))
                    .collect(Collectors.toSet());
            user.setRoles(rolesFromDb);
        }

        User saved = userRepository.save(user);
        saved.getRoles().size(); // pastikan roles dimuat
        return saved;
    }



    @Override
    @Transactional
    public void update(User user) {
        validateUser(user);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user); // JPA save() bisa handle update juga
    }


    @Override
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    private void validateUser(User user) {
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        if (!violations.isEmpty()) {
            String messages = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            throw new RuntimeException("Validation failed: " + messages);
        }
    }
}
