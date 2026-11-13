package it.unina.BugBoard26_backend_SL.service;

import it.unina.BugBoard26_backend_SL.dto.IssueDto;
import it.unina.BugBoard26_backend_SL.dto.IssueUpdateDto;
import it.unina.BugBoard26_backend_SL.entity.Issue;
import it.unina.BugBoard26_backend_SL.entity.Tag;
import it.unina.BugBoard26_backend_SL.entity.User;
import it.unina.BugBoard26_backend_SL.entity.enums.IssueType;
import it.unina.BugBoard26_backend_SL.entity.enums.Role;
import it.unina.BugBoard26_backend_SL.entity.state.ClosedState;
import it.unina.BugBoard26_backend_SL.entity.state.InProgressState;
import it.unina.BugBoard26_backend_SL.repository.CommentRepository;
import it.unina.BugBoard26_backend_SL.repository.IssueRepository;
import it.unina.BugBoard26_backend_SL.repository.TagRepository;
import it.unina.BugBoard26_backend_SL.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IssueServiceUpdateTest {

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private IssueService issueService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(String email, String roleAuthority) {
        var auth = new UsernamePasswordAuthenticationToken(
                email,
                "N/A",
                List.of(new SimpleGrantedAuthority(roleAuthority))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void updateIssue_rejectsClosedIssue_evenForAdmin() {
        authenticateAs("admin@bugboard.it", "ROLE_ADMIN");

        Issue closedIssue = Issue.builder()
                .id(1L)
                .title("Old title")
                .type(IssueType.BUG)
                .state(new ClosedState())
                .build();

        when(issueRepository.findById(1L)).thenReturn(Optional.of(closedIssue));

        IssueUpdateDto dto = new IssueUpdateDto("New title", "Desc", true, IssueType.BUG, null, List.of(), false);
        assertThrows(IllegalStateException.class, () -> issueService.updateIssue(1L, dto, null));
    }

    @Test
    void updateIssue_rejectsNonAssignedStandardUser() {
        authenticateAs("other@bugboard.it", "ROLE_USER");

        User assignee = User.builder().id(10L).email("assignee@bugboard.it").role(Role.USER).build();
        User other = User.builder().id(20L).email("other@bugboard.it").role(Role.USER).build();

        Issue issue = Issue.builder()
                .id(2L)
                .title("Bug")
                .type(IssueType.BUG)
                .state(new InProgressState())
                .assignee(assignee)
                .build();

        when(issueRepository.findById(2L)).thenReturn(Optional.of(issue));
        when(userRepository.findByEmail("other@bugboard.it")).thenReturn(Optional.of(other));

        IssueUpdateDto dto = new IssueUpdateDto("Edited", "Desc", false, IssueType.BUG, null, List.of(), false);
        assertThrows(AccessDeniedException.class, () -> issueService.updateIssue(2L, dto, null));
    }

    @Test
    void updateIssue_allowsAssignedUser_preservesAssignee_andUpdatesImageAndTags() {
        authenticateAs("assignee@bugboard.it", "ROLE_USER");

        User assignee = User.builder().id(10L).email("assignee@bugboard.it").firstName("Mario").lastName("Rossi").role(Role.USER).build();
        Issue issue = Issue.builder()
                .id(3L)
                .title("Old")
                .type(IssueType.BUG)
                .state(new InProgressState())
                .assignee(assignee)
                .build();

        when(issueRepository.findById(3L)).thenReturn(Optional.of(issue));
        when(userRepository.findByEmail("assignee@bugboard.it")).thenReturn(Optional.of(assignee));
        when(tagRepository.findByName("frontend")).thenReturn(Optional.of(new Tag(1L, "frontend", null, null)));
        when(issueRepository.save(any(Issue.class))).thenAnswer(inv -> inv.getArgument(0));
        when(commentRepository.countByIssueId(3L)).thenReturn(0L);

        byte[] newImg = new byte[]{1, 2, 3};
        IssueUpdateDto dto = new IssueUpdateDto("Updated title", "New desc", true, IssueType.FEATURE, null, List.of("#Frontend"), false);

        IssueDto result = issueService.updateIssue(3L, dto, newImg);

        assertEquals("Updated title", result.getTitle());
        assertEquals("New desc", result.getDescription());
        assertEquals(IssueType.FEATURE, result.getType());
        assertTrue(result.getIsUrgent());
        assertEquals(10L, result.getAssigneeId());
        assertEquals(List.of("frontend"), result.getTags());
        assertTrue(result.getHasImage());
        assertNotNull(result.getImageBase64());
    }

    @Test
    void updateIssue_allowsAdmin_toRemoveExistingImage() {
        authenticateAs("admin@bugboard.it", "ROLE_ADMIN");

        User assignee = User.builder().id(10L).email("assignee@bugboard.it").role(Role.USER).build();
        Issue issue = Issue.builder()
                .id(4L)
                .title("With image")
                .type(IssueType.BUG)
                .state(new InProgressState())
                .assignee(assignee)
                .image(new byte[]{9, 8, 7})
                .build();

        when(issueRepository.findById(4L)).thenReturn(Optional.of(issue));
        when(issueRepository.save(any(Issue.class))).thenAnswer(inv -> inv.getArgument(0));
        when(commentRepository.countByIssueId(4L)).thenReturn(0L);

        IssueUpdateDto dto = new IssueUpdateDto("No image now", "Desc", false, IssueType.BUG, null, List.of(), true);

        IssueDto result = issueService.updateIssue(4L, dto, null);

        assertFalse(result.getHasImage());
        assertNull(result.getImageBase64());
        assertEquals(10L, result.getAssigneeId());
    }
}

