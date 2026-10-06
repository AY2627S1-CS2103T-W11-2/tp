package seedu.boothmanagerpro.logic;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import seedu.boothmanagerpro.commons.core.GuiSettings;
import seedu.boothmanagerpro.commons.core.LogsCenter;
import seedu.boothmanagerpro.logic.commands.Command;
import seedu.boothmanagerpro.logic.commands.CommandResult;
import seedu.boothmanagerpro.logic.commands.ViewCommand;
import seedu.boothmanagerpro.logic.commands.exceptions.CommandException;
import seedu.boothmanagerpro.logic.parser.AddressBookParser;
import seedu.boothmanagerpro.logic.parser.ParserUtil;
import seedu.boothmanagerpro.logic.parser.exceptions.ParseException;
import seedu.boothmanagerpro.model.Model;
import seedu.boothmanagerpro.model.person.Person;
import seedu.boothmanagerpro.storage.Storage;

/**
 * The main LogicManager of the app.
 */
public class LogicManager implements Logic {
    public static final String FILE_OPS_ERROR_FORMAT = "Could not save data due to the following error: %s";

    public static final String FILE_OPS_PERMISSION_ERROR_FORMAT =
            "Could not save data to file %s due to insufficient permissions to write to the file or the folder.";

    public static final String MESSAGE_VIEW_SELECTION = "Please enter a number between 1 and %d.";
    public static final String MESSAGE_VIEW_CONTACT_CHANGED =
            "The selected contact has changed. Run view n/NAME again.";

    private List<Person> pendingViewChoices = List.of();

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final Storage storage;
    private final AddressBookParser addressBookParser;

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        this.model = model;
        this.storage = storage;
        addressBookParser = new AddressBookParser();
    }

    /**
     * Executes a command or resolves a pending numbered view choice without saving read-only view requests.
     * A successfully parsed command cancels the previous pending choice, even if execution fails.
     *
     * @throws CommandException If execution, selection, or saving fails.
     * @throws ParseException If the input cannot be parsed as a command or a pending selection.
     */
    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        logger.info("----------------[USER COMMAND][" + commandText + "]");

        Command command;
        try {
            command = addressBookParser.parseCommand(commandText);
        } catch (ParseException e) {
            if (!pendingViewChoices.isEmpty() && e.getMessage().equals(Messages.MESSAGE_UNKNOWN_COMMAND)) {
                return selectViewContact(commandText);
            }
            throw e;
        }
        // A new recognized command cancels the previous selection, even if its execution fails.
        pendingViewChoices = List.of();
        CommandResult commandResult = command.execute(model);
        if (command instanceof ViewCommand) {
            pendingViewChoices = commandResult.getViewChoices();
            logger.fine("View request completed; pending choices: " + pendingViewChoices.size());
            return commandResult;
        }

        try {
            storage.saveAddressBook(model.getAddressBook());
        } catch (AccessDeniedException e) {
            throw new CommandException(String.format(FILE_OPS_PERMISSION_ERROR_FORMAT, e.getMessage()), e);
        } catch (IOException ioe) {
            throw new CommandException(String.format(FILE_OPS_ERROR_FORMAT, ioe.getMessage()), ioe);
        }

        return commandResult;
    }

    /**
     * Resolves a one-based reply against the pending choices and clears them after a successful selection.
     * Invalid replies preserve the choices so the user can retry; stale contacts cancel the selection.
     *
     * @throws CommandException If the reply is invalid or the selected contact has changed.
     */
    private CommandResult selectViewContact(String input) throws CommandException {
        assert !pendingViewChoices.isEmpty() : "A numbered reply requires pending view choices";
        int index;
        try {
            index = ParserUtil.parseIndex(input).getZeroBased();
        } catch (ParseException e) {
            throw new CommandException(String.format(MESSAGE_VIEW_SELECTION, pendingViewChoices.size()));
        }
        if (index >= pendingViewChoices.size()) {
            throw new CommandException(String.format(MESSAGE_VIEW_SELECTION, pendingViewChoices.size()));
        }
        Person selected = pendingViewChoices.get(index);
        if (!model.getAddressBook().getPersonList().contains(selected)) {
            logger.fine("View selection canceled because the selected contact has changed");
            pendingViewChoices = List.of();
            throw new CommandException(MESSAGE_VIEW_CONTACT_CHANGED);
        }
        pendingViewChoices = List.of();
        logger.fine("Pending view selection resolved");
        return ViewCommand.createDetailsResult(selected);
    }

    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return model.getFilteredPersonList();
    }

    @Override
    public boolean isAwaitingViewSelection() {
        return !pendingViewChoices.isEmpty();
    }

    @Override
    public GuiSettings getGuiSettings() {
        return model.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        model.setGuiSettings(guiSettings);
    }
}
