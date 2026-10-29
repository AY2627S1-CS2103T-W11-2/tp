package seedu.boothmanagerpro.ui;

import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_COMPANY;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_FOLLOW_UP;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_METHOD;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_ORDER;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_STATUS;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import seedu.boothmanagerpro.logic.commands.AddCommand;
import seedu.boothmanagerpro.logic.commands.ClearCommand;
import seedu.boothmanagerpro.logic.commands.CommandResult;
import seedu.boothmanagerpro.logic.commands.DeleteCommand;
import seedu.boothmanagerpro.logic.commands.ExitCommand;
import seedu.boothmanagerpro.logic.commands.FindCommand;
import seedu.boothmanagerpro.logic.commands.HelpCommand;
import seedu.boothmanagerpro.logic.commands.ListCommand;
import seedu.boothmanagerpro.logic.commands.TagCommand;
import seedu.boothmanagerpro.logic.commands.UpdateCommand;
import seedu.boothmanagerpro.logic.commands.ViewCommand;
import seedu.boothmanagerpro.logic.commands.exceptions.CommandException;
import seedu.boothmanagerpro.logic.parser.exceptions.ParseException;
import seedu.boothmanagerpro.model.person.ContactMethod;
import seedu.boothmanagerpro.model.person.EnquiryStatus;
import seedu.boothmanagerpro.model.person.SortField;

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
    private static final Map<String, List<String>> COMMAND_PREFIXES = Map.of(
            AddCommand.COMMAND_WORD, List.of(PREFIX_NAME.toString(), PREFIX_COMPANY.toString(),
                    PREFIX_EMAIL.toString(), PREFIX_PHONE.toString(), PREFIX_METHOD.toString(),
                    PREFIX_STATUS.toString(), PREFIX_FOLLOW_UP.toString(), PREFIX_TAG.toString()),
            FindCommand.COMMAND_WORD, List.of(PREFIX_NAME.toString(), PREFIX_COMPANY.toString(),
                    PREFIX_EMAIL.toString(), PREFIX_PHONE.toString(), PREFIX_TAG.toString(),
                    PREFIX_FOLLOW_UP.toString()),
            ListCommand.COMMAND_WORD, List.of(PREFIX_ORDER.toString()),
            TagCommand.COMMAND_WORD, List.of(PREFIX_TAG.toString()),
            UpdateCommand.COMMAND_WORD, List.of(PREFIX_NAME.toString(), PREFIX_COMPANY.toString(),
                    PREFIX_EMAIL.toString(), PREFIX_PHONE.toString(), PREFIX_METHOD.toString(),
                    PREFIX_STATUS.toString(), PREFIX_FOLLOW_UP.toString(), PREFIX_TAG.toString()),
            ViewCommand.COMMAND_WORD, List.of(PREFIX_NAME.toString()));
    private static final Map<String, List<String>> PREFIX_VALUES = Map.of(
            PREFIX_METHOD.toString(), Arrays.stream(ContactMethod.values()).map(ContactMethod::toString).toList(),
            PREFIX_STATUS.toString(), Arrays.stream(EnquiryStatus.values()).map(EnquiryStatus::toString).toList(),
            PREFIX_ORDER.toString(), Arrays.stream(SortField.values()).map(SortField::toString).toList());

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
            if (event.getCode() == KeyCode.TAB && autocompleteCommand()) {
                event.consume();
            }
        });
        // JavaFX selects all text when keyboard focus returns, so the next keystroke would replace the command.
        // The caret is moved to the end after that selection is made instead.
        ChangeListener<Node> keepCaretOnFocusReturn = (unused, oldOwner, newOwner) -> {
            if (newOwner == commandTextField) {
                Platform.runLater(() -> commandTextField.positionCaret(commandTextField.getLength()));
            }
        };
        commandTextField.sceneProperty().addListener((unused, oldScene, newScene) -> {
            if (oldScene != null) {
                oldScene.focusOwnerProperty().removeListener(keepCaretOnFocusReturn);
            }
            if (newScene != null) {
                newScene.focusOwnerProperty().addListener(keepCaretOnFocusReturn);
            }
        });
    }

    /**
     * Completes the current command word, field prefix, or fixed-choice value when there is one clear completion.
     *
     * @return whether the command text was changed
     */
    private boolean autocompleteCommand() {
        String input = commandTextField.getText();
        String trimmedInput = input.trim();

        if (autocompleteAddField(trimmedInput)
                || autocompleteNameField(trimmedInput)
                || autocompleteListField(trimmedInput)
                || autocompleteFieldPrefix(trimmedInput)
                || autocompleteFieldValue(trimmedInput)
                || autocompleteTagField(trimmedInput)) {
            return true;
        }

        List<String> matches = COMMAND_WORDS.stream()
                .filter(command -> command.startsWith(trimmedInput))
                .toList();

        if (matches.size() == 1) {
            return setCommandText(matches.getFirst() + " ");
        }
        return false;
    }

    /**
     * Adds the next required field prefix while the user is building an add command.
     * A prefix is only advanced after the current field has a value.
     */
    private boolean autocompleteAddField(String input) {
        if (input.equals(AddCommand.COMMAND_WORD)) {
            return setCommandText(AddCommand.COMMAND_WORD + " " + ADD_REQUIRED_PREFIXES.getFirst());
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
            return false;
        }

        String currentPrefix = ADD_REQUIRED_PREFIXES.get(lastPrefixIndex);
        int valueStart = lastPrefixPosition + 1 + currentPrefix.length();
        boolean hasValue = !input.substring(valueStart).isBlank();
        boolean hasNextPrefix = lastPrefixIndex + 1 < ADD_REQUIRED_PREFIXES.size();
        if (hasValue && hasNextPrefix) {
            return setCommandText(input + " " + ADD_REQUIRED_PREFIXES.get(lastPrefixIndex + 1));
        }
        return false;
    }

    /** Adds the name prefix while the user is building a view or find command. */
    private boolean autocompleteNameField(String input) {
        if (input.equals(ViewCommand.COMMAND_WORD) || input.equals(FindCommand.COMMAND_WORD)) {
            return setCommandText(input + " " + PREFIX_NAME);
        }
        return false;
    }

    /** Adds the optional sort prefix when a user explicitly requests completion after {@code list}. */
    private boolean autocompleteListField(String input) {
        if (input.equals(ListCommand.COMMAND_WORD)) {
            return setCommandText(ListCommand.COMMAND_WORD + " " + PREFIX_ORDER);
        }
        return false;
    }

    /** Completes a partially typed field prefix supported by the current command. */
    private boolean autocompleteFieldPrefix(String input) {
        int firstSpace = input.indexOf(' ');
        int lastSpace = input.lastIndexOf(' ');
        if (firstSpace < 0 || lastSpace < firstSpace) {
            return false;
        }

        String commandWord = input.substring(0, firstSpace);
        List<String> supportedPrefixes = COMMAND_PREFIXES.getOrDefault(commandWord, List.of());
        String partialPrefix = input.substring(lastSpace + 1);
        List<String> matches = supportedPrefixes.stream()
                .filter(prefix -> prefix.startsWith(partialPrefix))
                .toList();
        if (matches.size() != 1) {
            return false;
        }
        return setCommandText(input.substring(0, lastSpace + 1) + matches.getFirst());
    }

    /** Completes contact methods, enquiry statuses, and list sort fields from an unambiguous prefix. */
    private boolean autocompleteFieldValue(String input) {
        int firstSpace = input.indexOf(' ');
        if (firstSpace < 0) {
            return false;
        }

        String commandWord = input.substring(0, firstSpace);
        List<String> supportedPrefixes = COMMAND_PREFIXES.getOrDefault(commandWord, List.of());
        for (Map.Entry<String, List<String>> entry : PREFIX_VALUES.entrySet()) {
            String prefix = entry.getKey();
            int prefixStart = input.lastIndexOf(" " + prefix);
            if (!supportedPrefixes.contains(prefix) || prefixStart < firstSpace) {
                continue;
            }
            String partialValue = input.substring(prefixStart + prefix.length() + 1);
            List<String> matches = entry.getValue().stream()
                    .filter(value -> value.regionMatches(true, 0, partialValue, 0, partialValue.length()))
                    .toList();
            if (matches.size() == 1) {
                return setCommandText(input.substring(0, prefixStart + prefix.length() + 1) + matches.getFirst());
            }
        }
        return false;
    }

    /** Adds the first required tag prefix after a tag command target has been entered. */
    private boolean autocompleteTagField(String input) {
        String commandPrefix = TagCommand.COMMAND_WORD + " ";
        if (!input.startsWith(commandPrefix) || input.substring(commandPrefix.length()).isBlank()
                || input.contains(" " + PREFIX_TAG)) {
            return false;
        }
        return setCommandText(input + " " + PREFIX_TAG);
    }

    /** Replaces changed command input and leaves the caret ready for continued typing. */
    private boolean setCommandText(String text) {
        if (text.equals(commandTextField.getText())) {
            return false;
        }
        commandTextField.setText(text);
        commandTextField.positionCaret(text.length());
        commandTextField.requestFocus();
        return true;
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
