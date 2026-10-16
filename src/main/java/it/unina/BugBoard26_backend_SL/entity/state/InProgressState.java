package it.unina.BugBoard26_backend_SL.entity.state;

import it.unina.BugBoard26_backend_SL.entity.Issue;
import it.unina.BugBoard26_backend_SL.entity.User;

public class InProgressState implements IssueState {

    @Override
    public void assign(Issue issue, User user) {
        throw new IllegalStateException(
                "Impossibile riassegnare una issue che è già in lavorazione (In Progress).");
    }

    @Override
    public void review(Issue issue) {
        issue.setState(new InReviewState());
        System.out.println("Transizione: IN_PROGRESS -> IN_REVIEW");
    }

    @Override
    public void sendBackToProgress(Issue issue) {
        throw new IllegalStateException(
                "La issue è già In Progress, non può essere rimandata indietro.");
    }

    @Override
    public void close(Issue issue) {
        issue.setState(new ClosedState());
        System.out.println("Transizione: IN_PROGRESS -> CLOSED");
    }

    @Override
    public String getStateName() {
        return "IN_PROGRESS";
    }
}