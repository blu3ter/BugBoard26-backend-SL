package it.unina.BugBoard26_backend_SL.entity;

import it.unina.BugBoard26_backend_SL.entity.enums.IssueState;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, unique = true)
	private IssueState state; // canonical issue state

	@Column(nullable = false)
	private String label; // human-friendly label

	@Column(length = 7)
	private String colorHex; // e.g. #FFCC00

	@OneToMany(mappedBy = "tag", fetch = FetchType.LAZY)
	private List<Issue> issues;
}
