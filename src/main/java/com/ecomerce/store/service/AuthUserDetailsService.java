package com.ecomerce.store.service;

import com.ecomerce.store.model.AuthUser; 
import com.ecomerce.store.repository.AuthUserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class AuthUserDetailsService implements UserDetailsService {

    private final AuthUserRepository authUserRepository;

    public AuthUserDetailsService(AuthUserRepository authUserRepository) {
        this.authUserRepository = authUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 🔍 Buscar usuario en la base de datos por email
        AuthUser authUser = authUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        // ⚡ Convertir el rol en GrantedAuthority
        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority("ROLE_" + authUser.getRole().name());

        // 🔑 Construir UserDetails para Spring Security
        return org.springframework.security.core.userdetails.User.builder()
                .username(authUser.getEmail())
                .password(authUser.getPassword())
                .authorities(Collections.singleton(authority))
                .disabled(!authUser.isEnabled()) // si enabled=false bloquea login
                .build();
    }
}
