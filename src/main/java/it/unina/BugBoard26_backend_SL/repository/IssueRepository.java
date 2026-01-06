package it.unina.BugBoard26_backend_SL.repository;

import it.unina.BugBoard26_backend_SL.entity.Issue;
import it.unina.BugBoard26_backend_SL.entity.enums.IssueType;
import it.unina.BugBoard26_backend_SL.entity.state.IssueState;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IssueRepository extends JpaRepository<Issue, Long> {
 
    // Req 3: Filtro per tipo (BUG, FEATURE, etc.)
    List<Issue> findByType(IssueType type);
 
    // Req 3: Filtro per stato.
    // Grazie al tuo Converter, Spring capisce come trasformare lo State in stringa per la query!
    List<Issue> findByState(IssueState state);
 
    // Req 3: Filtro per urgenza (nota: il campo nella entity si chiama "isUrgent")
    List<Issue> findByIsUrgent(Boolean isUrgent);
 
    // Req 10 & 3: Filtro per Tag.
    // "Entra nella lista 'tags' e controlla il campo 'name' dell'oggetto Tag"
    List<Issue> findByTags_Name(String tagName);
 
    // Filtro per assignee (usato per "My Issues")
    List<Issue> findByAssignee_Id(Long assigneeId);
}