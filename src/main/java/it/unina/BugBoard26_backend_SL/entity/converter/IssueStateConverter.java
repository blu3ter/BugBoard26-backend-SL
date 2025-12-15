package it.unina.BugBoard26_backend_SL.entity.converter;

import it.unina.BugBoard26_backend_SL.entity.state.*;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class IssueStateConverter implements AttributeConverter<IssueState, String> {

    @Override
    public String convertToDatabaseColumn(IssueState state) {
        if (state == null) return "TODO";
        return state.getStateName();
    }

    @Override
    public IssueState convertToEntityAttribute(String dbData) {
        if (dbData == null) return new TodoState();
        
        return switch (dbData) {
            case "IN_PROGRESS" -> new InProgressState();
            case "CLOSED" -> new ClosedState();
            default -> new TodoState();
        };
    }
}