package it.unina.BugBoard26_backend_SL.entity.state;

import it.unina.BugBoard26_backend_SL.entity.Issue;
import it.unina.BugBoard26_backend_SL.entity.User;

/**
 * Stato IN_REVIEW: l'issue è stata completata dallo sviluppatore
 * e attende la revisione da parte dell'admin.
 *
 * Transizioni consentite (solo ADMIN):
 *  - sendBackToProgress() → IN_PROGRESS (rifiuto: riassegnata allo stesso assignee)
 *  - close()              → CLOSED      (approvazione)
 *
 * Transizioni non consentite:
 *  - assign()             → lancia IllegalStateException
 *  - review()             → lancia IllegalStateException (già in review)
 */
public class InReviewState implements IssueState {

    @Override
    public void assign(Issue issue, User user) {
        throw new IllegalStateException(
                "Impossibile riassegnare una issue che è già in revisione (In Review).");
    }

    @Override
    public void review(Issue issue) {
        throw new IllegalStateException(
                "La issue è già nello stato In Review.");
    }

    @Override
    public void sendBackToProgress(Issue issue) {
        // L'assignee rimane invariato: l'admin rimanda indietro allo stesso sviluppatore
        issue.setState(new InProgressState());
        System.out.println("Transizione: IN_REVIEW -> IN_PROGRESS");
    }

    @Override
    public void close(Issue issue) {
        issue.setState(new ClosedState());
        System.out.println("Transizione: IN_REVIEW -> CLOSED");
    }

    @Override
    public String getStateName() {
        return "IN_REVIEW";
    }
}
