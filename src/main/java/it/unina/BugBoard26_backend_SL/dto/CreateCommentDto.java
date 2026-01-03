package it.unina.BugBoard26_backend_SL.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentDto {
    @NotBlank(message = "Il testo del commento non può essere vuoto")
    @Size(max = 2000, message = "Il commento può avere al massimo 2000 caratteri")
    private String text;
}
