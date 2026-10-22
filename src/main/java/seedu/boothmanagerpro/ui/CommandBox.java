package seedu.boothmanagerpro.ui;

import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_COMPANY;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_PHONE;

import java.util.List;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import seedu.boothmanagerpro.logic.commands.AddCommand;
import seedu.boothmanagerpro.logic.commands.ClearCommand;
import seedu.boothmanagerpro.logic.commands.CommandResult;
import seedu.boothmanagerpro.logic.commands.DeleteCommand;
import seedu.boothmanagerpro.logic.commands.EditCommand;
import seedu.boothmanagerpro.logic.commands.ExitCommand;
import seedu.boothmanagerpro.logic.commands.FindCommand;
import seedu.boothmanagerpro.logic.commands.HelpCommand;
import seedu.boothmanagerpro.logic.commands.ListCommand;
import seedu.boothmanagerpro.logic.commands.TagCommand;
import seedu.boothmanagerpro.logic.commands.UpdateCommand;
import seedu.boothmanagerpro.logic.commands.ViewCommand;
import seedu.boothmanagerpro.logic.commands.exceptions.CommandException;
import seedu.boothmanagerpro.logic.parser.exceptions.ParseException;

/**
 * The UI component that is responsible for receiving user command inputs.
 */
public class CommandBox extends UiPart<Region> {

    public static final String ERROR_STYLE_CLASS = "error";
    private static final String FXML = "CommandBox.fxml";
    private static final List<String> COMMAND_WORDS = List.of(
            AddCommand.COMMAND_WORD,
            ClearCommand.COMMAND_WORD,
            DeleteCommand.COMMAND_WORD,
            EditCommand.COMMAND_WORD,
            ExitCommand.COMMAND_WORD,
            FindCommand.COMMAND_WORD,
            HelpCommand.COMMAND_WORD,
            ListCommand.COMMAND_WORD,
            TagCommand.COMMAND_WORD,
            UpdateCommand.COMMAND_WORD,
            ViewCommand.COMMAND_WORD);
    private static final List<String> ADD_REQUIRED_PREFIXES = List.of(
            PREFIX_NAME.toString(),
            PREFIX_COMPANY.toString(),
            PREFIX_EMAIL.toString(),
            PREFIX_PHONE.toString());

    private final CommandExecutor commandExecutor;

    @FXML
    private TextField commandTextField;

    /**
     * Creates a {@code CommandBox} with the given {@code CommandExecutor}.
     */
    public CommandBox(CommandExecutor commandExecutor) {
        super(FXML);
        this.commandExecutor = commandExecutor;
        // calls #setStyleToDefault() whenever there is a change to the text of the command box.
        commandTextField.textProperty().addListener((unused1, unused2, unused3) -> setStyleToDefault());
        // Capture Tab before JavaFX uses it to move focus to another control.
        commandTextField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.TAB) {
                autocompleteCommand();
                event.consume();
            }
        });
    }

    /**
     * Completes the command word when the current input uniquely identifies a supported command.
     */
    private void autocompleteCommand() {
        String input = commandTextField.getText();
        String trimmedInput = input.trim();

        if (autocompleteAddField(trimmedInput) || autocompleteViewField(trimmedInput)) {
            return;
        }

        List<String> matches = COMMAND_WORDS.stream()
                .filter(command -> command.startsWith(trimmedInput))
                .toList();

        if (matches.size() == 1) {
            setCommandText(matches.get(0) + " ");
        }
    }

    /**
     * Adds the next required field prefix while the user is building an add command.
     * A prefix is only advanced after the current field has a value.
     */
    private boolean autocompleteAddField(String input) {
        if (input.equals(AddCommand.COMMAND_WORD)) {
            setCommandText(AddCommand.COMMAND_WORD + " " + ADD_REQUIRED_PREFIXES.getFirst());
            return true;
        }
        if (!input.startsWith(AddCommand.COMMAND_WORD + " ")) {
            return false;
        }

        int lastPrefixPosition = -1;
        int lastPrefixIndex = -1;
        for (int i = 0; i < ADD_REQUIRED_PREFIXES.size(); i++) {
            int position = input.lastIndexOf(" " + ADD_REQUIRED_PREFIXES.get(i));
            if (position > lastPrefixPosition) {
                lastPrefixPosition = position;
                lastPrefixIndex = i;
            }
        }

        if (lastPrefixIndex == -1) {
            return true;
        }

        String currentPrefix = ADD_REQUIRED_PREFIXES.get(lastPrefixIndex);
        int valueStart = lastPrefixPosition + 1 + currentPrefix.length();
        boolean hasValue = !input.substring(valueStart).isBlank();
        boolean hasNextPrefix = lastPrefixIndex + 1 < ADD_REQUIRED_PREFIXES.size();
        if (hasValue && hasNextPrefix) {
            setCommandText(input + " " + ADD_REQUIRED_PREFIXES.get(lastPrefixIndex + 1));
        }
        return true;
    }

    /** Adds the required name prefix while the user is building a view command. */
    private boolean autocompleteViewField(String input) {
        if (input.equals(ViewCommand.COMMAND_WORD)) {
            setCommandText(ViewCommand.COMMAND_WORD + " " + PREFIX_NAME);
            return true;
        }
        return input.startsWith(ViewCommand.COMMAND_WORD + " ");
    }

    /** Replaces the command input and leaves the caret ready for continued typing. */
    private void setCommandText(String text) {
        commandTextField.setText(text);
        commandTextField.positionCaret(text.length());
        commandTextField.requestFocus();
    }

    /** Returns keyboard focus to the command input. */
    public void focus() {
        commandTextField.requestFocus();
    }

    /**
     * Handles the Enter button pressed event.
     */
    @FXML
    private void handleCommandEntered() {
        String commandText = commandTextField.getText();
        if (commandText.isBlank()) {
            return;
        }

        try {
            commandExecutor.execute(commandText);
            commandTextField.setText("");
        } catch (CommandException | ParseException e) {
            setStyleToIndicateCommandFailure();
        }
    }

    /**
     * Sets the command box style to use the default style.
     */
    private void setStyleToDefault() {
        commandTextField.getStyleClass().remove(ERROR_STYLE_CLASS);
    }

    /**
     * Sets the command box style to indicate a failed command.
     */
    private void setStyleToIndicateCommandFailure() {
        ObservableList<String> styleClass = commandTextField.getStyleClass();

        if (styleClass.contains(ERROR_STYLE_CLASS)) {
            return;
        }

        styleClass.add(ERROR_STYLE_CLASS);
    }

    /**
     * Represents a function that can execute commands.
     */
    @FunctionalInterface
    public interface CommandExecutor {
        /**
         * Executes the command and returns the result.
         *
         * @see seedu.boothmanagerpro.logic.Logic#execute(String)
         */
        CommandResult execute(String commandText) throws CommandException, ParseException;
    }

}
