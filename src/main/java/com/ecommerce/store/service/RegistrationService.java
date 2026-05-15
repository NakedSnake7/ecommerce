package com.ecommerce.store.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ecommerce.store.dto.auth.RegisterRequestDTO;
import com.ecommerce.store.model.AuthUser;
import com.ecommerce.store.model.User;
import com.ecommerce.store.model.AuthUser.Role;
import com.ecommerce.store.repository.AuthUserRepository;

@Service
public class RegistrationService {

    private final AuthUserRepository authUserRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(
            AuthUserRepository authUserRepository,
            UserService userService,
            PasswordEncoder passwordEncoder
    ) {
        this.authUserRepository = authUserRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(RegisterRequestDTO dto) {

        String email = dto.getEmail().trim().toLowerCase();

        // 1️⃣ ¿Ya existe login?
        if (authUserRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("El usuario ya está registrado");
        }

        // 2️⃣ Crear / reutilizar cliente
        User user = userService.findOrCreateUserByEmail(
                email,
                dto.getFullName().trim(),
                dto.getPhone()
        );

        userService.saveUser(user);

        // 3️⃣ Crear AuthUser
        AuthUser authUser = new AuthUser();
        authUser.setEmail(email);
        authUser.setPassword(passwordEncoder.encode(dto.getPassword()));
        authUser.setRole(Role.USER);
        authUser.setEnabled(true);

        authUserRepository.save(authUser);
    }
}
