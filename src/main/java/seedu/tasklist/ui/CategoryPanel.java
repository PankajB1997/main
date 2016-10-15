package seedu.tasklist.ui;

import javafx.collections.ObservableList;
import javafx.fxml.*;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import seedu.tasklist.commons.core.LogsCenter;
import seedu.tasklist.model.task.ReadOnlyTask;

import java.util.logging.Logger;

/**
 * Panel containing the list of persons.
 */
public class CategoryPanel extends UiPart {
    private final Logger logger = LogsCenter.getLogger(CategoryPanel.class);
    private static final String FXML = "CategoryPanel.fxml";
    private AnchorPane gridpane;
    private AnchorPane placeHolderPane;

    @FXML
    private Label overdueNo;
    @FXML
    private Label todayNo;
    @FXML
    private Label tomorrowNo;
    @FXML
    private Label floatingNo;
    @FXML
    private Label otherNo;
    @FXML
    private Label totalNo;
    @FXML
    private Label upcomingNo;
    
    
    
    public CategoryPanel() {
        super();
    }
    
    @Override
    public void setNode(Node node) {
    	gridpane = (AnchorPane) node;
    }

    @Override
    public String getFxmlPath() {
        return FXML;
    }

    @Override
    public void setPlaceholder(AnchorPane pane) {
        this.placeHolderPane = pane;
    }

    public static CategoryPanel load(Stage primaryStage, AnchorPane personListPlaceholder) {
        CategoryPanel categoryPanel =
                UiPartLoader.loadUiPart(primaryStage, personListPlaceholder, new CategoryPanel());
        categoryPanel.configure();
        return categoryPanel;
    }

    private void configure() {
    	gridpane.setStyle("-fx-background-color: #FFFFFF;");
        addToPlaceholder();
    }

    private void setConnections() {
    }

    private void addToPlaceholder() {
        SplitPane.setResizableWithParent(placeHolderPane, false);
        placeHolderPane.getChildren().add(gridpane);
    }
}