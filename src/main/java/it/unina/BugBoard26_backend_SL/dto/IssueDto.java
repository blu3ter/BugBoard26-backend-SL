package it.unina.BugBoard26_backend_SL.dto;

import it.unina.BugBoard26_backend_SL.entity.enums.IssueType;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class IssueDto {
    private Long id;
    private String title;
    private String description;
    private Boolean isUrgent;
    private LocalDateTime creationDate;
    private IssueType type;
    private String state;
    private Long reporterId;
    private String reporterEmail;
    private String reporterFullName;
    private Long assigneeId;
    private String assigneeEmail;
    private String assigneeFullName;
    private List<String> tags;
    private Long commentsCount;
}
