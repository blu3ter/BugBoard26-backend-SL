package it.unina.BugBoard26_backend_SL.repository;

import it.unina.BugBoard26_backend_SL.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    // Serve per cercare se un tag esiste già (es. per non creare due tag "Java")
    Optional<Tag> findByName(String name);
}