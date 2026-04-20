package com.ecomerce.store.config;

import com.ecomerce.store.service.AuthUserDetailsService; 
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final AuthUserDetailsService authUserDetailsService;

    public SecurityConfig(AuthUserDetailsService authUserDetailsService) {
        this.authUserDetailsService = authUserDetailsService;
    }

    // AuthenticationManager moderno
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // Configuración de seguridad
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            PasswordEncoder passwordEncoder
    ) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
            		.requestMatchers(
            			    "/", "/index", "/inicio","/privacy", "/productos/**", "/products/**",
            			    "/api/checkout/**", "/api/auth/register",
            			    "/login", "/menu","/fragmento-resenas",
            			    "/fragmento-menu",
            			    "/css/**", "/js/**", "/images/**",
            			    "/assets/**",        
            			    "/webjars/**",
            			    "/favicon.ico"
            			).permitAll()
                .requestMatchers(
                    "/subirProducto", "/VerProductos", "/servicio", "/orders",
                    "/order-details", "/modificar-precios"
                ).hasRole("ADMIN")
                .requestMatchers(
                    "/cuenta/**", "/pedidos/**"
                ).hasRole("USER")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/inicio", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            )
            .userDetailsService(authUserDetailsService); // UserDetailsService inyectado

        return http.build();
    }
}
