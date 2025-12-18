package it.unina.BugBoard26_backend_SL.dto;

import jakarta.validation.constraints.NotBlank;

public class TagDto {

    private Long id;

    @NotBlank
    private String name;

    private String colour;

    public TagDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getColour() { return colour; }
    public void setColour(String colour) { this.colour = colour; }
}
