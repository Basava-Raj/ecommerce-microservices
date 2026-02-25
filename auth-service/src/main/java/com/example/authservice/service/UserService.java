package com.example.authservice.service;

import com.example.authservice.dto.AuthResponse;
import com.example.authservice.dto.LoginRequest;
import com.example.authservice.dto.RegisterRequest;
import com.example.authservice.dto.TokenValidationResponse;
import com.example.authservice.model.Role;
import com.example.authservice.model.User;
import com.example.authservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    /**
     * Register new user
     */
    @Transactional
    public String register(RegisterRequest request) {
        logger.info("Registering new user: {}", request.getEmail());

//        Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            logger.error("Email already exists: {}", request.getEmail());
            throw new RuntimeException("Email already registered");
        }

        // check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            logger.error("Username already exists: {}", request.getUsername());
            throw new RuntimeException("Username already taken");
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setEnabled(true);

        userRepository.save(user);
        logger.info("User registered successfully: {}", request.getEmail());

        return "User registered successfully";
    }

    /**
     * Login user and generate JWT token
     */
    public AuthResponse login(LoginRequest request) {
        logger.info("Login attempt for user: {}", request.getEmail());

        //Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    logger.error("User not found: {}", request.getEmail());
                    return new RuntimeException("Invalid email or password");
                });

        //Check if user is enabled
        if(!user.getEnabled()) {
            logger.error("User account is disabled: {}", request.getEmail());
            throw new RuntimeException("User account is disabled");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            logger.error("Invalid password for user: {}", request.getEmail());
            throw new RuntimeException("Invalid email or password");
        }

        // Generate JWT token
        String token = jwtService.generateToken(user);
        logger.info("User logged in successfully: {}", request.getEmail());

        return new AuthResponse(token, user.getUsername(), user.getEmail(), user.getRole());
    }

    /**
     * Validate jwt token
     */
    public TokenValidationResponse validateToken(String token) {
        try {
            //Remove "Bearer " prefix if present
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // validate token
            if(!jwtService.validateToken(token)) {
                logger.error("Invalid or expired token");
                return new TokenValidationResponse(false, null, null, null);
            }

            // Extract user info from token
            String username = jwtService.extractUsername(token);
            String email = jwtService.extractEmail(token);
            String roleStr = jwtService.extractRole(token);
            Role role = Role.valueOf(roleStr);

            logger.info("Token validated successfully for user: {}", username);
            return new TokenValidationResponse(true, username, email, role);

        } catch (Exception e) {
            logger.error("Token validation failed: {}", e.getMessage());
            return new TokenValidationResponse(false, null, null, null);
        }


    }

    /**
     * Get user by email
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Get user by username
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

}
