package com.ecomerce.store.model;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderGenerator {
	public static void main(String[] args) {
	    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

	    if (args.length == 0) {
	        System.out.println("❌ Debes pasar una contraseña");
	        return;
	    }

	    String rawPassword = args[0];
	    String encodedPassword = encoder.encode(rawPassword);

	    System.out.println("🔐 BCrypt: " + encodedPassword);
	}
}
