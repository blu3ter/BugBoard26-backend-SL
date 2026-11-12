package it.unina.BugBoard26_backend_SL.entity.state;

import it.unina.BugBoard26_backend_SL.entity.Issue;
import it.unina.BugBoard26_backend_SL.entity.User;

public class ClosedState implements IssueState {

    @Override
    public void assign(Issue issue, User user) {
        throw new IllegalStateException("Non puoi assegnare una issue chiusa!");
    }

    @Override
    public void review(Issue issue) {
        throw new IllegalStateException(
                "Impossibile mandare in revisione una issue già chiusa.");
    }

    @Override
    public void sendBackToProgress(Issue issue) {
        throw new IllegalStateException(
                "Impossibile rimandare in lavorazione una issue già chiusa.");
    }

    @Override
    public void close(Issue issue) {
        // Già chiusa, non fare nulla
    }

    @Override
    public String getStateName() {
        return "CLOSED";
    }
}