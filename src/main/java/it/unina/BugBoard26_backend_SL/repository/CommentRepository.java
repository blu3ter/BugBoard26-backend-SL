package it.unina.BugBoard26_backend_SL.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import it.unina.BugBoard26_backend_SL.entity.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByIssueIdOrderByCreationDateAsc(Long issueId);

    long countByIssueId(Long issueId);

    @Query("SELECT c.issue.id, COUNT(c) FROM Comment c WHERE c.issue.id IN :issueIds GROUP BY c.issue.id")
    List<Object[]> countByIssueIds(@Param("issueIds") List<Long> issueIds);
}
