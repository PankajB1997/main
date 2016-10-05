package seedu.address.logic.commands;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.tag.Tag;
import seedu.address.model.tag.UniqueTagList;
import seedu.address.model.task.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Adds a person to the address book.
 */
public class NoteCommand extends Command {

    public static final String COMMAND_WORD = "note";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a note or floating task to the Task Manager. "
            + "Parameters: note [TASKNAME]\n"
            + "Example: " + COMMAND_WORD
            + " Eat egg";

    public static final String MESSAGE_SUCCESS = "New note added: %1$s";

    private final Task toNote;

    /**
     * Convenience constructor using raw values.
     *
     * @throws IllegalValueException if any of the raw values are invalid
     */
    public NoteCommand(String taskName)
            throws IllegalValueException {
        final Set<Tag> tagSet = new HashSet<>();
        this.toNote = new Task(
                new Name(taskName),
                new StartTime(null),
                new EndTime(null),
                new UniqueTagList(tagSet)
        );
    }

    @Override
    public CommandResult execute() {
        assert model != null;
        model.addNote(toNote);
        return new CommandResult(String.format(MESSAGE_SUCCESS, toNote));

    }

}