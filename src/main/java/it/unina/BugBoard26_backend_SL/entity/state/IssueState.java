package it.unina.BugBoard26_backend_SL.entity.state;

import it.unina.BugBoard26_backend_SL.entity.Issue;
import it.unina.BugBoard26_backend_SL.entity.User;

public interface IssueState {
    void assign(Issue issue, User user);
    void close(Issue issue);
    String getStateName(); // Serve per salvare il nome nel DB
}