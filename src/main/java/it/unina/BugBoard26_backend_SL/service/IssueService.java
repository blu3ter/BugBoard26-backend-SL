package it.unina.BugBoard26_backend_SL.service;

import lombok.extern.slf4j.Slf4j;
import it.unina.BugBoard26_backend_SL.dto.IssueCreateDto;
import it.unina.BugBoard26_backend_SL.dto.IssueDto;
import it.unina.BugBoard26_backend_SL.dto.IssueUpdateDto;
import it.unina.BugBoard26_backend_SL.dto.IssueAssignDto;
import it.unina.BugBoard26_backend_SL.entity.Issue;
import it.unina.BugBoard26_backend_SL.entity.Tag;
import it.unina.BugBoard26_backend_SL.entity.User;
import it.unina.BugBoard26_backend_SL.entity.enums.IssueType;
import it.unina.BugBoard26_backend_SL.entity.state.IssueState;
import it.unina.BugBoard26_backend_SL.repository.IssueRepository;
import it.unina.BugBoard26_backend_SL.repository.TagRepository;
import it.unina.BugBoard26_backend_SL.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service responsabile della logica di business relativa alle Issue.
 * Centralizza mapping DTO <-> Entity, orchestrazione Tag/User e transazioni.
 */
@Service
@Slf4j
public class IssueService {

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserRepository userRepository;

    // --- Query oriented (Entity) ---

    public List<Issue> findAll() {
        return issueRepository.findAll();
    }

    public Optional<Issue> findById(Long id) {
        return issueRepository.findById(id);
    }

    public List<Issue> findByType(IssueType type) {
        return issueRepository.findByType(type);
    }

    public List<Issue> findByState(IssueState state) {
        return issueRepository.findByState(state);
    }

    public List<Issue> findByIsUrgent(Boolean isUrgent) {
        return issueRepository.findByIsUrgent(isUrgent);
    }

    public List<Issue> findByTagName(String tagName) {
        return issueRepository.findByTags_Name(tagName);
    }

    // --- DTO-oriented API used by Controller ---

    public List<IssueDto> findAllDto() {
        return findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public IssueDto findByIdDto(Long id) {
        Issue i = findById(id).orElseThrow(NoSuchElementException::new);
        return toDto(i);
    }

    public List<IssueDto> findByTagNameDto(String tagName) {
        return findByTagName(tagName).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public IssueDto createIssue(IssueCreateDto dto, byte[] imageBytes) {
        log.debug("Inizio creazione issue nel service per reporterId: {}", dto.getReporterId());
        Issue i = new Issue();
        i.setTitle(dto.getTitle());
        i.setDescription(dto.getDescription());
        i.setIsUrgent(dto.getIsUrgent());
        i.setType(dto.getType());

        // reporter (obbligatorio)
        User reporter = userRepository.findById(dto.getReporterId())
                .orElseThrow(() -> {
                    log.error("Reporter non trovato con ID: {}", dto.getReporterId());
                    return new NoSuchElementException("Reporter non trovato");
                });
        i.setReporter(reporter);

        // assignee opzionale
        if (dto.getAssigneeId() != null) {
            User a = userRepository.findById(dto.getAssigneeId()).orElseThrow(NoSuchElementException::new);
            i.setAssignee(a);
        }

        // tags: crea se non esistono
        if (dto.getTagNames() != null) {
            List<Tag> tags = new ArrayList<>();
            for (String name : dto.getTagNames()) {
                Tag t = tagRepository.findByName(name)
                        .orElseGet(() -> tagRepository.save(new Tag(null, name, null, null)));
                tags.add(t);
            }
            i.setTags(tags);
        }

        if (imageBytes != null) {
            i.setImage(imageBytes);
        }

        Issue saved = issueRepository.save(i);
        return toDto(saved);
    }

    @Transactional
    public IssueDto updateIssue(Long id, IssueUpdateDto dto, byte[] imageBytes) {
        Issue existing = issueRepository.findById(id).orElseThrow(NoSuchElementException::new);

        existing.setTitle(dto.getTitle());
        existing.setDescription(dto.getDescription());
        existing.setIsUrgent(dto.getIsUrgent());
        existing.setType(dto.getType());

        if (dto.getAssigneeId() != null) {
            User a = userRepository.findById(dto.getAssigneeId()).orElseThrow(NoSuchElementException::new);
            existing.setAssignee(a);
        } else {
            existing.setAssignee(null);
        }

        if (dto.getTagNames() != null) {
            List<Tag> tags = new ArrayList<>();
            for (String n : dto.getTagNames()) {
                Tag t = tagRepository.findByName(n).orElseGet(() -> tagRepository.save(new Tag(null, n, null, null)));
                tags.add(t);
            }
            existing.setTags(tags);
        } else {
            existing.setTags(null);
        }

        if (imageBytes != null) {
            existing.setImage(imageBytes);
        }

        Issue saved = issueRepository.save(existing);
        return toDto(saved);
    }


    @Transactional
    public void deleteById(Long id) {
        issueRepository.deleteById(id);
    }


    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public IssueDto assignTo(Long issueId, IssueAssignDto dto) {
        Issue issue = issueRepository.findById(issueId).orElseThrow(NoSuchElementException::new);
        User user = userRepository.findById(dto.getAssigneeId()).orElseThrow(NoSuchElementException::new);
        issue.assignTo(user);
        Issue saved = issueRepository.save(issue);
        return toDto(saved);
    }


    @Transactional
    public IssueDto closeIssue(Long issueId) {
        Issue issue = issueRepository.findById(issueId).orElseThrow(NoSuchElementException::new);
        issue.close();
        Issue saved = issueRepository.save(issue);
        return toDto(saved);
    }

    
    // --- Legacy save for Entity callers (kept for compatibility) ---
    @Transactional
    public Issue save(Issue issue) {
        // ensure tags exist or are saved
        if (issue.getTags() != null) {
            for (Tag t : issue.getTags()) {
                if (t.getId() == null) {
                    Optional<Tag> existing = tagRepository.findByName(t.getName());
                    if (existing.isPresent()) {
                        t.setId(existing.get().getId());
                    } else {
                        tagRepository.save(t);
                    }
                }
            }
        }

        // ensure reporter/assignee exist
        if (issue.getReporter() != null && issue.getReporter().getId() != null) {
            Optional<User> r = userRepository.findById(issue.getReporter().getId());
            r.ifPresent(issue::setReporter);
        }
        if (issue.getAssignee() != null && issue.getAssignee().getId() != null) {
            Optional<User> a = userRepository.findById(issue.getAssignee().getId());
            a.ifPresent(issue::setAssignee);
        }

        return issueRepository.save(issue);
    }

    // Mapper interno: Entity -> DTO
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
            dto.setReporterFullName(i.getReporter().getFullName());
        }
        if (i.getAssignee() != null) {
            dto.setAssigneeId(i.getAssignee().getId());
            dto.setAssigneeEmail(i.getAssignee().getEmail());
            dto.setAssigneeFullName(i.getAssignee().getFullName());
        }
        if (i.getTags() != null) {
            dto.setTags(i.getTags().stream().map(Tag::getName).collect(Collectors.toList()));
        }
        return dto;
    }
}
