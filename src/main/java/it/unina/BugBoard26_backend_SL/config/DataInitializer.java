package it.unina.BugBoard26_backend_SL.config;

import it.unina.BugBoard26_backend_SL.entity.Tag;
import it.unina.BugBoard26_backend_SL.entity.User;
import it.unina.BugBoard26_backend_SL.entity.enums.Role;
import it.unina.BugBoard26_backend_SL.repository.TagRepository;
import it.unina.BugBoard26_backend_SL.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Inizializzatore automatico dei dati di base per sviluppo e testing locale.
 * Popola il database vuoto con un account ADMIN predefinito, un utente standard
 * e la palette iniziale dei Tag.
 */
@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        seedUsers();
        seedTags();
    }

    private void seedUsers() {
        if (userRepository.count() == 0) {
            log.info("Database utenti vuoto: inizializzazione utenti predefiniti in corso...");

            // 1. Account Amministratore
            User admin = User.builder()
                    .email("admin@bugboard26.it")
                    .password(passwordEncoder.encode("admin"))
                    .firstName("Admin")
                    .lastName("BugBoard")
                    .role(Role.ADMIN)
                    .build();

            // 2. Account Sviluppatore / Membro del team
            User dev = User.builder()
                    .email("dev@bugboard26.it")
                    .password(passwordEncoder.encode("dev123"))
                    .firstName("Mario")
                    .lastName("Rossi")
                    .role(Role.USER)
                    .build();

            userRepository.saveAll(Arrays.asList(admin, dev));
            log.info("Utenti inizializzati con successo: admin@bugboard26.it (pwd: admin), dev@bugboard26.it (pwd: dev123)");
        }
    }

    private void seedTags() {
        if (tagRepository.count() == 0) {
            log.info("Inizializzazione tag predefiniti in corso...");

            List<Tag> initialTags = Arrays.asList(
                    Tag.builder().name("backend").colour("#3b82f6").build(),
                    Tag.builder().name("frontend").colour("#0ea5e9").build(),
                    Tag.builder().name("security").colour("#a855f7").build(),
                    Tag.builder().name("database").colour("#eab308").build(),
                    Tag.builder().name("ui/ux").colour("#ec4899").build(),
                    Tag.builder().name("urgent").colour("#ef4444").build()
            );

            tagRepository.saveAll(initialTags);
            log.info("Tag inizializzati con successo.");
        }
    }
}

