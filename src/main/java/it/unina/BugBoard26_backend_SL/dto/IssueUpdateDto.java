package it.unina.BugBoard26_backend_SL.dto;

import it.unina.BugBoard26_backend_SL.entity.enums.IssueType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class IssueUpdateDto {

    @NotBlank
    private String title;

    private String description;

    private Boolean isUrgent = false;

    @NotNull
    private IssueType type;

    private Long assigneeId;

    private List<String> tagNames;

}
