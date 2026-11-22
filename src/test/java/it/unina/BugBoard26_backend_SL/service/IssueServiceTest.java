package it.unina.BugBoard26_backend_SL.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import it.unina.BugBoard26_backend_SL.dto.IssueCreateDto;
import it.unina.BugBoard26_backend_SL.dto.IssueDto;
import it.unina.BugBoard26_backend_SL.dto.IssueUpdateDto;
import it.unina.BugBoard26_backend_SL.entity.Issue;
import it.unina.BugBoard26_backend_SL.entity.Tag;
import it.unina.BugBoard26_backend_SL.entity.User;
import it.unina.BugBoard26_backend_SL.entity.enums.IssueType;
import it.unina.BugBoard26_backend_SL.entity.enums.Role;
import it.unina.BugBoard26_backend_SL.entity.state.ClosedState;
import it.unina.BugBoard26_backend_SL.entity.state.InProgressState;
import it.unina.BugBoard26_backend_SL.entity.state.IssueState;
import it.unina.BugBoard26_backend_SL.entity.state.TodoState;
import it.unina.BugBoard26_backend_SL.repository.CommentRepository;
import it.unina.BugBoard26_backend_SL.repository.IssueRepository;
import it.unina.BugBoard26_backend_SL.repository.TagRepository;
import it.unina.BugBoard26_backend_SL.repository.UserRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class IssueServiceTest {

  @Mock private IssueRepository issueRepository;

  @Mock private TagRepository tagRepository;

  @Mock private UserRepository userRepository;

  @Mock private CommentRepository commentRepository;

  @InjectMocks private IssueService issueService;

  private User standardUser;
  private User adminUser;
  private User assigneeUser;

  @BeforeEach
  void setUp() {
    standardUser =
        User.builder()
            .id(1L)
            .email("mario@unina.it")
            .firstName("Mario")
            .lastName("Rossi")
            .role(Role.USER)
            .build();

    adminUser =
        User.builder()
            .id(2L)
            .email("admin@unina.it")
            .firstName("Anna")
            .lastName("Bianchi")
            .role(Role.ADMIN)
            .build();

    assigneeUser =
        User.builder()
            .id(3L)
            .email("dev@unina.it")
            .firstName("Luca")
            .lastName("Verdi")
            .role(Role.USER)
            .build();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void mockAuthenticatedUser(String email, String roleAuthority) {
    Authentication auth =
        new UsernamePasswordAuthenticationToken(
            email, null, List.of(new SimpleGrantedAuthority(roleAuthority)));
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @Nested
  class CreateIssueTests {

    @Test
    void testCreateIssue_TC1_UserValidWithoutAssigneeTagsOrImage() {
      mockAuthenticatedUser(standardUser.getEmail(), "ROLE_USER");
      when(userRepository.findByEmail(standardUser.getEmail()))
          .thenReturn(Optional.of(standardUser));
      when(issueRepository.save(any(Issue.class)))
          .thenAnswer(inv -> {
            Issue toSave = inv.getArgument(0);
            toSave.setId(10L);
            return toSave;
          });

      IssueCreateDto dto =
          new IssueCreateDto("  Bug login  ", "Errore 500", false, IssueType.BUG, null, null);

      IssueDto result = issueService.createIssue(dto, null);

      assertNotNull(result);
      assertEquals(10L, result.getId());
      assertEquals("Bug login", result.getTitle());
      assertEquals("Errore 500", result.getDescription());
      assertFalse(result.getIsUrgent());
      assertEquals(IssueType.BUG, result.getType());
      assertEquals("TODO", result.getState());
      assertEquals(standardUser.getId(), result.getReporterId());
      assertEquals(standardUser.getEmail(), result.getReporterEmail());
      assertNull(result.getAssigneeId());
      assertNull(result.getTags());
      assertFalse(result.getHasImage());
      assertNull(result.getImageBase64());

      verify(userRepository, never()).findById(any());
      verify(tagRepository, never()).findByName(any());
      verify(issueRepository, times(1)).save(any(Issue.class));
    }

    @Test
    void testCreateIssue_TC2_AdminValidWithAssigneeExistingAndNewTagsAndImage() {
      mockAuthenticatedUser(adminUser.getEmail(), "ROLE_ADMIN");
      when(userRepository.findByEmail(adminUser.getEmail())).thenReturn(Optional.of(adminUser));
      when(userRepository.findById(3L)).thenReturn(Optional.of(assigneeUser));

      Tag existingTag = new Tag(100L, "backend", "#1e3a8a", null);
      when(tagRepository.findByName("backend")).thenReturn(Optional.of(existingTag));
      when(tagRepository.findByName("nuovo-tag")).thenReturn(Optional.empty());
      when(tagRepository.save(any(Tag.class)))
          .thenAnswer(inv -> {
            Tag t = inv.getArgument(0);
            t.setId(101L);
            return t;
          });

      when(issueRepository.save(any(Issue.class)))
          .thenAnswer(inv -> {
            Issue toSave = inv.getArgument(0);
            toSave.setId(11L);
            return toSave;
          });

      byte[] imageBytes = new byte[] {1, 2, 3, 4};
      IssueCreateDto dto =
          new IssueCreateDto(
              "Crash dashboard",
              "Dettaglio crash",
              true,
              IssueType.FEATURE,
              3L,
              List.of("backend", "nuovo-tag"));

      IssueDto result = issueService.createIssue(dto, imageBytes);

      assertNotNull(result);
      assertEquals(11L, result.getId());
      assertTrue(result.getIsUrgent());
      assertEquals(IssueType.FEATURE, result.getType());
      assertEquals(adminUser.getId(), result.getReporterId());
      assertEquals(assigneeUser.getId(), result.getAssigneeId());
      assertEquals(assigneeUser.getEmail(), result.getAssigneeEmail());
      assertEquals(List.of("backend", "nuovo-tag"), result.getTags());
      assertTrue(result.getHasImage());
      assertEquals(Base64.getEncoder().encodeToString(imageBytes), result.getImageBase64());

      verify(tagRepository, times(1)).save(any(Tag.class));
      verify(issueRepository, times(1)).save(any(Issue.class));
    }

    @Test
    void testCreateIssue_TC3_ValidWithEmptyTagsListEmptyImageAndNullUrgent() {
      mockAuthenticatedUser(standardUser.getEmail(), "ROLE_USER");
      when(userRepository.findByEmail(standardUser.getEmail()))
          .thenReturn(Optional.of(standardUser));
      when(issueRepository.save(any(Issue.class)))
          .thenAnswer(inv -> {
            Issue toSave = inv.getArgument(0);
            toSave.setId(12L);
            return toSave;
          });

      IssueCreateDto dto =
          new IssueCreateDto("Domanda API", null, null, IssueType.QUESTION, null, List.of());

      IssueDto result = issueService.createIssue(dto, new byte[0]);

      assertNotNull(result);
      assertFalse(result.getIsUrgent());
      assertTrue(result.getTags().isEmpty());
      assertFalse(result.getHasImage());
      assertNull(result.getImageBase64());
    }

    @Test
    void testCreateIssue_TC4_NullDto_ThrowsIllegalArgumentException() {
      assertThrows(IllegalArgumentException.class, () -> issueService.createIssue(null, null));

      verifyNoInteractions(userRepository, tagRepository, issueRepository);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t\n"})
    void testCreateIssue_TC5_InvalidTitle_ThrowsIllegalArgumentException(String invalidTitle) {
      IssueCreateDto dto =
          new IssueCreateDto(invalidTitle, "Descrizione", false, IssueType.BUG, null, null);

      assertThrows(IllegalArgumentException.class, () -> issueService.createIssue(dto, null));

      verifyNoInteractions(userRepository, tagRepository, issueRepository);
    }

    @Test
    void testCreateIssue_TC6_NullType_ThrowsIllegalArgumentException() {
      IssueCreateDto dto =
          new IssueCreateDto("Titolo valido", "Descrizione", false, null, null, null);

      assertThrows(IllegalArgumentException.class, () -> issueService.createIssue(dto, null));

      verifyNoInteractions(userRepository, tagRepository, issueRepository);
    }

    @Test
    void testCreateIssue_TC7_UnauthenticatedUser_ThrowsNoSuchElementException() {
      SecurityContextHolder.clearContext();
      IssueCreateDto dto =
          new IssueCreateDto("Titolo valido", "Descrizione", false, IssueType.BUG, null, null);

      assertThrows(NoSuchElementException.class, () -> issueService.createIssue(dto, null));

      Authentication authWithNullName = mock(Authentication.class);
      when(authWithNullName.getName()).thenReturn(null);
      SecurityContextHolder.getContext().setAuthentication(authWithNullName);

      assertThrows(NoSuchElementException.class, () -> issueService.createIssue(dto, null));

      verifyNoInteractions(userRepository, tagRepository, issueRepository);
    }

    @Test
    void testCreateIssue_TC8_ReporterNotFoundInDb_ThrowsNoSuchElementException() {
      mockAuthenticatedUser("fantasma@unina.it", "ROLE_USER");
      when(userRepository.findByEmail("fantasma@unina.it")).thenReturn(Optional.empty());

      IssueCreateDto dto =
          new IssueCreateDto("Titolo valido", "Descrizione", false, IssueType.BUG, null, null);

      assertThrows(NoSuchElementException.class, () -> issueService.createIssue(dto, null));

      verify(issueRepository, never()).save(any());
    }

    @Test
    void testCreateIssue_TC9_NonAdminSettingAssignee_ThrowsAccessDeniedException() {
      mockAuthenticatedUser(standardUser.getEmail(), "ROLE_USER");
      when(userRepository.findByEmail(standardUser.getEmail()))
          .thenReturn(Optional.of(standardUser));

      IssueCreateDto dto =
          new IssueCreateDto("Titolo valido", "Descrizione", false, IssueType.BUG, 3L, null);

      assertThrows(AccessDeniedException.class, () -> issueService.createIssue(dto, null));

      verify(userRepository, never()).findById(any());
      verify(issueRepository, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L})
    void testCreateIssue_TC10_AdminWithNonPositiveAssigneeId_ThrowsIllegalArgumentException(
        Long invalidAssigneeId) {
      mockAuthenticatedUser(adminUser.getEmail(), "ROLE_ADMIN");
      when(userRepository.findByEmail(adminUser.getEmail())).thenReturn(Optional.of(adminUser));

      IssueCreateDto dto =
          new IssueCreateDto(
              "Titolo valido", "Descrizione", false, IssueType.BUG, invalidAssigneeId, null);

      assertThrows(IllegalArgumentException.class, () -> issueService.createIssue(dto, null));

      verify(userRepository, never()).findById(any());
      verify(issueRepository, never()).save(any());
    }

    @Test
    void testCreateIssue_TC11_AdminWithAssigneeNotFoundInDb_ThrowsNoSuchElementException() {
      mockAuthenticatedUser(adminUser.getEmail(), "ROLE_ADMIN");
      when(userRepository.findByEmail(adminUser.getEmail())).thenReturn(Optional.of(adminUser));
      when(userRepository.findById(999L)).thenReturn(Optional.empty());

      IssueCreateDto dto =
          new IssueCreateDto("Titolo valido", "Descrizione", false, IssueType.BUG, 999L, null);

      assertThrows(NoSuchElementException.class, () -> issueService.createIssue(dto, null));

      verify(issueRepository, never()).save(any());
    }
  }

  @Nested
  class UpdateIssueTests {

    private Issue buildExistingIssue(Long id, IssueState state, User assignee, byte[] image) {
      return Issue.builder()
          .id(id)
          .title("Vecchio titolo")
          .description("Vecchia descrizione")
          .isUrgent(false)
          .type(IssueType.BUG)
          .state(state)
          .reporter(standardUser)
          .assignee(assignee)
          .tags(new ArrayList<>())
          .image(image)
          .build();
    }

    private IssueUpdateDto validDto() {
      return new IssueUpdateDto(
          "Titolo valido", "Descrizione", false, IssueType.BUG, null, null, false);
    }

    private void stubSaveReturnsArgument() {
      when(issueRepository.save(any(Issue.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // ---------------- CLASSI VALIDE ----------------

    @Test
    void testUpdateIssue_TC1_AdminValidFullUpdateKeepsAssigneeAndState() {
      mockAuthenticatedUser(adminUser.getEmail(), "ROLE_ADMIN");
      Issue existing = buildExistingIssue(5L, new InProgressState(), assigneeUser, null);
      when(issueRepository.findById(5L)).thenReturn(Optional.of(existing));
      stubSaveReturnsArgument();

      IssueUpdateDto dto =
          new IssueUpdateDto(
              "  Nuovo titolo  ", "Nuova descrizione", true, IssueType.FEATURE, null, null, false);

      IssueDto result = issueService.updateIssue(5L, dto, null);

      assertEquals("Nuovo titolo", result.getTitle());
      assertEquals("Nuova descrizione", result.getDescription());
      assertTrue(result.getIsUrgent());
      assertEquals(IssueType.FEATURE, result.getType());
      assertEquals("IN_PROGRESS", result.getState());
      assertEquals(assigneeUser.getId(), result.getAssigneeId());
      assertFalse(result.getHasImage());
      verify(userRepository, never()).findByEmail(any());
      verify(userRepository, never()).findById(any());
      verify(issueRepository, times(1)).save(existing);
    }

    @Test
    void testUpdateIssue_TC2_AssignedUserUpdatesOwnIssueWithNullDescriptionAndUrgentFalse() {
      mockAuthenticatedUser(standardUser.getEmail(), "ROLE_USER");
      when(userRepository.findByEmail(standardUser.getEmail()))
          .thenReturn(Optional.of(standardUser));
      Issue existing = buildExistingIssue(6L, new InProgressState(), standardUser, null);
      when(issueRepository.findById(6L)).thenReturn(Optional.of(existing));
      stubSaveReturnsArgument();

      byte[] newImage = new byte[] {9, 9, 9};
      // assigneeId uguale a quello corrente -> nessuna riassegnazione; isUrgent null -> false
      IssueUpdateDto dto =
          new IssueUpdateDto(
              "Titolo", null, null, IssueType.QUESTION, standardUser.getId(), List.of(), null);

      IssueDto result = issueService.updateIssue(6L, dto, newImage);

      assertNull(result.getDescription());
      assertFalse(result.getIsUrgent());
      assertEquals(IssueType.QUESTION, result.getType());
      assertEquals(standardUser.getId(), result.getAssigneeId());
      assertTrue(result.getTags().isEmpty());
      assertTrue(result.getHasImage());
      assertEquals(Base64.getEncoder().encodeToString(newImage), result.getImageBase64());
      verify(userRepository, never()).findById(any());
    }

    @Test
    void testUpdateIssue_TC3_AdminFirstAssignmentMovesStateTodoToInProgress() {
      mockAuthenticatedUser(adminUser.getEmail(), "ROLE_ADMIN");
      Issue existing = buildExistingIssue(7L, new TodoState(), null, null);
      when(issueRepository.findById(7L)).thenReturn(Optional.of(existing));
      when(userRepository.findById(3L)).thenReturn(Optional.of(assigneeUser));
      stubSaveReturnsArgument();

      IssueUpdateDto dto =
          new IssueUpdateDto("Titolo", "Descr", false, IssueType.BUG, 3L, null, false);

      IssueDto result = issueService.updateIssue(7L, dto, null);

      assertEquals(assigneeUser.getId(), result.getAssigneeId());
      assertEquals("IN_PROGRESS", result.getState());
    }

    @Test
    void testUpdateIssue_TC4_AdminReassignsIssueKeepsState() {
      mockAuthenticatedUser(adminUser.getEmail(), "ROLE_ADMIN");
      Issue existing = buildExistingIssue(8L, new InProgressState(), standardUser, null);
      when(issueRepository.findById(8L)).thenReturn(Optional.of(existing));
      when(userRepository.findById(3L)).thenReturn(Optional.of(assigneeUser));
      stubSaveReturnsArgument();

      IssueUpdateDto dto =
          new IssueUpdateDto("Titolo", "Descr", false, IssueType.BUG, 3L, null, false);

      IssueDto result = issueService.updateIssue(8L, dto, null);

      assertEquals(assigneeUser.getId(), result.getAssigneeId());
      assertEquals("IN_PROGRESS", result.getState());
    }

    @Test
    void testUpdateIssue_TC5_TagsAreNormalizedDeduplicatedAndNewOnesCreated() {
      mockAuthenticatedUser(adminUser.getEmail(), "ROLE_ADMIN");
      Issue existing = buildExistingIssue(9L, new InProgressState(), assigneeUser, null);
      when(issueRepository.findById(9L)).thenReturn(Optional.of(existing));
      Tag backendTag = new Tag(100L, "backend", "#1e3a8a", null);
      when(tagRepository.findByName("backend")).thenReturn(Optional.of(backendTag));
      when(tagRepository.findByName("nuovo")).thenReturn(Optional.empty());
      when(tagRepository.save(any(Tag.class))).thenAnswer(inv -> inv.getArgument(0));
      stubSaveReturnsArgument();

      // "#Backend" e " backend " sono lo stesso tag; null, blank e "#" vengono scartati
      IssueUpdateDto dto =
          new IssueUpdateDto(
              "Titolo",
              "Descr",
              false,
              IssueType.BUG,
              null,
              Arrays.asList("#Backend", " backend ", null, "   ", "#", "nuovo"),
              false);

      IssueDto result = issueService.updateIssue(9L, dto, null);

      assertEquals(List.of("backend", "nuovo"), result.getTags());
      verify(tagRepository, times(1)).findByName("backend");
      verify(tagRepository, times(1)).save(any(Tag.class));
    }

    @Test
    void testUpdateIssue_TC6_RemoveImageTrueWithoutNewImageRemovesIt() {
      mockAuthenticatedUser(adminUser.getEmail(), "ROLE_ADMIN");
      Issue existing =
          buildExistingIssue(10L, new InProgressState(), assigneeUser, new byte[] {1, 2});
      when(issueRepository.findById(10L)).thenReturn(Optional.of(existing));
      stubSaveReturnsArgument();

      IssueUpdateDto dto =
          new IssueUpdateDto("Titolo", "Descr", false, IssueType.BUG, null, null, true);

      IssueDto result = issueService.updateIssue(10L, dto, null);

      assertFalse(result.getHasImage());
      assertNull(result.getImageBase64());
    }

    @Test
    void testUpdateIssue_TC7_EmptyImageArrayWithRemoveImageTrueRemovesIt() {
      mockAuthenticatedUser(adminUser.getEmail(), "ROLE_ADMIN");
      Issue existing =
          buildExistingIssue(11L, new InProgressState(), assigneeUser, new byte[] {1, 2});
      when(issueRepository.findById(11L)).thenReturn(Optional.of(existing));
      stubSaveReturnsArgument();

      IssueUpdateDto dto =
          new IssueUpdateDto("Titolo", "Descr", false, IssueType.BUG, null, null, true);

      IssueDto result = issueService.updateIssue(11L, dto, new byte[0]);

      assertFalse(result.getHasImage());
    }

    @Test
    void testUpdateIssue_TC8_NoNewImageAndRemoveImageNullKeepsExistingImage() {
      mockAuthenticatedUser(adminUser.getEmail(), "ROLE_ADMIN");
      byte[] oldImage = new byte[] {1, 2};
      Issue existing = buildExistingIssue(12L, new InProgressState(), assigneeUser, oldImage);
      when(issueRepository.findById(12L)).thenReturn(Optional.of(existing));
      stubSaveReturnsArgument();

      IssueUpdateDto dto =
          new IssueUpdateDto("Titolo", "Descr", false, IssueType.BUG, null, null, null);

      IssueDto result = issueService.updateIssue(12L, dto, null);

      assertTrue(result.getHasImage());
      assertEquals(Base64.getEncoder().encodeToString(oldImage), result.getImageBase64());
    }

    @Test
    void testUpdateIssue_TC9_IssueWithNullStateIsStillUpdatable() {
      mockAuthenticatedUser(adminUser.getEmail(), "ROLE_ADMIN");
      Issue existing = buildExistingIssue(13L, null, null, null);
      when(issueRepository.findById(13L)).thenReturn(Optional.of(existing));
      stubSaveReturnsArgument();

      IssueDto result = issueService.updateIssue(13L, validDto(), null);

      assertEquals("Titolo valido", result.getTitle());
      assertNull(result.getState());
    }

    // ---------------- CLASSI NON VALIDE: parametri ----------------

    @ParameterizedTest
    @NullSource
    @ValueSource(longs = {0L, -1L})
    void testUpdateIssue_TC10_InvalidIssueId_ThrowsIllegalArgumentException(Long invalidId) {
      assertThrows(
          IllegalArgumentException.class,
          () -> issueService.updateIssue(invalidId, validDto(), null));

      verifyNoInteractions(userRepository, tagRepository, issueRepository);
    }

    @Test
    void testUpdateIssue_TC11_NullDto_ThrowsIllegalArgumentException() {
      assertThrows(IllegalArgumentException.class, () -> issueService.updateIssue(5L, null, null));

      verifyNoInteractions(userRepository, tagRepository, issueRepository);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t\n"})
    void testUpdateIssue_TC12_InvalidTitle_ThrowsIllegalArgumentException(String invalidTitle) {
      IssueUpdateDto dto =
          new IssueUpdateDto(invalidTitle, "Descr", false, IssueType.BUG, null, null, false);

      assertThrows(IllegalArgumentException.class, () -> issueService.updateIssue(5L, dto, null));

      verifyNoInteractions(userRepository, tagRepository, issueRepository);
    }

    @Test
    void testUpdateIssue_TC13_NullType_ThrowsIllegalArgumentException() {
      IssueUpdateDto dto = new IssueUpdateDto("Titolo", "Descr", false, null, null, null, false);

      assertThrows(IllegalArgumentException.class, () -> issueService.updateIssue(5L, dto, null));

      verifyNoInteractions(userRepository, tagRepository, issueRepository);
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L})
    void testUpdateIssue_TC14_NonPositiveAssigneeId_ThrowsIllegalArgumentException(
        Long invalidAssigneeId) {
      IssueUpdateDto dto =
          new IssueUpdateDto(
              "Titolo", "Descr", false, IssueType.BUG, invalidAssigneeId, null, false);

      assertThrows(IllegalArgumentException.class, () -> issueService.updateIssue(5L, dto, null));

      verifyNoInteractions(userRepository, tagRepository, issueRepository);
    }

    // ---------------- CLASSI NON VALIDE: stato / contesto ----------------

    @Test
    void testUpdateIssue_TC15_IssueNotFound_ThrowsNoSuchElementException() {
      when(issueRepository.findById(99L)).thenReturn(Optional.empty());

      assertThrows(
          NoSuchElementException.class, () -> issueService.updateIssue(99L, validDto(), null));

      verify(issueRepository, never()).save(any());
    }

    @Test
    void testUpdateIssue_TC16_ClosedIssue_ThrowsIllegalStateException() {
      mockAuthenticatedUser(adminUser.getEmail(), "ROLE_ADMIN");
      Issue closed = buildExistingIssue(14L, new ClosedState(), assigneeUser, null);
      when(issueRepository.findById(14L)).thenReturn(Optional.of(closed));

      assertThrows(
          IllegalStateException.class, () -> issueService.updateIssue(14L, validDto(), null));

      verify(issueRepository, never()).save(any());
    }

    @Test
    void testUpdateIssue_TC17_UnauthenticatedUser_ThrowsAccessDeniedException() {
      Issue existing = buildExistingIssue(15L, new InProgressState(), assigneeUser, null);
      when(issueRepository.findById(15L)).thenReturn(Optional.of(existing));

      SecurityContextHolder.clearContext();
      assertThrows(
          AccessDeniedException.class, () -> issueService.updateIssue(15L, validDto(), null));

      Authentication authWithNullName = mock(Authentication.class);
      when(authWithNullName.getName()).thenReturn(null);
      SecurityContextHolder.getContext().setAuthentication(authWithNullName);
      assertThrows(
          AccessDeniedException.class, () -> issueService.updateIssue(15L, validDto(), null));

      verify(issueRepository, never()).save(any());
    }

    @Test
    void testUpdateIssue_TC18_NonAdminNotFoundInDb_ThrowsAccessDeniedException() {
      mockAuthenticatedUser("fantasma@unina.it", "ROLE_USER");
      Issue existing = buildExistingIssue(16L, new InProgressState(), assigneeUser, null);
      when(issueRepository.findById(16L)).thenReturn(Optional.of(existing));
      when(userRepository.findByEmail("fantasma@unina.it")).thenReturn(Optional.empty());

      assertThrows(
          AccessDeniedException.class, () -> issueService.updateIssue(16L, validDto(), null));

      verify(issueRepository, never()).save(any());
    }

    @Test
    void testUpdateIssue_TC19_NonAdminOnUnassignedIssue_ThrowsAccessDeniedException() {
      mockAuthenticatedUser(standardUser.getEmail(), "ROLE_USER");
      Issue existing = buildExistingIssue(17L, new TodoState(), null, null);
      when(issueRepository.findById(17L)).thenReturn(Optional.of(existing));
      when(userRepository.findByEmail(standardUser.getEmail()))
          .thenReturn(Optional.of(standardUser));

      assertThrows(
          AccessDeniedException.class, () -> issueService.updateIssue(17L, validDto(), null));

      verify(issueRepository, never()).save(any());
    }

    @Test
    void testUpdateIssue_TC20_NonAdminNotTheAssignee_ThrowsAccessDeniedException() {
      mockAuthenticatedUser(standardUser.getEmail(), "ROLE_USER");
      Issue existing = buildExistingIssue(18L, new InProgressState(), assigneeUser, null);
      when(issueRepository.findById(18L)).thenReturn(Optional.of(existing));
      when(userRepository.findByEmail(standardUser.getEmail()))
          .thenReturn(Optional.of(standardUser));

      assertThrows(
          AccessDeniedException.class, () -> issueService.updateIssue(18L, validDto(), null));

      verify(issueRepository, never()).save(any());
    }

    @Test
    void testUpdateIssue_TC21_NonAdminAssigneeTriesToReassign_ThrowsAccessDeniedException() {
      mockAuthenticatedUser(standardUser.getEmail(), "ROLE_USER");
      Issue existing = buildExistingIssue(19L, new InProgressState(), standardUser, null);
      when(issueRepository.findById(19L)).thenReturn(Optional.of(existing));
      when(userRepository.findByEmail(standardUser.getEmail()))
          .thenReturn(Optional.of(standardUser));

      IssueUpdateDto dto =
          new IssueUpdateDto("Titolo", "Descr", false, IssueType.BUG, 3L, null, false);

      assertThrows(AccessDeniedException.class, () -> issueService.updateIssue(19L, dto, null));

      verify(userRepository, never()).findById(any());
      verify(issueRepository, never()).save(any());
    }

    @Test
    void testUpdateIssue_TC22_AdminAssigneeNotFoundInDb_ThrowsNoSuchElementException() {
      mockAuthenticatedUser(adminUser.getEmail(), "ROLE_ADMIN");
      Issue existing = buildExistingIssue(20L, new InProgressState(), standardUser, null);
      when(issueRepository.findById(20L)).thenReturn(Optional.of(existing));
      when(userRepository.findById(999L)).thenReturn(Optional.empty());

      IssueUpdateDto dto =
          new IssueUpdateDto("Titolo", "Descr", false, IssueType.BUG, 999L, null, false);

      assertThrows(NoSuchElementException.class, () -> issueService.updateIssue(20L, dto, null));

      verify(issueRepository, never()).save(any());
    }
  }
}
