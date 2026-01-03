package it.unina.BugBoard26_backend_SL.service;

import it.unina.BugBoard26_backend_SL.dto.CommentDto;
import it.unina.BugBoard26_backend_SL.dto.CreateCommentDto;
import it.unina.BugBoard26_backend_SL.entity.Comment;
import it.unina.BugBoard26_backend_SL.entity.Issue;
import it.unina.BugBoard26_backend_SL.entity.User;
import it.unina.BugBoard26_backend_SL.repository.CommentRepository;
import it.unina.BugBoard26_backend_SL.repository.IssueRepository;
import it.unina.BugBoard26_backend_SL.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CommentDto> findByIssueId(Long issueId) {
        // ensure issue exists
        Issue issue = issueRepository.findById(issueId).orElseThrow(NoSuchElementException::new);
        List<Comment> comments = commentRepository.findByIssueIdOrderByCreationDateAsc(issueId);
        return comments.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public CommentDto create(Long issueId, CreateCommentDto dto) {
        Issue issue = issueRepository.findById(issueId).orElseThrow(NoSuchElementException::new);

        // get current authenticated user (assumes username is the email)
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User author = userRepository.findByEmail(username).orElseThrow(NoSuchElementException::new);

        Comment c = Comment.builder()
                .text(dto.getText())
                .issue(issue)
                .author(author)
                .build();

        Comment saved = commentRepository.save(c);
        return toDto(saved);
    }

    // Mapper
    private CommentDto toDto(Comment c) {
        CommentDto d = new CommentDto();
        d.setId(c.getId());
        d.setText(c.getText());
        if (c.getAuthor() != null) {
            d.setAuthorName(c.getAuthor().getFirstName());
            d.setAuthorLastName(c.getAuthor().getLastName());
        }
        d.setCreationDate(c.getCreationDate());
        return d;
    }
}