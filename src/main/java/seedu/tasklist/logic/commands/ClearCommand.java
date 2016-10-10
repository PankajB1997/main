package seedu.tasklist.logic.commands;

import seedu.tasklist.model.TaskList;

/**
 * Clears the address book.
 */
public class ClearCommand extends Command {

    public static final String COMMAND_WORD = "clear";
    public static final String MESSAGE_SUCCESS = "Your to-do list has been cleared!";

    public ClearCommand() {}


    @Override
    public CommandResult execute() {
        assert model != null;
        model.resetData(TaskList.getEmptyAddressBook());
        return new CommandResult(MESSAGE_SUCCESS);
    }
}