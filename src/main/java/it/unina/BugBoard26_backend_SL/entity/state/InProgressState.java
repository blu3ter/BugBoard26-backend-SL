package it.unina.BugBoard26_backend_SL.entity.state;

import it.unina.BugBoard26_backend_SL.entity.Issue;
import it.unina.BugBoard26_backend_SL.entity.User;

public class InProgressState implements IssueState {

    @Override
    public void assign(Issue issue, User user) {
        // MODIFICA: Invece di riassegnare, lanciamo un'eccezione
        throw new IllegalStateException("Impossibile riassegnare una issue che è già in lavorazione (In Progress).");
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