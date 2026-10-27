package seedu.boothmanagerpro.ui;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import seedu.boothmanagerpro.commons.core.GuiSettings;
import seedu.boothmanagerpro.commons.core.LogsCenter;
import seedu.boothmanagerpro.logic.Logic;
import seedu.boothmanagerpro.logic.commands.CommandResult;
import seedu.boothmanagerpro.logic.commands.HelpCommand;
import seedu.boothmanagerpro.logic.commands.exceptions.CommandException;
import seedu.boothmanagerpro.logic.parser.exceptions.ParseException;

/**
 * The Main Window. Provides the basic application layout containing
 * command input and text output, without graphical contact panels.
 */
public class MainWindow extends UiPart<Stage> {

    private static final String FXML = "MainWindow.fxml";
    private static final KeyCombination FOCUS_COMMAND = KeyCombination.valueOf("Shortcut+L");

    private final Logger logger = LogsCenter.getLogger(getClass());

    private Stage primaryStage;
    private Logic logic;
    private Path dataFilePath;

    // Independent Ui parts residing in this Ui container
    private ResultDisplay resultDisplay;
    private CommandBox commandBox;

    @FXML
    private Label commandHints;

    @FXML
    private StackPane commandBoxPlaceholder;

    @FXML
    private StackPane resultDisplayPlaceholder;

    @FXML
    private StackPane statusbarPlaceholder;

    /**
     * Creates a {@code MainWindow} with the given {@code Stage}, {@code Logic},
     * and the data file path available in the status bar tooltip.
     */
    public MainWindow(Stage primaryStage, Logic logic, Path dataFilePath) {
        super(FXML, primaryStage);

        // Set dependencies
        this.primaryStage = primaryStage;
        this.logic = logic;
        this.dataFilePath = dataFilePath;

        // Configure the UI
        setWindowDefaultSize(logic.getGuiSettings());
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Fills up all the placeholders of this window.
     */
    void fillInnerParts() {
        resultDisplay = new ResultDisplay();
        resultDisplayPlaceholder.getChildren().add(resultDisplay.getRoot());
        resultDisplay.setFeedbackToUser("Ready. Type list to show all contacts, find to search, "
                + "or help for usage instructions.");

        StatusBarFooter statusBarFooter = new StatusBarFooter(dataFilePath);
        statusbarPlaceholder.getChildren().add(statusBarFooter.getRoot());

        commandBox = new CommandBox(this::executeCommand);
        commandBoxPlaceholder.getChildren().add(commandBox.getRoot());
        commandHints.setText("Enter to run  |  " + FOCUS_COMMAND.getDisplayText() + " to focus  |  F1 for help");
        getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (FOCUS_COMMAND.match(event)) {
                commandBox.focus();
                event.consume();
            } else if (KeyCombination.valueOf("F1").match(event)) {
                handleHelp();
                event.consume();
            }
        });
        commandBox.focus();
    }

    /**
     * Restores saved bounds against the current monitors, recovering inaccessible or oversized windows.
     */
    private void setWindowDefaultSize(GuiSettings guiSettings) {
        List<Screen> screens = new ArrayList<>(Screen.getScreens());
        screens.remove(Screen.getPrimary());
        screens.addFirst(Screen.getPrimary());
        Rectangle2D bounds = WindowPlacement.fit(guiSettings,
                screens.stream().map(Screen::getVisualBounds).toList(),
                primaryStage.getMinWidth(), primaryStage.getMinHeight());
        primaryStage.setMinWidth(Math.min(primaryStage.getMinWidth(), bounds.getWidth()));
        primaryStage.setMinHeight(Math.min(primaryStage.getMinHeight(), bounds.getHeight()));
        primaryStage.setWidth(bounds.getWidth());
        primaryStage.setHeight(bounds.getHeight());
        primaryStage.setX(bounds.getMinX());
        primaryStage.setY(bounds.getMinY());
    }

    /**
     * Displays help in the command output area.
     */
    @FXML
    public void handleHelp() {
        try {
            executeCommand(HelpCommand.COMMAND_WORD);
        } catch (CommandException | ParseException e) {
            // executeCommand has already displayed the failure in the output area.
            logger.warning("Unable to show help: " + e.getMessage());
        }
    }

    void show() {
        primaryStage.show();
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleExit() {
        GuiSettings guiSettings = new GuiSettings(primaryStage.getWidth(), primaryStage.getHeight(),
                (int) primaryStage.getX(), (int) primaryStage.getY());
        logic.setGuiSettings(guiSettings);
        primaryStage.hide();
    }

    /**
     * Executes the command and returns the result.
     *
     * @see seedu.boothmanagerpro.logic.Logic#execute(String)
     */
    private CommandResult executeCommand(String commandText) throws CommandException, ParseException {
        try {
            CommandResult commandResult = logic.execute(commandText);
            logger.info("Result: " + commandResult.getFeedbackToUser());
            resultDisplay.setFeedbackToUser(commandResult.getFeedbackToUser());
            resultDisplay.showContacts(commandResult.getContactsToShow(), commandResult.isNumberedContacts(),
                    commandResult.isShowingContactTotal());

            commandBox.focus();

            if (commandResult.isExit()) {
                handleExit();
            }

            return commandResult;
        } catch (CommandException | ParseException e) {
            logger.info("An error occurred while executing command: " + commandText);
            resultDisplay.setFeedbackToUser(e.getMessage(), true);
            throw e;
        }
    }
}
