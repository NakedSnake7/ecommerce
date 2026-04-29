package com.ecomerce.store.controller;

import com.ecomerce.store.dto.auth.RegisterRequestDTO;
import com.ecomerce.store.service.RegistrationService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegistrationService registrationService;
    private final AuthenticationManager authenticationManager;

    
    public AuthController(
            RegistrationService registrationService,
            AuthenticationManager authenticationManager
    ) {
        this.registrationService = registrationService;
        this.authenticationManager = authenticationManager;
    }
    
    
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequestDTO dto,
            HttpServletRequest request
    ) {
        // 1️⃣ Registrar usuario
        registrationService.register(dto);

        // 2️⃣ Autenticarlo
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                		dto.getEmail(), 
                        dto.getPassword()
                );

        Authentication authentication =
                authenticationManager.authenticate(authToken);

        // 3️⃣ Guardar autenticación en contexto
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        // 4️⃣ Guardar sesión (MUY IMPORTANTE)
        request.getSession(true)
               .setAttribute(
                   HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                   context
               );

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Registro exitoso"
        ));
    }

}
