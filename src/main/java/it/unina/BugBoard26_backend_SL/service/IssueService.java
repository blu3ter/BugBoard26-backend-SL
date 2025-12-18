package it.unina.BugBoard26_backend_SL.service;

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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class IssueService {

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserRepository userRepository;

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

    public void deleteById(Long id) {
        issueRepository.deleteById(id);
    }

    @Transactional
    public Issue assignTo(Long issueId, Long userId) {
        Issue issue = issueRepository.findById(issueId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();
        issue.assignTo(user);
        return issueRepository.save(issue);
    }

    @Transactional
    public Issue closeIssue(Long issueId) {
        Issue issue = issueRepository.findById(issueId).orElseThrow();
        issue.close();
        return issueRepository.save(issue);
    }
}
