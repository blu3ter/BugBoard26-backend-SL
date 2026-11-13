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
import it.unina.BugBoard26_backend_SL.repository.CommentRepository;
import it.unina.BugBoard26_backend_SL.repository.IssueRepository;
import it.unina.BugBoard26_backend_SL.repository.TagRepository;
import it.unina.BugBoard26_backend_SL.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
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

    @Autowired
    private CommentRepository commentRepository;

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
        return toDtoList(findAll());
    }

    public IssueDto findByIdDto(Long id) {
        Issue i = findById(id).orElseThrow(NoSuchElementException::new);
        return toDto(i, true);
    }

    public List<IssueDto> findByTagNameDto(String tagName) {
        return toDtoList(findByTagName(tagName));
    }

    /**
     * Restituisce le issue assegnate all'utente attualmente autenticato.
     * Usa SecurityContextHolder per recuperare l'email dell'utente corrente,
     * poi cerca l'entity User e infine le issue via repository.
     */
    public List<IssueDto> findByAssigneeCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new NoSuchElementException("Utente non autenticato");
        }
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new NoSuchElementException("Utente non trovato: " + email));
        return toDtoList(issueRepository.findByAssignee_Id(user.getId()));
    }

    @Transactional
    public IssueDto createIssue(IssueCreateDto dto, byte[] imageBytes) {
        log.debug("Inizio creazione issue nel service");
        Issue i = new Issue();
        i.setTitle(dto.getTitle());
        i.setDescription(dto.getDescription());
        i.setIsUrgent(dto.getIsUrgent());
        i.setType(dto.getType());

        // reporter: ricavato dall'utente autenticato (token JWT)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new NoSuchElementException("Utente non autenticato");
        }
        String email = auth.getName();
        User reporter = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Reporter non trovato per email: {}", email);
                    return new NoSuchElementException("Reporter non trovato");
                });
        i.setReporter(reporter);

        // assignee opzionale: consentito solo agli ADMIN
        if (dto.getAssigneeId() != null) {
            boolean isAdmin = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdmin) {
                throw new AccessDeniedException("Solo gli ADMIN possono impostare l'assignee durante la creazione");
            }
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
        return toDto(saved, true);
    }

    @Transactional
    public IssueDto updateIssue(Long id, IssueUpdateDto dto, byte[] imageBytes) {
        Issue existing = issueRepository.findById(id).orElseThrow(NoSuchElementException::new);

        if (existing.getState() != null && "CLOSED".equals(existing.getState().getStateName())) {
            throw new IllegalStateException("Impossibile modificare una issue chiusa.");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new AccessDeniedException("Utente non autenticato");
        }

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        if (!isAdmin) {
            User currentUser = userRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new AccessDeniedException("Utente non trovato"));
            if (existing.getAssignee() == null || !existing.getAssignee().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException(
                        "Solo gli admin o l'utente assegnatario possono modificare questa issue.");
            }
        }

        existing.setTitle(dto.getTitle().trim());
        existing.setDescription(dto.getDescription());
        existing.setIsUrgent(Boolean.TRUE.equals(dto.getIsUrgent()));
        existing.setType(dto.getType());

        if (dto.getAssigneeId() != null) {
            Long currentAssigneeId = existing.getAssignee() != null ? existing.getAssignee().getId() : null;
            if (!dto.getAssigneeId().equals(currentAssigneeId)) {
                if (!isAdmin) {
                    throw new AccessDeniedException("Solo gli ADMIN possono assegnare o riassegnare una issue");
                }
                User assignee = userRepository.findById(dto.getAssigneeId()).orElseThrow(NoSuchElementException::new);
                if (existing.getAssignee() == null) {
                    existing.assignTo(assignee);
                } else {
                    existing.setAssignee(assignee);
                }
            }
        }

        if (dto.getTagNames() != null) {
            List<Tag> tags = new ArrayList<>();
            Set<String> seenTags = new HashSet<>();
            for (String rawName : dto.getTagNames()) {
                if (rawName == null) continue;
                String cleanName = rawName.trim().toLowerCase().replaceAll("^#", "");
                if (cleanName.isEmpty() || !seenTags.add(cleanName)) continue;
                Tag t = tagRepository.findByName(cleanName)
                        .orElseGet(() -> tagRepository.save(new Tag(null, cleanName, null, null)));
                tags.add(t);
            }
            existing.setTags(tags);
        }

        if (imageBytes != null && imageBytes.length > 0) {
            existing.setImage(imageBytes);
        } else if (Boolean.TRUE.equals(dto.getRemoveImage())) {
            existing.setImage(null);
        }

        Issue saved = issueRepository.save(existing);
        return toDto(saved, true);
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
        return toDto(saved, true);
    }


    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public IssueDto closeIssue(Long issueId) {
        Issue issue = issueRepository.findById(issueId).orElseThrow(NoSuchElementException::new);
        issue.close();
        Issue saved = issueRepository.save(issue);
        return toDto(saved, true);
    }

    /**
     * Manda la issue in revisione (IN_PROGRESS → IN_REVIEW).
     * Può essere eseguito solo dall'assignee della issue o da un admin.
     */
    @Transactional
    public IssueDto sendToReview(Long issueId) {
        Issue issue = issueRepository.findById(issueId).orElseThrow(NoSuchElementException::new);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new NoSuchElementException("Utente non autenticato");
        }

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            // Verifica che l'utente autenticato sia l'assignee della issue
            String email = auth.getName();
            User currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new NoSuchElementException("Utente non trovato: " + email));

            if (issue.getAssignee() == null || !issue.getAssignee().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException(
                        "Solo l'assignee della issue o un admin può mandarla in revisione.");
            }
        }

        issue.sendToReview();
        Issue saved = issueRepository.save(issue);
        return toDto(saved, true);
    }


    /**
     * Rimanda la issue in lavorazione (IN_REVIEW → IN_PROGRESS).
     * Operazione riservata esclusivamente all'admin.
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public IssueDto sendBackToProgress(Long issueId) {
        Issue issue = issueRepository.findById(issueId).orElseThrow(NoSuchElementException::new);
        issue.sendBackToProgress();
        Issue saved = issueRepository.save(issue);
        return toDto(saved, true);
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

    // Mapper interno: Lista Entity -> Lista DTO (evita N+1 query per il conteggio commenti)
    private List<IssueDto> toDtoList(List<Issue> issues) {
        if (issues.isEmpty()) {
            return List.of();
        }
        List<Long> issueIds = issues.stream()
                .map(Issue::getId)
                .collect(Collectors.toList());
        Map<Long, Long> countsByIssueId = new HashMap<>();
        for (Object[] row : commentRepository.countByIssueIds(issueIds)) {
            countsByIssueId.put((Long) row[0], ((Number) row[1]).longValue());
        }
        return issues.stream()
                .map(issue -> toDto(issue, countsByIssueId.getOrDefault(issue.getId(), 0L), false))
                .collect(Collectors.toList());
    }

    // Mapper interno: Entity -> DTO
    private IssueDto toDto(Issue i, boolean includeImage) {
        long commentsCount = (i.getId() != null) ? commentRepository.countByIssueId(i.getId()) : 0L;
        return toDto(i, commentsCount, includeImage);
    }

    private IssueDto toDto(Issue i, long commentsCount, boolean includeImage) {
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
        dto.setCommentsCount(commentsCount);
        boolean hasImg = i.getImage() != null && i.getImage().length > 0;
        dto.setHasImage(hasImg);
        if (includeImage && hasImg) {
            dto.setImageBase64(Base64.getEncoder().encodeToString(i.getImage()));
        }
        return dto;
    }
}
