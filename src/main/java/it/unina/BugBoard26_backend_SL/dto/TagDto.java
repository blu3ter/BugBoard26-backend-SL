package it.unina.BugBoard26_backend_SL.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagDto {

    private Long id;

    @NotBlank
    private String name;

    private String colour;
}
