package com.ecomerce.store.config;

import com.ecomerce.store.service.AuthUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
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

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // ====================================
    // LOGIN ADMIN
    // ====================================
    @Bean
    @Order(1)
    public SecurityFilterChain adminSecurity(
            HttpSecurity http
    ) throws Exception {

        http
            .securityMatcher("/admin/**")

            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/login").permitAll()
                .anyRequest().hasRole("ADMIN")
            )

            .formLogin(form -> form
                .loginPage("/admin/login")
                .loginProcessingUrl("/admin/login")
                .defaultSuccessUrl("/admin/dashboard", true)
                .permitAll()
            )

            .logout(logout -> logout
                .logoutUrl("/admin/logout")
                .logoutSuccessUrl("/admin/login")
            );

        return http.build();
    }


    // ====================================
    // LOGIN CLIENTE
    // ====================================
    @Bean
    @Order(2)
    public SecurityFilterChain storeSecurity(
            HttpSecurity http,
            PasswordEncoder passwordEncoder
    ) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/index", "/inicio", "/privacy",
                    "/productos/**",
                    "/products/**",
                    "/producto-detalle/**",
                    "/fragmento-menu",
                    "/fragmento-resenas",

                    "/login",

                    "/themes/**",
                    "/assets/**",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/webjars/**",
                    "/favicon.ico"
                ).permitAll()

                .requestMatchers(
                    "/cuenta/**",
                    "/pedidos/**"
                ).hasRole("USER")

                .anyRequest().authenticated()
            )

            .formLogin(form -> form
            	    .loginPage("/login")
            	    .loginProcessingUrl("/login")

            	    .successHandler((request, response, authentication) -> {

            	        if (authentication.getAuthorities().stream()
            	                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {

            	            response.sendRedirect("/admin");

            	        } else {

            	            response.sendRedirect("/inicio");

            	        }
            	    })

            	    .permitAll()
            	)

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
            )

            .userDetailsService(authUserDetailsService);

        return http.build();
    }
}