package com.ewit.airshare.controller;

import com.ewit.airshare.dto.AuthResponse;
import com.ewit.airshare.dto.LoginRequest;
import com.ewit.airshare.dto.RegisterRequest;
import com.ewit.airshare.model.User;
import com.ewit.airshare.repository.UserRepository;
import com.ewit.airshare.service.DatabaseInitializer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepo;

    public AuthController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    /**
     * Register a new user in PostgreSQL
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest req) {
        if (req.getName() == null || req.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new AuthResponse(false, "Name is required"));
        }
        if (req.getEmail() == null || req.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new AuthResponse(false, "Email is required"));
        }
        if (req.getPassword() == null || req.getPassword().length() < 4) {
            return ResponseEntity.badRequest().body(new AuthResponse(false, "Password must be at least 4 characters"));
        }

        String email = req.getEmail().trim().toLowerCase();
        if (userRepo.existsByEmailIgnoreCase(email)) {
            return ResponseEntity.badRequest().body(new AuthResponse(false, "An account with this email already exists"));
        }

        String role = (req.getRole() != null && req.getRole().equalsIgnoreCase("driver")) ? "DRIVER" : "PASSENGER";
        String hashed = DatabaseInitializer.hashPassword(req.getPassword());

        User newUser = new User(
            req.getName().trim(),
            email,
            req.getPhone() != null ? req.getPhone().trim() : "",
            hashed,
            role
        );

        User saved = userRepo.save(newUser);
        return ResponseEntity.ok(new AuthResponse(
            true,
            "Account registered successfully in PostgreSQL!",
            saved.getId(),
            saved.getName(),
            saved.getEmail(),
            saved.getPhone(),
            saved.getRole()
        ));
    }

    /**
     * Sign in with email/phone and password verified against PostgreSQL
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
        if (req.getIdentifier() == null || req.getIdentifier().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new AuthResponse(false, "Email or phone number is required"));
        }
        if (req.getPassword() == null || req.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new AuthResponse(false, "Password is required"));
        }

        String id = req.getIdentifier().trim();
        Optional<User> userOpt = userRepo.findByIdentifier(id);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(new AuthResponse(false, "Account not found with provided identifier"));
        }

        User user = userOpt.get();
        String hashedInput = DatabaseInitializer.hashPassword(req.getPassword());

        if (!hashedInput.equalsIgnoreCase(user.getPasswordHash())) {
            return ResponseEntity.status(401).body(new AuthResponse(false, "Incorrect password. Please try again."));
        }

        // Role verification (optional warning/override)
        String requestedRole = req.getRole() != null ? req.getRole().toUpperCase() : user.getRole();

        return ResponseEntity.ok(new AuthResponse(
            true,
            "Sign-in successful!",
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getPhone(),
            requestedRole
        ));
    }

    /**
     * List registered users (passwords omitted) for verification & Expo judges inspection
     */
    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> listUsers() {
        List<User> all = userRepo.findAll();
        List<Map<String, Object>> response = new ArrayList<>();
        for (User u : all) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", u.getId());
            map.put("name", u.getName());
            map.put("email", u.getEmail());
            map.put("phone", u.getPhone());
            map.put("role", u.getRole());
            map.put("createdAt", u.getCreatedAt());
            response.add(map);
        }
        return ResponseEntity.ok(response);
    }
}
