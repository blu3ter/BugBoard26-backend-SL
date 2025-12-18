package it.unina.BugBoard26_backend_SL.controller;

import it.unina.BugBoard26_backend_SL.dto.LoginRequest;
import it.unina.BugBoard26_backend_SL.dto.LoginResponse;
import it.unina.BugBoard26_backend_SL.dto.RegisterRequest;
import it.unina.BugBoard26_backend_SL.entity.User;
import it.unina.BugBoard26_backend_SL.entity.enums.Role;
import it.unina.BugBoard26_backend_SL.repository.UserRepository;
import it.unina.BugBoard26_backend_SL.security.JwtUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtility jwtUtility;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 1. LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Autentico l'utente
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            // Genero il token JWT
            String token = jwtUtility.generateToken(loginRequest.getEmail());

            // Recupero i dati dell'utente
            User user = userRepository.findByEmail(loginRequest.getEmail()).orElse(null);

            return ResponseEntity.ok(new LoginResponse(
                    token,
                    loginRequest.getEmail(),
                    user != null ? user.getRole().name() : "USER"
            ));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Email o password non corretti");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore durante il login: " + e.getMessage());
        }
    }

    // 2. REGISTER
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        try {
            // Controllo se l'email esiste già
            if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Email già registrata");
            }

            // Creo il nuovo utente
            User newUser = new User();
            newUser.setEmail(registerRequest.getEmail());
            newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

            // Setto il ruolo (default: USER)
            try {
                newUser.setRole(Role.valueOf(registerRequest.getRole().toUpperCase()));
            } catch (IllegalArgumentException e) {
                newUser.setRole(Role.USER);
            }

            // Salvo nel database
            userRepository.save(newUser);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Utente registrato con successo");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore durante la registrazione: " + e.getMessage());
        }
    }

    // 3. VERIFICA TOKEN (opzionale, utile per il frontend)
    @GetMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestHeader("Authorization") String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                String jwt = token.substring(7);
                // estrai l'email dal token e verifica che l'utente esista
                String email = jwtUtility.extractEmail(jwt);
                if (email != null) {
                    var user = userRepository.findByEmail(email).orElse(null);
                    if (user != null && jwtUtility.isTokenValid(jwt, user.getEmail())) {
                        return ResponseEntity.ok("Token valido");
                    }
                }
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token non valido");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token scaduto o malformato");
        }
    }
}
