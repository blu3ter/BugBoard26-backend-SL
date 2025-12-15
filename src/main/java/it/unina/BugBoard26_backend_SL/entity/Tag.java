package it.unina.BugBoard26_backend_SL.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "tags")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; 

    @Column(length = 7)
    private String colour; 

    @ManyToMany(mappedBy = "tags")
    @ToString.Exclude
    private List<Issue> issues;
}