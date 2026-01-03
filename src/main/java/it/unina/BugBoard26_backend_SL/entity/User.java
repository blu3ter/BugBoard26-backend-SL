package it.unina.BugBoard26_backend_SL.entity;

import it.unina.BugBoard26_backend_SL.entity.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "L'email è obbligatoria")
    @Email(message = "Formato email non valido")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "La password è obbligatoria")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "Il nome è obbligatorio")
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank(message = "Il cognome è obbligatorio")
    @Column(name = "last_name", nullable = false)
    private String lastName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // Issue reported by this user
    @OneToMany(mappedBy = "reporter", fetch = FetchType.LAZY)
    @ToString.Exclude // Avoid circular references in toString
    private List<Issue> reportedIssues;
    
    // Issue assigned to this user
    @OneToMany(mappedBy = "assignee", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Issue> assignedIssues;

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Comment> comments;

    // Method helper
    public void assignIssue(Issue issue) {
        issue.assignTo(this);
    }

    // Convenience for frontend / DTO mapping
    public String getFullName() {
        return (firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName);
    }
}