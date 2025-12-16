package it.unina.BugBoard26_backend_SL.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtility jwtUtility;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Prendo l'header "Authorization"
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 2. Controllo se l'header c'è e inizia con "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Se non c'è il token, passo oltre (sarà bloccato dopo se la rotta è protetta)
            return;
        }

        // 3. Estraggo il token (tolgo "Bearer " che sono 7 caratteri)
        jwt = authHeader.substring(7);
        
        try {
            userEmail = jwtUtility.extractEmail(jwt); // Estraggo l'email dal token
        } catch (Exception e) {
            // Se il token è manomesso o scaduto, continuo senza autenticare
            filterChain.doFilter(request, response);
            return;
        }

        // 4. Se ho trovato l'email e l'utente non è già autenticato nel contesto
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            // Carico i dettagli dell'utente dal DB
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 5. Verifico se il token è valido per questo utente
            if (jwtUtility.isTokenValid(jwt, userDetails.getUsername())) {
                
                // Creo il "pass" di Spring Security
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 6. REGISTRO L'UTENTE COME AUTENTICATO! 🎉
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Passo la palla al prossimo filtro
        filterChain.doFilter(request, response);
    }
}