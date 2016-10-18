package seedu.tasklist.model;
import seedu.tasklist.model.task.*;

import javafx.collections.transformation.FilteredList;
import seedu.tasklist.commons.core.ComponentManager;
import seedu.tasklist.commons.core.LogsCenter;
import seedu.tasklist.commons.core.UnmodifiableObservableList;
import seedu.tasklist.commons.events.model.TaskListChangedEvent;
import seedu.tasklist.logic.commands.UndoCommand;
import seedu.tasklist.model.tag.UniqueTagList;
import seedu.tasklist.model.task.EndTime;
import seedu.tasklist.model.task.Priority;
import seedu.tasklist.model.task.ReadOnlyTask;
import seedu.tasklist.model.task.StartTime;
import seedu.tasklist.model.task.Task;
import seedu.tasklist.model.task.TaskDetails;
import seedu.tasklist.model.task.UniqueTaskList;
import seedu.tasklist.model.task.UniqueTaskList.TaskNotFoundException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.time.DateUtils;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;

/**
 * Represents the in-memory model of the task list data. All changes to any
 * model should be synchronized.
 */
public class ModelManager extends ComponentManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    public static LinkedList<UndoInfo> undoStack = new LinkedList<UndoInfo>();
    public static LinkedList<UndoInfo> redoStack = new LinkedList<UndoInfo>();

    private final TaskList taskList;
    private final FilteredList<Task> filteredTasks;

	/**
	 * Initializes a ModelManager with the given TaskList
	 * TaskList and its variables should not be null
	 */
	public ModelManager(TaskList src, UserPrefs userPrefs) {
		super();
		assert src != null;
		assert userPrefs != null;

		logger.fine("Initializing with tasklist: " + src + " and user prefs " + userPrefs);

		taskList = new TaskList(src);
		filteredTasks = new FilteredList<>(taskList.getTasks());
	}

	public ModelManager() {
		this(new TaskList(), new UserPrefs());
	}

	public ModelManager(ReadOnlyTaskList initialData, UserPrefs userPrefs) {
		taskList = new TaskList(initialData);
		filteredTasks = new FilteredList<>(taskList.getTasks());
	}

	@Override
	public void resetData(ReadOnlyTaskList newData) {
		taskList.resetData(newData);
		indicateTaskListChanged();
		if (newData.isEmpty())
     		addToUndoStack(UndoCommand.CLR_CMD_ID, (Task) taskList.getTaskList());
	}

    @Override
    public void clearTaskUndo(ArrayList<Task> tasks) throws TaskNotFoundException {
        TaskList oldTaskList = new TaskList();
        oldTaskList.setTasks(tasks);
        taskList.resetData(oldTaskList);
    }

	@Override
	public ReadOnlyTaskList getTaskList() {
		return taskList;
	}

	/** Raises an event to indicate the model has changed */
	private void indicateTaskListChanged() {
		raise(new TaskListChangedEvent(taskList));
	}
	
    @Override
    public void deleteTaskUndo(ReadOnlyTask target) throws TaskNotFoundException {
        taskList.removeTask(target);
        updateFilteredListToShowIncomplete();
        indicateTaskListChanged();
    }

	@Override
	public synchronized void deleteTask(ReadOnlyTask target) throws TaskNotFoundException {
		if(target instanceof Task){
			Task myTask = (Task) target;
			if(!myTask.isComplete())
				myTask.IncompleteCounter--;
			if(myTask.isOverDue()&&!myTask.isComplete()){
				myTask.overdueCounter--;
			}
			if(myTask.isFloating()&&!myTask.isComplete()){
				myTask.floatCounter--;
			}	
		}
		taskList.removeTask(target);
		updateFilteredListToShowIncomplete();
		indicateTaskListChanged();
		addToUndoStack(UndoCommand.DEL_CMD_ID, (Task) target);
	}

	@Override
	public synchronized void addTask(Task task) throws UniqueTaskList.DuplicateTaskException {
		if(task instanceof Task){
			Task myTask = (Task) task;
			myTask.IncompleteCounter++;
			if(myTask.isOverDue()){
				myTask.overdueCounter++;
			}
			if(myTask.isFloating()){
				myTask.floatCounter++;
			}	
		}
		taskList.addTask(task);
		updateFilteredListToShowIncomplete();
		indicateTaskListChanged();
		addToUndoStack(UndoCommand.ADD_CMD_ID, task);
	}

    @Override
    public void addTaskUndo(Task task) throws UniqueTaskList.DuplicateTaskException {
        taskList.addTask(task);
        updateFilteredListToShowIncomplete();
        indicateTaskListChanged();
    }
    
    @Override
    public synchronized void updateTask(Task taskToUpdate, TaskDetails taskDetails, StartTime startTime,
            EndTime endTime, Priority priority, UniqueTagList tags) throws UniqueTaskList.DuplicateTaskException {
        Task originalTask = new Task(taskToUpdate);
        taskList.updateTask(taskToUpdate, taskDetails, startTime, endTime, priority);
        updateFilteredListToShowIncomplete();
        indicateTaskListChanged();
        addToUndoStack(UndoCommand.UPD_CMD_ID, taskToUpdate, originalTask);
    }

    @Override
    public void updateTaskUndo(Task taskToUpdate, TaskDetails taskDetails, StartTime startTime, EndTime endTime, Priority priority, UniqueTagList tags) throws UniqueTaskList.DuplicateTaskException {
        taskList.updateTask(taskToUpdate, taskDetails, startTime, endTime, priority);
        updateFilteredListToShowIncomplete();
        indicateTaskListChanged();
    }

	@Override
	public synchronized void markTaskAsComplete(ReadOnlyTask task) throws TaskNotFoundException {
		if(task instanceof Task){
			Task myTask = (Task) task;
			myTask.IncompleteCounter--;
			if(myTask.isOverDue()){
				myTask.overdueCounter--;
			}
			if(myTask.isFloating()){
				myTask.floatCounter--;
			}
		}
		taskList.markTaskAsComplete(task);
		updateFilteredListToShowIncomplete();
		indicateTaskListChanged();
		addToUndoStack(UndoCommand.DONE_CMD_ID, (Task) task);
	}


    @Override
    public synchronized void markTaskAsIncomplete(ReadOnlyTask task) throws TaskNotFoundException {
        taskList.markTaskAsIncomplete(task);
        updateFilteredListToShowIncomplete();
        indicateTaskListChanged();
    }
    

    private void addToUndoStack(int undoID, Task... tasks) {
        if (undoStack.size() == 3) {
            undoStack.remove(undoStack.size() - 1);
        }
        UndoInfo undoInfo = new UndoInfo(undoID, tasks);
        undoStack.push(undoInfo);
    }
    
    @Override
    public void updateFilteredListToShowPriority(String priority) {
        updateFilteredListToShowAll();
        updateFilteredTaskList(new PredicateExpression(new PriorityQualifier(priority)));
    }
    
    @Override
    public void updateFilteredListToShowDate(String date) {
    	updateFilteredListToShowAll();
	    updateFilteredTaskList(new PredicateExpression(new DateQualifier(date)));

    }
	//=========== Filtered Person List Accessors ===============================================================

	@Override
	public UnmodifiableObservableList<ReadOnlyTask> getFilteredTaskList() {
		return new UnmodifiableObservableList<>(filteredTasks);
	}

	public UnmodifiableObservableList<Task> getModifiableTaskList() {
		return new UnmodifiableObservableList<>(filteredTasks);
	}

	@Override
	public void updateFilteredListToShowAll() {
		filteredTasks.setPredicate(null);
	}

	@Override
	public void updateFilteredListToShowIncomplete() {
		updateFilteredListToShowAll();
		updateFilteredTaskList(new PredicateExpression(new DefaultDisplayQualifier()));
	}

	public void updateFilteredList(){
		updateFilteredListToShowIncomplete();
		indicateTaskListChanged();
	}

	@Override
	public void updateFilteredTaskList(Set<String> keywords){
		updateFilteredTaskList(new PredicateExpression(new NameQualifier(keywords)));
	}

	private void updateFilteredTaskList(Expression expression) {
		filteredTasks.setPredicate(expression::satisfies);
	}

	public Set<String> getKeywordsFromList(List<ReadOnlyTask> tasks){
		Set<String> keywords = new HashSet<String>();
		for(ReadOnlyTask task: tasks){
			keywords.addAll(Arrays.asList(task.getTaskDetails().toString().split(" ")));
		}
		return keywords;
	}

	@Override
	public void updateFilteredListToShowComplete() {
		updateFilteredListToShowAll();
		updateFilteredTaskList(new PredicateExpression(new CompletedQualifier()));
	}

	@Override
	public void updateFilteredListToShowFloating(){
		updateFilteredListToShowAll();
		updateFilteredTaskList(new PredicateExpression(new FloatingQualifier()));
	}

	@Override
	public void updateFilteredListToShowOverDue(){
		updateFilteredListToShowAll();
		updateFilteredTaskList(new PredicateExpression(new OverDueQualifier()));
	}

	//========== Inner classes/interfaces used for filtering ==================================================

	interface Expression {
		boolean satisfies(ReadOnlyTask person);
		String toString();
	}

	private class PredicateExpression implements Expression {

		private final Qualifier qualifier;

		PredicateExpression(Qualifier qualifier) {
			this.qualifier = qualifier;
		}

		@Override
		public boolean satisfies(ReadOnlyTask person) {
			return qualifier.run(person);
		}

		@Override
		public String toString() {
			return qualifier.toString();
		}
	}

	interface Qualifier {
		boolean run(ReadOnlyTask person);
		String toString();
	}

	private class DefaultDisplayQualifier implements Qualifier {

		DefaultDisplayQualifier(){

		}

		@Override
		public boolean run(ReadOnlyTask person) {
			return !person.isComplete();
		}
	}

	private class CompletedQualifier implements Qualifier {
		@Override
		public boolean run(ReadOnlyTask person) {
			return person.isComplete();
		}
	}

	private class FloatingQualifier implements Qualifier {
		@Override
		public boolean run(ReadOnlyTask person) {
			return person.isFloating();
		}
	}
    
    private class PriorityQualifier implements Qualifier {
        private String priority;
	    
        public PriorityQualifier(String priority) {
            this.priority = priority.replaceFirst("p/", "");
        }
    	
        @Override
        public boolean run(ReadOnlyTask person) {
            return person.getPriority().priorityLevel.equals(this.priority);
        }
    }
    
    private class DateQualifier implements Qualifier {
        private final Calendar requestedTime;
	    
        public DateQualifier(String time) {
        	requestedTime = Calendar.getInstance();
        	List<DateGroup> dates = new Parser().parse(time);
        	requestedTime.setTime(dates.get(0).getDates().get(0));
        }
    	
        @Override
        public boolean run(ReadOnlyTask person) {        	
        	return DateUtils.isSameDay(person.getStartTime().starttime, requestedTime) ||
                    (person.getStartTime().toCardString().equals("-") && DateUtils.isSameDay(person.getEndTime().endtime, requestedTime));
        }
    }

	private class OverDueQualifier implements Qualifier {
		@Override
		public boolean run(ReadOnlyTask person) {
			return person.isOverDue();
		}
	}

	private class NameQualifier implements Qualifier {
		private Set<String> nameKeyWords;
		private Pattern NAME_QUERY;


		NameQualifier(Set<String> nameKeyWords) {
			this.nameKeyWords = nameKeyWords;
			this.NAME_QUERY = Pattern.compile(getRegexFromString(), Pattern.CASE_INSENSITIVE);
		}

		private String getRegexFromString(){
			String result = "";
			for(String keyword: nameKeyWords){
				for(char c: keyword.toCharArray()){
					switch(c){
					case '*':
						result += ".*";
						break;
					default:
						result += c;
					}
				}
			}
			return result;
		}

		@Override
		public boolean run(ReadOnlyTask person) {
			Matcher matcher = NAME_QUERY.matcher(person.getTaskDetails().taskDetails);
			return matcher.matches() && !person.isComplete();
		}

		@Override
		public String toString() {
			return "name=" + String.join(", ", nameKeyWords);
		}
	}

}
