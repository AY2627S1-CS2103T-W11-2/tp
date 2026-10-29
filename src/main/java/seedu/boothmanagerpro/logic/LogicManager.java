package seedu.boothmanagerpro.logic;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.function.Predicate;
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
import seedu.boothmanagerpro.model.AddressBook;
import seedu.boothmanagerpro.model.Model;
import seedu.boothmanagerpro.model.ReadOnlyAddressBook;
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
    /** The numbered prompt for {@code pendingViewChoices}, repeated when a reply is invalid. */
    private String pendingViewPrompt = "";

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
     * Executes a command or resolves a pending numbered view choice, saving only address-book mutations.
     * A successfully parsed command cancels the previous pending choice, even if execution fails.
     * If saving fails or execution fails, any changes made to the in-memory model are rolled back
     * to prevent unpersisted state from leaking or being persisted later.
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
        ReadOnlyAddressBook previousAddressBook = new AddressBook(model.getAddressBook());
        Predicate<? super Person> previousPredicate = model.getFilteredPersonListPredicate();

        CommandResult commandResult;
        try {
            commandResult = command.execute(model);
        } catch (CommandException | RuntimeException e) {
            rollbackModel(previousAddressBook, previousPredicate);
            throw e;
        }

        if (command instanceof ViewCommand) {
            pendingViewChoices = commandResult.getViewChoices();
            pendingViewPrompt = commandResult.getFeedbackToUser();
            if (!pendingViewChoices.isEmpty()) {
                assert !pendingViewPrompt.isBlank() : "Selection prompt must not be empty when view choices exist";
            }
            logger.fine("View request completed; pending choices: " + pendingViewChoices.size());
            return commandResult;
        }

        if (!command.modifiesAddressBook() && !storage.isAddressBookRecoveryPending()) {
            return commandResult;
        }

        try {
            storage.saveAddressBook(model.getAddressBook());
        } catch (AccessDeniedException e) {
            rollbackModel(previousAddressBook, previousPredicate);
            throw new CommandException(String.format(FILE_OPS_PERMISSION_ERROR_FORMAT, e.getMessage()), e);
        } catch (IOException ioe) {
            rollbackModel(previousAddressBook, previousPredicate);
            throw new CommandException(String.format(FILE_OPS_ERROR_FORMAT, ioe.getMessage()), ioe);
        }

        return commandResult;
    }

    /**
     * Restores the in-memory address book and filter predicate to the state prior to command execution.
     */
    private void rollbackModel(ReadOnlyAddressBook previousAddressBook,
                               Predicate<? super Person> previousPredicate) {
        model.setAddressBook(previousAddressBook);
        if (previousPredicate != null) {
            model.updateFilteredPersonList(previousPredicate::test);
        } else {
            model.updateFilteredPersonList(Model.PREDICATE_SHOW_ALL_PERSONS);
        }
    }

    /**
     * Resolves a one-based reply against the pending choices, filters the displayed list to the selected contact,
     * and clears the choices. Invalid replies preserve the choices so the user can retry; stale contacts cancel the
     * selection.
     *
     * @throws CommandException If the reply is invalid or the selected contact has changed.
     */
    private CommandResult selectViewContact(String input) throws CommandException {
        assert !pendingViewChoices.isEmpty() : "A numbered reply requires pending view choices";
        int index;
        try {
            index = ParserUtil.parseIndex(input).getZeroBased();
        } catch (ParseException e) {
            throw createInvalidSelectionException();
        }
        if (index >= pendingViewChoices.size()) {
            throw createInvalidSelectionException();
        }
        Person selected = pendingViewChoices.get(index);
        if (!model.getAddressBook().getPersonList().contains(selected)) {
            logger.fine("View selection canceled because the selected contact has changed");
            pendingViewChoices = List.of();
            throw new CommandException(MESSAGE_VIEW_CONTACT_CHANGED);
        }
        model.updateFilteredPersonList(person -> person.equals(selected));
        pendingViewChoices = List.of();
        logger.fine("Pending view selection resolved and filtered to the selected contact");
        return ViewCommand.createDetailsResult(selected);
    }

    /** Returns the valid-range error followed by the pending prompt, so the user can see the choices to retry. */
    private CommandException createInvalidSelectionException() {
        assert !pendingViewPrompt.isBlank() : "Pending view prompt should not be blank";
        return new CommandException(String.format(MESSAGE_VIEW_SELECTION, pendingViewChoices.size())
                + "\n" + pendingViewPrompt);
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
