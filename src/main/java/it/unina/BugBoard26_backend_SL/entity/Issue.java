package it.unina.BugBoard26_backend_SL.entity;

import it.unina.BugBoard26_backend_SL.entity.enums.IssueType;
import it.unina.BugBoard26_backend_SL.entity.state.IssueState;
import it.unina.BugBoard26_backend_SL.entity.state.TodoState;
import it.unina.BugBoard26_backend_SL.entity.converter.IssueStateConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "issues")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Issue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_urgent")
    private Boolean isUrgent; 

    
    @Lob
    @Column(name = "image", length = 1000000) //1MB
    private byte[] image;
    
    @Column(name = "creation_date", updatable = false)
    @Builder.Default 
    private LocalDateTime creationDate = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssueType type;

    //now is an object , not a simple enum or string
    @Convert(converter = IssueStateConverter.class)
    @Builder.Default
    private IssueState state = new TodoState(); // Default state is TODO

    // User that reported the bug
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    @ToString.Exclude
    private User reporter;

    // User assigned to fix the bug
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    @ToString.Exclude
    private User assignee;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "issue_tags",
        joinColumns = @JoinColumn(name = "issue_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @ToString.Exclude
    private List<Tag> tags;

    

    // State transition methods
    
    public void assignTo(User user) {
        this.state.assign(this, user);
    }

    public void close() {
        this.state.close(this);
    }
}