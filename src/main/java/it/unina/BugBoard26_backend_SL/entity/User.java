package it.unina.BugBoard26_backend_SL.entity;

import it.unina.BugBoard26_backend_SL.entity.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;    

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    //Issue reported by this user
    @OneToMany(mappedBy = "reporter", fetch = FetchType.LAZY)
    @ToString.Exclude // Avoid circular references in toString
    private List<Issue> reportedIssues;
    
    // Issue assigned to this user
    @OneToMany(mappedBy = "assignee", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Issue> assignedIssues;

    // Method helper
    public void assignIssue(Issue issue) {
        issue.assignTo(this);
    }
}