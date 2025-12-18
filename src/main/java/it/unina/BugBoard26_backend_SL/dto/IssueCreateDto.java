package it.unina.BugBoard26_backend_SL.dto;

import it.unina.BugBoard26_backend_SL.entity.enums.IssueType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class IssueCreateDto {

    @NotBlank
    private String title;

    private String description;

    private Boolean isUrgent = false;

    @NotNull
    private IssueType type;

    @NotNull
    private Long reporterId;

    private Long assigneeId;

    private List<String> tagNames;

    public IssueCreateDto() {}

    // getters and setters

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Boolean getIsUrgent() { return isUrgent; }
    public void setIsUrgent(Boolean isUrgent) { this.isUrgent = isUrgent; }
    public IssueType getType() { return type; }
    public void setType(IssueType type) { this.type = type; }
    public Long getReporterId() { return reporterId; }
    public void setReporterId(Long reporterId) { this.reporterId = reporterId; }
    public Long getAssigneeId() { return assigneeId; }
    public void setAssigneeId(Long assigneeId) { this.assigneeId = assigneeId; }
    public List<String> getTagNames() { return tagNames; }
    public void setTagNames(List<String> tagNames) { this.tagNames = tagNames; }
}
