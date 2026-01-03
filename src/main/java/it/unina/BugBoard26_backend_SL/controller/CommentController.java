package it.unina.BugBoard26_backend_SL.controller;

import it.unina.BugBoard26_backend_SL.dto.CommentDto;
import it.unina.BugBoard26_backend_SL.dto.CreateCommentDto;
import it.unina.BugBoard26_backend_SL.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/issues/{issueId}/comments")
@Validated
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping
    public ResponseEntity<List<CommentDto>> list(@PathVariable Long issueId) {
        List<CommentDto> dtos = commentService.findByIssueId(issueId);
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<CommentDto> create(@PathVariable Long issueId,
                                             @Valid @RequestBody CreateCommentDto dto) {
        CommentDto saved = commentService.create(issueId, dto);
        return ResponseEntity.created(URI.create("/issues/" + issueId + "/comments/" + saved.getId())).body(saved);
    }
}