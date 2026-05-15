package com.webempresarial.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webempresarial.store.model.AuthUser;

import java.util.Optional;

public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {

    Optional<AuthUser> findByEmail(String email);

    boolean existsByEmail(String email);
}
