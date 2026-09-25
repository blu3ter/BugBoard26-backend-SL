package it.unina.BugBoard26_backend_SL.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor 
@AllArgsConstructor 
public class LoginResponseDto {
    private String token;
    private String email;
    private String role;
    private String name;
}