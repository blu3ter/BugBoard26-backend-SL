package it.unina.BugBoard26_backend_SL.config;

import it.unina.BugBoard26_backend_SL.entity.User;
import it.unina.BugBoard26_backend_SL.entity.enums.Role;
import it.unina.BugBoard26_backend_SL.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByEmail("admin@bugboard.it").isEmpty()) {
                User admin = User.builder()
                        .email("admin@bugboard.it")
                        .password(passwordEncoder.encode("password123"))
                        .firstName("Admin")
                        .lastName("BugBoard")
                        .role(Role.ADMIN)
                        .build();
                userRepository.save(admin);
                System.out.println("==================================================");
                System.out.println(">>> [BugBoard26] Utente Admin iniziale creato:");
                System.out.println(">>> Email: admin@bugboard.it");
                System.out.println(">>> Password: password123");
                System.out.println("==================================================");
            }
        };
    }
}
