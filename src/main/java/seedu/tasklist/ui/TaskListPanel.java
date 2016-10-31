package seedu.tasklist.ui;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import seedu.tasklist.commons.core.EventsCenter;
import seedu.tasklist.commons.core.LogsCenter;
import seedu.tasklist.commons.events.TickEvent;
import seedu.tasklist.commons.events.model.TaskModifiedEvent;
import seedu.tasklist.commons.events.ui.TaskPanelSelectionChangedEvent;
import seedu.tasklist.model.task.ReadOnlyTask;

import static com.google.common.base.Preconditions.checkArgument;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import com.google.common.eventbus.Subscribe;

/**
 * Panel containing the list of persons.
 */
public class TaskListPanel extends UiPart {
	private final Logger logger = LogsCenter.getLogger(TaskListPanel.class);
	private static final String FXML = "TaskListPanel.fxml";
	private VBox panel;
	private AnchorPane placeHolderPane;

	@FXML
	private Label dateTimeLabel;
	@FXML
	private ListView<ReadOnlyTask> personListView;
	@FXML
	private ScrollPane scrollPane;

	public TaskListPanel() {
		super();
	}

	//@@author A0146107M
	@Subscribe
	public void tickEventHandler(TickEvent te){
		personListView.refresh();
		setLabelText();
	}

	@Subscribe
	public void taskModifiedEventHandler(TaskModifiedEvent tme){
		personListView.scrollTo(tme.task);
		personListView.getSelectionModel().select(tme.task);
	}
	//@@author
	@Override
	public void setNode(Node node) {
		panel = (VBox) node;
	}

	@Override
	public String getFxmlPath() {
		return FXML;
	}

	@Override
	public void setPlaceholder(AnchorPane pane) {
		this.placeHolderPane = pane;
	}

	public static TaskListPanel load(Stage primaryStage, AnchorPane personListPlaceholder,
			ObservableList<ReadOnlyTask> personList) {
		TaskListPanel taskListPanel =
				UiPartLoader.loadUiPart(primaryStage, personListPlaceholder, new TaskListPanel());
		taskListPanel.configure(personList);
		taskListPanel.setLabelText();
		//@@author A0146107M
		taskListPanel.scrollPane.setFitToHeight(true);
		taskListPanel.scrollPane.setFitToWidth(true);
		EventsCenter.getInstance().registerHandler(taskListPanel);
		//@@author
		return taskListPanel;
	}

	private void configure(ObservableList<ReadOnlyTask> personList) {
		setConnections(personList);
		addToPlaceholder();
	}

	private void setConnections(ObservableList<ReadOnlyTask> personList) {
		personListView.setItems(personList);
		personListView.setCellFactory(listView -> new PersonListViewCell());
		setEventHandlerForSelectionChangeEvent();
	}

	private void addToPlaceholder() {
		SplitPane.setResizableWithParent(placeHolderPane, false);
		placeHolderPane.getChildren().add(panel);
	}

	private void setEventHandlerForSelectionChangeEvent() {
		personListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				logger.fine("Selection in person list panel changed to : '" + newValue + "'");
				raise(new TaskPanelSelectionChangedEvent(newValue));
			}
		});
	}

	public void scrollTo(int index) {
		Platform.runLater(() -> {
			personListView.scrollTo(index);
			personListView.getSelectionModel().clearAndSelect(index);
		});
	}

	//@@author A0144919W
	public void setLabelText() {
		assert dateTimeLabel != null;
		SimpleDateFormat dateFormatter = new SimpleDateFormat("dd\'"+getDateSuffix(Integer.parseInt(new SimpleDateFormat("dd").format(new Date())))+"\' MMMMMMMMM, yyyy | h:mm a");
		dateTimeLabel.setText(dateFormatter.format(new Date()));
	}
	private String getDateSuffix(int date) {
		checkArgument(date >= 1 && date <= 31, "illegal day of month: " + date);
		if (date >= 11 && date <= 13) {
			return "th";
		}
		switch (date % 10) {
		case 1:  return "st";
		case 2:  return "nd";
		case 3:  return "rd";
		default: return "th";
		}
	}
	//@@author

	class PersonListViewCell extends ListCell<ReadOnlyTask> {

		public PersonListViewCell() {
		}

		@Override
		protected void updateItem(ReadOnlyTask person, boolean empty) {
			super.updateItem(person, empty);

			if (empty || person == null) {
				setGraphic(null);
				setText(null);
			} else {
				setGraphic(TaskCard.load(person, getIndex() + 1).getLayout());
			}
		}
	}

}
