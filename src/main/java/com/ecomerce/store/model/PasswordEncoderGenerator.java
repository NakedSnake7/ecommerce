package com.ecomerce.store.model;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "Bluesuedeshoes7";  // 🔑 Cambia esto por la nueva contraseña
        String encodedPassword = encoder.encode(rawPassword);
        System.out.println("Nueva contraseña en BCrypt: " + encodedPassword);
    }
}
