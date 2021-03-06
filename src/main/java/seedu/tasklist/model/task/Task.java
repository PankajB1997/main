package seedu.tasklist.model.task;

import java.util.Calendar;
import java.util.Objects;

import seedu.tasklist.commons.util.CollectionUtil;


/**
 * Represents a Person in the address book.
 * Guarantees: details are present and not null, field values are validated.
 */
public class Task implements ReadOnlyTask, Comparable<Task> {

	private static int currentID = 0;

	private TaskDetails taskDetails;
	private StartTime startTime;
	private EndTime endTime;
	private Priority priority;
	private int uniqueID;
	private boolean isComplete;
	private boolean isRecurring;
	private String recurringFrequency;
	//private UniqueTagList tags;
	
	/**
	 * Every field must be present and not null.
	 */
	public Task(TaskDetails taskDetails, StartTime startTime, EndTime endTime, Priority priority, String recurringFrequency) {
		assert !CollectionUtil.isAnyNull(taskDetails, startTime, endTime, priority, recurringFrequency);
		this.taskDetails = taskDetails;
		this.startTime = startTime;
		this.endTime = endTime;
		this.priority = priority;
		this.uniqueID = currentID++;
		//this.tags = new UniqueTagList(tags); // protect internal tags from changes in the arg list
		this.isComplete = false;

		if (recurringFrequency==null) recurringFrequency = "";
		this.setRecurringFrequency(recurringFrequency);
		
		
//		endTimeOnly();	
	}
	
	public Task(TaskDetails taskDetails, StartTime startTime, EndTime endTime, Priority priority) {
		this(taskDetails, startTime, endTime, priority, "");
	}

	/**
	 * Copy constructor.
	 */
	public Task(ReadOnlyTask source) {
		this(source.getTaskDetails(), source.getStartTime(), source.getEndTime(), source.getPriority(), source.getRecurringFrequency());
		if(source.isComplete()){
			this.markAsComplete();
		}
	}

	@Override
	public TaskDetails getTaskDetails() {
		return taskDetails;
	}

	@Override
	public StartTime getStartTime() {
		return startTime;
	}

	@Override
	public Priority getPriority() {
		return priority;
	}
	
	@Override
	public String getRecurringFrequency() {
	    return recurringFrequency;
	}
	
	@Override
	public int getUniqueID() {
		return uniqueID;
	}

	public boolean isRecurring() {
	    return this.isRecurring;
	}
	
	@Override
	public boolean isComplete() {
		return isComplete;
	}

	public void markAsComplete() {
		isComplete = true;
	}
	
    public void markAsIncomplete() {
        isComplete = false;
    }

	public EndTime getEndTime(){
		setRecurringTime();
		return endTime;
	}

	public void setTaskDetails(TaskDetails taskDetails){
		this.taskDetails = taskDetails;
	}

	public void setStartTime(StartTime startTime){
		this.startTime = startTime;
	}

	public void setEndTime(EndTime endTime){
		this.endTime = endTime;
	}

	public void setPriority(Priority priority){
		this.priority = priority;
	}
	
	//@@author A0142102E
	public void setRecurringFrequency(String frequency) {
		if (this.isValidFrequency(frequency) && (this.hasStartTime() || this.hasEndTime())) {
			this.recurringFrequency = frequency;
			this.isRecurring = true;
		}
		else {
			this.recurringFrequency = "";
			this.isRecurring = false;
		}
	}

	/**
	 * Sets the time of the task to its next recurring time when marked as complete.
	 */
	public void setRecurringTime() {
	    if (isRecurring && !this.recurringFrequency.equals("")) {
	    	if (isComplete) {
	    		updateRecurringDate(startTime.time, recurringFrequency, 1);
	    		updateRecurringDate(endTime.time, recurringFrequency, 1);
	    	}

	        if (this.hasStartTime() || this.hasEndTime()) {
	            isComplete = false;
	        }
	    }
	}
	
	public boolean isValidFrequency(String frequency) {
		if (frequency.equals("daily") || frequency.equals("weekly") || frequency.equals("monthly") || frequency.equals("yearly")) {
			return true;
		}
		return false;
	}
	
	//@@author
	public boolean isFloating(){
		return endTime.isMissing()&&startTime.isMissing();
	}
	/*
	@Override
	public boolean equals(Object other) {
		return other == this // short circuit if same object
				|| (other instanceof ReadOnlyTask // instanceof handles nulls
						&& this.isSameStateAs((ReadOnlyTask) other));
	}
*/
	public boolean isOverDue(){
		if(!hasEndTime()){
			return false;
		}
		else{
			return endTime.time.before(Calendar.getInstance());
		}
	}

	@Override
	public int hashCode() {
		// use this method for custom fields hashing instead of implementing your own
		return Objects.hash(taskDetails, startTime, endTime, priority, uniqueID, recurringFrequency);
	}

	@Override
	public String toString() {
		return getAsText();
	}

    public boolean hasStartTime() {
        return startTime.time.getTimeInMillis()!=0;
    }
    
    public boolean hasEndTime() {
        return endTime.time.getTimeInMillis()!=0;
    }
    
	@Override
    public boolean isEvent() {
		return hasStartTime() && hasEndTime();
    }
    
	@Override
    public boolean isToday() {
    	if(hasStartTime()){
    		return startTime.time.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        }
    	else if(hasEndTime()){
    		return endTime.time.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
    	}
    	else {
    		return false;
    	}
	}
    	
    
	@Override
    public boolean isTomorrow() {
    	if(hasStartTime()){
    		return startTime.time.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR) + 1;
        }
    	else if(hasEndTime()){
    		return endTime.time.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR) + 1;
    	}
    	else {
    		return false;
    	}
    }

	//@@author A0142102E
	@Override
	public int compareTo(Task o) {
	    if (this.startTime.isMissing() && this.endTime.isMissing() && !o.getEndTime().isMissing() && o.isOverDue()) {
	        return 1;
	    }
	    else if (o.getStartTime().isMissing() && o.getEndTime().isMissing() && !this.endTime.isMissing() && this.isOverDue()) {
            return -1;
        }
	    if (this.startTime.equals(o.getStartTime()) && this.endTime.equals(o.getEndTime())) {
			return this.priority.compareTo(o.getPriority());
		}
		else {
			if (this.startTime.equals(o.getStartTime())) {
			    return this.endTime.compareTo(o.getEndTime());
			}
			else if (this.endTime.equals(o.getEndTime())) {
			    return this.startTime.compareTo(o.getStartTime());
			}
			else if(this.startTime.isMissing()) {
			    return this.endTime.compareTo(o.getStartTime());
			}
			else if (o.getStartTime().isMissing()){
			    return this.startTime.compareTo(o.getEndTime());
			}
			// if only has start time
			else {
			    return this.startTime.compareTo(o.getStartTime());
			}
		}
	}

    @Override
    public boolean equals(Task task) {
        if (this.getTaskDetails().equals(task.getTaskDetails())
            && this.getStartTime().equals(task.getStartTime())
            && this.getEndTime().equals(task.getEndTime())
            && this.getPriority().equals(task.getPriority())
            && this.recurringFrequency.equals(task.getRecurringFrequency())    )
            return true;
        else return false;
    }
	
	/*
	@Override
	public int compareTo(Task o) {
		if(!this.startTime.equals(o.getStartTime())){
			return this.startTime.compareTo(o.getStartTime());
		}
		else {
			return this.priority.compareTo(o.getPriority());
		}
	}
	*/
}