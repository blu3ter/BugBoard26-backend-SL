package it.unina.BugBoard26_backend_SL.repository;

import it.unina.BugBoard26_backend_SL.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    
    // Essenziale per il Login e per il Token JWT
    Optional<User> findByEmail(String email);
    

}