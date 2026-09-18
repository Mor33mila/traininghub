package com.traininghub.identity.configuration;

import com.traininghub.identity.entity.User;
import com.traininghub.identity.entity.UserRole;
import com.traininghub.identity.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String password;
    private final String email;
    private final boolean resetPasswordOnStartup;

    public AdminInitializer(UserRepository repository, PasswordEncoder passwordEncoder,
                            @Value("${admin.username}") String username,
                            @Value("${admin.password}") String password,
                            @Value("${admin.email}") String email,
                            @Value("${admin.reset-password-on-startup:false}") boolean resetPasswordOnStartup) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.password = password;
        this.email = email;
        this.resetPasswordOnStartup = resetPasswordOnStartup;
    }

    /** Crea l'admin iniziale o completa solo la password degli utenti legacy. */
    @Override
    public void run(String... args) {
        User existingAdmin = repository.findByUsernameIgnoreCase(username).orElse(null);
        if (existingAdmin != null) {
            // Completa gli utenti creati prima dell'introduzione del login senza cambiare password esistenti.
                if (resetPasswordOnStartup || existingAdmin.getPasswordHash() == null
                    || existingAdmin.getPasswordHash().isBlank()) {
                existingAdmin.setPasswordHash(passwordEncoder.encode(password));
                existingAdmin.setRole(UserRole.ADMINISTRATOR);
                existingAdmin.setActive(true);
                repository.save(existingAdmin);
            }
            return;
        }
        User admin = new User();
        admin.setUsername(username.toLowerCase());
        admin.setFirstName("System");
        admin.setLastName("Administrator");
        admin.setEmail(email.toLowerCase());
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setRole(UserRole.ADMINISTRATOR);
        admin.setActive(true);
        repository.save(admin);
    }
}