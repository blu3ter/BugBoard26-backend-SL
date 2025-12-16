package it.unina.BugBoard26_backend_SL.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtility {

    // CHIAVE SEGRETA: In un progetto vero va in application.properties!
    // Deve essere lunga almeno 256 bit (32 caratteri) per l'algoritmo HS256.
    private static final String SECRET = "PasswordHashataPerJWTCheDeveEssereMoltoLunga!Infattiverràcriptata."; 

    // Durata del token: 1 ora (in millisecondi)
    private static final long EXPIRATION_TIME = 3600000; 

    // Genera la chiave crittografica partendo dalla stringa segreta
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(java.util.Base64.getEncoder().encodeToString(SECRET.getBytes()));
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // 1. GENERAZIONE DEL TOKEN (Lo userai nel Service di Login)
    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email) // Il "Soggetto" del token è l'email
                .issuedAt(new Date(System.currentTimeMillis())) // Data creazione
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // Scadenza
                .signWith(getSignInKey()) // Firma con la chiave segreta
                .compact();
    }

    // 2. VALIDAZIONE E LETTURA (Lo userà il Filtro)
    
    // Estrae l'email dal token
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Controlla se il token è valido
    public boolean isTokenValid(String token, String emailFromDb) {
        final String emailInToken = extractEmail(token);
        return (emailInToken.equals(emailFromDb) && !isTokenExpired(token));
    }

    // Metodi di supporto generici
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}