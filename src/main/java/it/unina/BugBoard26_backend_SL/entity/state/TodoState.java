package it.unina.BugBoard26_backend_SL.entity.state;

import it.unina.BugBoard26_backend_SL.entity.Issue;
import it.unina.BugBoard26_backend_SL.entity.User;

public class TodoState implements IssueState {
    
    @Override
    public void assign(Issue issue, User user) {
        issue.setAssignee(user);
        issue.setState(new InProgressState());
        System.out.println("Transizione: TODO -> IN_PROGRESS");
    }

    @Override
    public void close(Issue issue) {
        issue.setState(new ClosedState());
    }

    @Override
    public String getStateName() {
        return "TODO";
    }
}