package it.unina.BugBoard26_backend_SL.entity.state;

import it.unina.BugBoard26_backend_SL.entity.Issue;
import it.unina.BugBoard26_backend_SL.entity.User;

public interface IssueState {
    /** Assegna la issue a un utente (TODO → IN_PROGRESS). */
    void assign(Issue issue, User user);

    /** Manda la issue in revisione (IN_PROGRESS → IN_REVIEW). */
    void review(Issue issue);

    /**
     * Rimanda la issue in lavorazione (IN_REVIEW → IN_PROGRESS).
     * Usato dall'admin per rifiutare la revisione.
     */
    void sendBackToProgress(Issue issue);

    /** Chiude la issue (→ CLOSED). */
    void close(Issue issue);

    /** Serve per salvare il nome nel DB. */
    String getStateName();
}