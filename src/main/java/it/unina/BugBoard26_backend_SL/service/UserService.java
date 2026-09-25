package it.unina.BugBoard26_backend_SL.service;

import it.unina.BugBoard26_backend_SL.dto.LoginRequestDto;
import it.unina.BugBoard26_backend_SL.dto.LoginResponseDto;
import it.unina.BugBoard26_backend_SL.dto.RegisterRequestDto;
import it.unina.BugBoard26_backend_SL.entity.User;
import it.unina.BugBoard26_backend_SL.entity.enums.Role;
import it.unina.BugBoard26_backend_SL.repository.UserRepository;
import it.unina.BugBoard26_backend_SL.security.JwtUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtility jwtUtility;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    // --- Business logic moved from controller: authentication, registration, token verification
    public LoginResponseDto login(LoginRequestDto request) throws BadCredentialsException, NoSuchElementException {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String token = jwtUtility.generateToken(request.getEmail());
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(NoSuchElementException::new);

        return new LoginResponseDto(token, request.getEmail(), user != null ? user.getRole().name() : Role.USER.name(), user != null ? user.getFirstName() : "");
    }

    @Transactional
    public void register(RegisterRequestDto request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("Email già registrata");
        }

        User newUser = new User();
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setFirstName(request.getFirstName());
        newUser.setLastName(request.getLastName());

        try {
            if (request.getRole() != null) {
                newUser.setRole(Role.valueOf(request.getRole().toUpperCase()));
            } else {
                newUser.setRole(Role.USER);
            }
        } catch (IllegalArgumentException e) {
            newUser.setRole(Role.USER);
        }

        userRepository.save(newUser);
    }

    public boolean verifyToken(String tokenHeader) {
        if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
            String jwt = tokenHeader.substring(7);
            String email = jwtUtility.extractEmail(jwt);
            if (email != null) {
                var user = userRepository.findByEmail(email).orElse(null);
                return user != null && jwtUtility.isTokenValid(jwt, user.getEmail());
            }
        }
        return false;
    }
}
