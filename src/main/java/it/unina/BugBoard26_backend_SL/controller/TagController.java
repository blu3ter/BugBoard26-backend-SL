package it.unina.BugBoard26_backend_SL.controller;

import it.unina.BugBoard26_backend_SL.dto.TagDto;
import it.unina.BugBoard26_backend_SL.entity.Tag;
import it.unina.BugBoard26_backend_SL.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tags")
@Validated
public class TagController {

    @Autowired
    private TagService tagService;

    @GetMapping
    public ResponseEntity<List<TagDto>> list() {
        List<Tag> tags = tagService.findAll();
        List<TagDto> dtos = tags.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagDto> get(@PathVariable Long id) {
        Tag t = tagService.findById(id).orElseThrow(NoSuchElementException::new);
        return ResponseEntity.ok(toDto(t));
    }

    @PostMapping
    public ResponseEntity<TagDto> create(@Valid @RequestBody TagDto dto) {
        Tag t = new Tag();
        t.setName(dto.getName());
        t.setColour(dto.getColour());
        Tag saved = tagService.save(t);
        return ResponseEntity.created(URI.create("/tags/" + saved.getId())).body(toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TagDto> update(@PathVariable Long id, @Valid @RequestBody TagDto dto) {
        Tag existing = tagService.findById(id).orElseThrow(NoSuchElementException::new);
        existing.setName(dto.getName());
        existing.setColour(dto.getColour());
        Tag saved = tagService.save(existing);
        return ResponseEntity.ok(toDto(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tagService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private TagDto toDto(Tag t) {
        TagDto d = new TagDto();
        d.setId(t.getId());
        d.setName(t.getName());
        d.setColour(t.getColour());
        return d;
    }
}
