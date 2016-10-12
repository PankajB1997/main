package seedu.tasklist.model.task;

import seedu.tasklist.commons.exceptions.IllegalValueException;

/**
 * Represents a task's priority in the to-do list. Guarantees: immutable; is
 * valid as declared in {@link #isValidName(String)}
 */
public class Priority {
    public static final String MESSAGE_NAME_CONSTRAINTS = "The priority should only be specified as 'high', 'med' or 'low'.";
    public static final String PRIORITY_VALIDATION_REGEX = "\\b(high)|(low)|(med)\\b";

    public final String priorityLevel;

    public Priority(String priority) throws IllegalValueException {
        if (priority != null){
            priority = priority.trim().toLowerCase();
        	if (!isPriorityLevel(priority)) {
        		throw new IllegalValueException(MESSAGE_NAME_CONSTRAINTS);
	        }
	        this.priorityLevel = priority;
    	}
        else {
        	this.priorityLevel = "low";
        }
        
    }
    
    public static boolean isPriorityLevel(String test) {
            return test.matches(PRIORITY_VALIDATION_REGEX);
    }

    public String toString() {
        return priorityLevel;
    }

    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof Priority // instanceof handles nulls
                && this.priorityLevel.equals(((Priority) other).priorityLevel)); // state check
    }

    @Override
    public int hashCode() {
        return priorityLevel.hashCode();
    }

}
