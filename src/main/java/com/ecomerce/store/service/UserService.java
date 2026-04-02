package com.ecomerce.store.service;

import com.ecomerce.store.repository.UserRepository;         
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ecomerce.store.exceptions.UserNotFoundException;
import com.ecomerce.store.model.User;


import java.util.Optional;
	
@Service
public class UserService {

    private final UserRepository userRepository;
    private final OrderService orderService;

    public UserService(UserRepository userRepository, OrderService orderService) {
        this.userRepository = userRepository;
        this.orderService = orderService;
    }

    public User findOrCreateUserByEmail(String email, String name, String phone) {

        String normalizedEmail = email.trim().toLowerCase();

        return userRepository.findByEmail(normalizedEmail)
                .orElseGet(() -> {
                    User user = new User();
                    user.setEmail(normalizedEmail);
                    user.setFullName(name);
                    user.setPhone(phone != null ? phone : "No disponible");
                    return userRepository.save(user);
                });
    }

    @Transactional
    public User registerUser(String email, String name, String phone) {

        String normalizedEmail = email.trim().toLowerCase();

        Optional<User> existingUser = userRepository.findByEmail(normalizedEmail);

        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setFullName(name);
        user.setPhone(phone != null ? phone : "No disponible");

        User savedUser = userRepository.save(user);

        // 🔥 clave del negocio
        orderService.claimGuestOrders(savedUser);

        return savedUser;
    }


    // Verifica si el email ya existe en la base de datos
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // Guardar un usuario (solo nombre y correo)
    @Transactional  
    public User saveUser(User user) {
        // Verificar si el email ya está registrado
    	 Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
         if (existingUser.isPresent()) {
             return existingUser.get(); // Devuelve el usuario existente
         }
         // Guardar un nuevo usuario si no existe
         return userRepository.save(user);
    }

    // Buscar un usuario por su correo electrónico
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    

    // Buscar un usuario por correo o lanzar una excepción si no existe
    public User findUserByEmail(String email) {
        return findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con el email: " + email));
    }
    public void save(User user) {
        userRepository.save(user);
    }

}