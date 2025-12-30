package it.unina.BugBoard26_backend_SL.controller;

import it.unina.BugBoard26_backend_SL.dto.TagDto;
import it.unina.BugBoard26_backend_SL.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/tags")
@Validated
public class TagController {

    @Autowired
    private TagService tagService;

    @GetMapping
    public ResponseEntity<List<TagDto>> list() {
        List<TagDto> dtos = tagService.findAllDto();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagDto> get(@PathVariable Long id) {
        TagDto dto = tagService.findByIdDto(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<TagDto> create(@Valid @RequestBody TagDto dto) {
        TagDto saved = tagService.create(dto);
        return ResponseEntity.created(URI.create("/tags/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TagDto> update(@PathVariable Long id, @Valid @RequestBody TagDto dto) {
        TagDto updated = tagService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tagService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
