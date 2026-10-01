package it.unina.BugBoard26_backend_SL.controller;

import lombok.extern.slf4j.Slf4j;
import it.unina.BugBoard26_backend_SL.dto.IssueAssignDto;
import it.unina.BugBoard26_backend_SL.dto.IssueCreateDto;
import it.unina.BugBoard26_backend_SL.dto.IssueDto;
import it.unina.BugBoard26_backend_SL.dto.IssueUpdateDto;
import it.unina.BugBoard26_backend_SL.service.IssueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

/**
 * Controller snello: si occupa solo di HTTP, validazione e delega al Service.
 */
@RestController
@RequestMapping("/issues")
@Validated
@Slf4j
public class IssueController {

    @Autowired
    private IssueService issueService;

    @GetMapping
    public ResponseEntity<List<IssueDto>> list(@RequestParam(required = false) String tag) {
        List<IssueDto> dtos = (tag != null) ? issueService.findByTagNameDto(tag) : issueService.findAllDto();
        return ResponseEntity.ok(dtos);
    }

    /**
     * Endpoint per ottenere le issue assegnate all'utente attualmente autenticato.
     * URL: GET /issues/me
     */
    @GetMapping("/me")
    public ResponseEntity<List<IssueDto>> myIssues() {
        List<IssueDto> dtos = issueService.findByAssigneeCurrentUser();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IssueDto> get(@PathVariable Long id) {
        IssueDto dto = issueService.findByIdDto(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping(consumes = { "multipart/form-data" })
    public ResponseEntity<IssueDto> create(@Valid @RequestPart("data") IssueCreateDto dto,
            @RequestPart(value = "image", required = false) MultipartFile image) throws Exception {
        log.info("Ricevuta richiesta creazione issue da utente autenticato: {}",
                SecurityContextHolder.getContext().getAuthentication() != null
                        ? SecurityContextHolder.getContext().getAuthentication().getName()
                        : "anonymous");
        byte[] imgBytes = (image != null && !image.isEmpty()) ? image.getBytes() : null;
        IssueDto saved = issueService.createIssue(dto, imgBytes);
        log.info("Issue creata con successo: {}", saved.getId());
        return ResponseEntity.created(URI.create("/issues/" + saved.getId())).body(saved);
    }

    @PutMapping(path = "/{id}", consumes = { "multipart/form-data" })
    public ResponseEntity<IssueDto> update(@PathVariable Long id,
            @Valid @RequestPart("data") IssueUpdateDto dto,
            @RequestPart(value = "image", required = false) MultipartFile image) throws Exception {
        byte[] imgBytes = (image != null && !image.isEmpty()) ? image.getBytes() : null;
        IssueDto updated = issueService.updateIssue(id, dto, imgBytes);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        issueService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<IssueDto> assign(@PathVariable Long id, @Valid @RequestBody IssueAssignDto assignDto) {
        IssueDto updated = issueService.assignTo(id, assignDto);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<IssueDto> close(@PathVariable Long id) {
        IssueDto updated = issueService.closeIssue(id);
        return ResponseEntity.ok(updated);
    }
}
