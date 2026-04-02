package com.ecomerce.store.config;

import com.ecomerce.store.model.AuthUser;
import com.ecomerce.store.repository.AuthUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initAdmin(AuthUserRepository authUserRepository,
                                PasswordEncoder passwordEncoder) {
        return args -> {

            String adminEmail = "admin@admin.com";

            boolean exists = authUserRepository.existsByEmail(adminEmail);

            if (!exists) {
                AuthUser admin = new AuthUser();
                admin.setEmail(adminEmail);

                // 🔐 IMPORTANTE: encriptar contraseña
                admin.setPassword(passwordEncoder.encode("Admin123*"));

                admin.setRole(AuthUser.Role.ADMIN);
                admin.setEnabled(true);
                admin.setProfileCompleted(true);

                authUserRepository.save(admin);

                System.out.println("🔥 ADMIN creado: " + adminEmail);
            } else {
                System.out.println("✅ ADMIN ya existe");
            }
        };
    }
}