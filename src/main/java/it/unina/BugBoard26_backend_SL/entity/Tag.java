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
    private String name; // Nel diagramma si chiama 'name', nel tuo codice 'label'. Meglio 'name'.

    @Column(length = 7)
    private String color; // Nel diagramma è 'color'. Esempio: #FF0000

    // Relazione ManyToMany inversa (opzionale, ma utile se vuoi sapere quali issue ha un tag)
    @ManyToMany(mappedBy = "tags")
    private List<Issue> issues;
}