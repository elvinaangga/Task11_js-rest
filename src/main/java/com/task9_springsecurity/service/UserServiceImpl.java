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

import java.util.Collections;
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

        user.setRoles(fetchRolesFromDb(user.getRoles()));

        User saved = userRepository.save(user);
        saved.getRoles().size();
        return saved;
    }

    @Override
    @Transactional
    public User update(User user) {
        System.out.println("==== UPDATE USER ====");
        System.out.println("Payload diterima: " + user);

        validateUser(user); // optional, validasi email/role/etc

        // Ambil entity dari DB
        User existing = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        System.out.println("Password lama di DB: " + existing.getPassword());

        // Update nama & email
        existing.setFirstName(user.getFirstName());
        existing.setLastName(user.getLastName());
        existing.setEmail(user.getEmail());

        // Update roles kalau ada
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            existing.setRoles(fetchRolesFromDb(user.getRoles()));
        }

        // Update password hanya kalau ada isi non-whitespace
        if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
            // Hanya encode kalau password **baru**, bukan yang sudah terenkripsi
            if (!user.getPassword().startsWith("{bcrypt}")) {
                existing.setPassword(passwordEncoder.encode(user.getPassword()));
            } else {
                existing.setPassword(user.getPassword());
            }
        }


        userRepository.save(existing);
        System.out.println("Password setelah save: " + existing.getPassword());
        System.out.println("==== END UPDATE ====");
        return existing;
    }


    // ⬇️ Taruh helper method di sini
    private Set<Role> fetchRolesFromDb(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return Collections.emptySet();
        }
        return roles.stream()
                .map(r -> roleRepository.findById(r.getId())
                        .orElseThrow(() -> new RuntimeException("Role not found: " + r.getId())))
                .collect(Collectors.toSet());
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
