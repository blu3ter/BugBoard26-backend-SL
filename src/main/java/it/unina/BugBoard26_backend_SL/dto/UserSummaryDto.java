package it.unina.BugBoard26_backend_SL.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDto {


    private Long id;
    private String firstName;
    private String lastName;
    private String role;

    // Comodità per il frontend quando si vuole mostrare "Nome Cognome"
    public String getFullName() {
        return (firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName).trim();
    }
}
