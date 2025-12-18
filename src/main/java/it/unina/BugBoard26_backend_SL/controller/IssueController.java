package it.unina.BugBoard26_backend_SL.controller;

import it.unina.BugBoard26_backend_SL.dto.IssueCreateDto;
import it.unina.BugBoard26_backend_SL.dto.IssueDto;
import it.unina.BugBoard26_backend_SL.dto.IssueUpdateDto;
import it.unina.BugBoard26_backend_SL.entity.Issue;
import it.unina.BugBoard26_backend_SL.entity.Tag;
import it.unina.BugBoard26_backend_SL.entity.User;
import it.unina.BugBoard26_backend_SL.service.IssueService;
import it.unina.BugBoard26_backend_SL.service.TagService;
import it.unina.BugBoard26_backend_SL.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/issues")
@Validated
public class IssueController {

    @Autowired
    private IssueService issueService;

    @Autowired
    private TagService tagService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<IssueDto>> list(@RequestParam(required = false) String tag) {
        List<Issue> issues;
        if (tag != null) {
            issues = issueService.findByTagName(tag);
        } else {
            issues = issueService.findAll();
        }
        List<IssueDto> dtos = issues.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IssueDto> get(@PathVariable Long id) {
        Issue issue = issueService.findById(id).orElseThrow(NoSuchElementException::new);
        return ResponseEntity.ok(toDto(issue));
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<IssueDto> create(@Valid @RequestPart("data") IssueCreateDto dto,
                                           @RequestPart(value = "image", required = false) MultipartFile image) throws Exception {
        Issue issue = mapToEntity(dto, image);
        Issue saved = issueService.save(issue);
        return ResponseEntity.created(URI.create("/issues/" + saved.getId())).body(toDto(saved));
    }

    @PutMapping(path = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<IssueDto> update(@PathVariable Long id,
                                           @Valid @RequestPart("data") IssueUpdateDto dto,
                                           @RequestPart(value = "image", required = false) MultipartFile image) throws Exception {
        Issue existing = issueService.findById(id).orElseThrow(NoSuchElementException::new);
        existing.setTitle(dto.getTitle());
        existing.setDescription(dto.getDescription());
        existing.setIsUrgent(dto.getIsUrgent());
        existing.setType(dto.getType());
        if (dto.getAssigneeId() != null) {
            User u = userService.findById(dto.getAssigneeId()).orElseThrow(NoSuchElementException::new);
            existing.setAssignee(u);
        }
        if (dto.getTagNames() != null) {
            List<Tag> tags = new ArrayList<>();
            for (String n : dto.getTagNames()) {
                Tag t = tagService.findByName(n).orElseGet(() -> tagService.save(new Tag(null, n, null, null)));
                tags.add(t);
            }
            existing.setTags(tags);
        }
        if (image != null && !image.isEmpty()) {
            existing.setImage(image.getBytes());
        }
        Issue saved = issueService.save(existing);
        return ResponseEntity.ok(toDto(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        issueService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<IssueDto> assign(@PathVariable Long id, @RequestParam Long userId) {
        Issue updated = issueService.assignTo(id, userId);
        return ResponseEntity.ok(toDto(updated));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<IssueDto> close(@PathVariable Long id) {
        Issue updated = issueService.closeIssue(id);
        return ResponseEntity.ok(toDto(updated));
    }

    // Mapping helpers
    private IssueDto toDto(Issue i) {
        IssueDto dto = new IssueDto();
        dto.setId(i.getId());
        dto.setTitle(i.getTitle());
        dto.setDescription(i.getDescription());
        dto.setIsUrgent(i.getIsUrgent());
        dto.setCreationDate(i.getCreationDate());
        dto.setType(i.getType());
        dto.setState(i.getState() != null ? i.getState().getStateName() : null);
        if (i.getReporter() != null) {
            dto.setReporterId(i.getReporter().getId());
            dto.setReporterEmail(i.getReporter().getEmail());
        }
        if (i.getAssignee() != null) {
            dto.setAssigneeId(i.getAssignee().getId());
            dto.setAssigneeEmail(i.getAssignee().getEmail());
        }
        if (i.getTags() != null) {
            dto.setTags(i.getTags().stream().map(Tag::getName).collect(Collectors.toList()));
        }
        return dto;
    }

    private Issue mapToEntity(IssueCreateDto dto, MultipartFile image) throws Exception {
        Issue i = new Issue();
        i.setTitle(dto.getTitle());
        i.setDescription(dto.getDescription());
        i.setIsUrgent(dto.getIsUrgent());
        i.setType(dto.getType());
        User reporter = userService.findById(dto.getReporterId()).orElseThrow(NoSuchElementException::new);
        i.setReporter(reporter);
        if (dto.getAssigneeId() != null) {
            User a = userService.findById(dto.getAssigneeId()).orElseThrow(NoSuchElementException::new);
            i.setAssignee(a);
        }
        if (dto.getTagNames() != null) {
            List<Tag> tags = new ArrayList<>();
            for (String n : dto.getTagNames()) {
                Tag t = tagService.findByName(n).orElseGet(() -> tagService.save(new Tag(null, n, null, null)));
                tags.add(t);
            }
            i.setTags(tags);
        }
        if (image != null && !image.isEmpty()) {
            i.setImage(image.getBytes());
        }
        return i;
    }
}
