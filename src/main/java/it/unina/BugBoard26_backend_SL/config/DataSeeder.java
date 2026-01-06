
package it.unina.BugBoard26_backend_SL.config;

import it.unina.BugBoard26_backend_SL.entity.User;
import it.unina.BugBoard26_backend_SL.entity.enums.Role;
import it.unina.BugBoard26_backend_SL.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Controlla se esiste già un admin, altrimenti lo crea
        if (userRepository.findByEmail("admin@bugboard.com").isEmpty()) {
            User admin = new User();
            admin.setEmail("admin@bugboard.com");
