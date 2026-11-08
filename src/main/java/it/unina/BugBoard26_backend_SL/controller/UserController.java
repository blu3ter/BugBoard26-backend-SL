package it.unina.BugBoard26_backend_SL.controller;

import it.unina.BugBoard26_backend_SL.dto.LoginRequestDto;
import it.unina.BugBoard26_backend_SL.dto.LoginResponseDto;
import it.unina.BugBoard26_backend_SL.dto.RegisterRequestDto;
import it.unina.BugBoard26_backend_SL.dto.UserSummaryDto;
import it.unina.BugBoard26_backend_SL.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    // 0. ELENCO UTENTI (riservato agli ADMIN: serve per assegnare le issue)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<List<UserSummaryDto>> getAllUsers() {
        return ResponseEntity.ok(userService.findAllSummaries());
    }

    // 1. LOGIN
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequest) {
        try {
            LoginResponseDto response = userService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Email o password non corretti");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore durante il login: " + e.getMessage());
        }
    }

    // 2. REGISTER
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDto registerRequest) {
        try {
            userService.register(registerRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Utente registrato con successo");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore durante la registrazione: " + e.getMessage());
        }
    }

    // 3. VERIFICA TOKEN (opzionale, utile per il frontend)
    @GetMapping("/auth/verify")
    public ResponseEntity<?> verifyToken(@RequestHeader("Authorization") String token) {
        try {
            boolean valid = userService.verifyToken(token);
            if (valid) {
                return ResponseEntity.ok("Token valido");
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token non valido");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token scaduto o malformato");
        }
    }
}
