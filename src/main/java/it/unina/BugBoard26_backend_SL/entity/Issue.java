package it.unina.BugBoard26_backend_SL.entity;

import it.unina.BugBoard26_backend_SL.entity.enums.IssueState;
import it.unina.BugBoard26_backend_SL.entity.enums.IssueType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private IssueType type;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private IssueState state;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reporter_email", nullable = false)
	private User reporter;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tag_id", nullable = false)
	private Tag tag;
}
