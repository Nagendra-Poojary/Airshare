package com.ewit.airshare.service;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class DatabaseInitializer {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        try {
            // Automatically ensure the users table exists in PostgreSQL
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id BIGSERIAL PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    email VARCHAR(150) NOT NULL,
                    phone VARCHAR(30),
                    password_hash VARCHAR(255) NOT NULL,
                    role VARCHAR(30) DEFAULT 'PASSENGER',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
            """);

            jdbcTemplate.execute("""
                CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email ON users (LOWER(email));
            """);

            // Seed demo accounts if empty
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
            if (count != null && count == 0) {
                String demoHash = hashPassword("password123");
                jdbcTemplate.update("""
                    INSERT INTO users (name, email, phone, password_hash, role)
                    VALUES (?, ?, ?, ?, ?)
                """, "Ananya Rao", "ananya@airshare.in", "+91 98765 43210", demoHash, "PASSENGER");

                jdbcTemplate.update("""
                    INSERT INTO users (name, email, phone, password_hash, role)
                    VALUES (?, ?, ?, ?, ?)
                """, "Rajesh Kumar", "rajesh@airshare.in", "+91 98765 12345", demoHash, "DRIVER");

                System.out.println("✅ AirShare: Initialized users table & seeded demo accounts (password: password123)");
            } else {
                System.out.println("✅ AirShare: Users table verified in PostgreSQL (count = " + count + ")");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Warning during database initialization: " + e.getMessage());
        }
    }

    public static String hashPassword(String plain) {
        if (plain == null) return "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(plain.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return plain; // fallback
        }
    }
}
