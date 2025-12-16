package it.unina.BugBoard26_backend_SL.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    // 1. IL FILTRO DI SICUREZZA (Le Regole)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // Disabilita CSRF (non serve per JWT/Stateless)
            .authorizeHttpRequests(auth -> auth
                // LE ROTTE PUBBLICHE (Aperte a tutti)
                .requestMatchers("/auth/**").permitAll() // Login e Register
                .requestMatchers("/h2-console/**").permitAll() // Console H2 (se la usi)
                // TUTTE LE ALTRE SONO PROTETTE
                .anyRequest().authenticated()
            )
            .sessionManagement(sess -> sess
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Niente sessioni server!
            )
            .authenticationProvider(authenticationProvider()) // Dice come caricare gli utenti
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // Metti il filtro JWT prima!

        // Fix per poter vedere la console H2 nel browser (opzionale)
        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));

        return http.build();
    }

    // 2. IL MANAGER DELL'AUTENTICAZIONE
    // Serve al tuo AuthController per fare il login manuale
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // 3. IL PROVIDER (Collega il Service e l'Encoder)
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService); // Usa il nostro CustomUserDetailsService
        authProvider.setPasswordEncoder(passwordEncoder()); // Usa BCrypt
        return authProvider;
    }

    // 4. L'ENCODER DELLE PASSWORD
    // Trasforma "pippo" in "$2a$10$..."
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}