package it.unina.BugBoard26_backend_SL.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IssueAssignDto {

    @NotNull(message = "L'ID dell'assegnatario è obbligatorio")
    private Long assigneeId;
}