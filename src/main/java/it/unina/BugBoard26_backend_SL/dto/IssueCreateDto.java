package it.unina.BugBoard26_backend_SL.dto;

import it.unina.BugBoard26_backend_SL.entity.enums.IssueType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class IssueCreateDto {

    @NotBlank
    private String title;

    private String description;

    private Boolean isUrgent = false; // default

    @NotNull
    private IssueType type;

    @NotNull
    private Long reporterId;

    private Long assigneeId;

    private List<String> tagNames;

   
}
