package it.unina.BugBoard26_backend_SL.dto;

import it.unina.BugBoard26_backend_SL.entity.enums.IssueType;
import java.time.LocalDateTime;
import java.util.List;

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
    private Long assigneeId;
    private String assigneeEmail;
    private List<String> tags;

    public IssueDto() {}

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Boolean getIsUrgent() { return isUrgent; }
    public void setIsUrgent(Boolean isUrgent) { this.isUrgent = isUrgent; }
    public LocalDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDateTime creationDate) { this.creationDate = creationDate; }
    public IssueType getType() { return type; }
    public void setType(IssueType type) { this.type = type; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public Long getReporterId() { return reporterId; }
    public void setReporterId(Long reporterId) { this.reporterId = reporterId; }
    public String getReporterEmail() { return reporterEmail; }
    public void setReporterEmail(String reporterEmail) { this.reporterEmail = reporterEmail; }
    public Long getAssigneeId() { return assigneeId; }
    public void setAssigneeId(Long assigneeId) { this.assigneeId = assigneeId; }
    public String getAssigneeEmail() { return assigneeEmail; }
    public void setAssigneeEmail(String assigneeEmail) { this.assigneeEmail = assigneeEmail; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}
