package seedu.boothmanagerpro.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.boothmanagerpro.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import seedu.boothmanagerpro.commons.core.LogsCenter;
import seedu.boothmanagerpro.commons.core.index.Index;
import seedu.boothmanagerpro.logic.Messages;
import seedu.boothmanagerpro.logic.commands.exceptions.CommandException;
import seedu.boothmanagerpro.model.Model;
import seedu.boothmanagerpro.model.person.ContactUpdate;
import seedu.boothmanagerpro.model.person.Name;
import seedu.boothmanagerpro.model.person.Person;

/** Updates a uniquely named contact or a displayed index, checking duplicates before any mutation. */
public class UpdateCommand extends Command {
    public static final String COMMAND_WORD = "update";
    public static final String MESSAGE_USAGE = "update NAME or INDEX [n/NAME] [c/COMPANY] [e/EMAIL] [p/PHONE] "
            + "[m/METHOD] [s/STATUS] [f/YYYYMMDD] [t/TAG]...\n"
            + "Supply at least one field. Empty f/ clears the date; empty t/ clears tags. "
            + "Use list or find for displayed indices. Example: update Alicia Tan s/In progress";
    public static final String MESSAGE_AMBIGUOUS = "More than one contact has the name %s. "
            + "Use list or find, then update the contact by its displayed index. No contact was updated.";
    public static final String MESSAGE_DUPLICATE = "This update conflicts with another contact: %s. "
            + "Check the email and name/company fields. No contact was updated.";

    private static final Logger logger = LogsCenter.getLogger(UpdateCommand.class);

    private final Index index;
    private final Name name;
    private final ContactUpdate changes;

    /** Targets the current filtered list using a one-based displayed index. */
    public UpdateCommand(Index index, ContactUpdate changes) {
        this.index = requireNonNull(index);
        this.name = null;
        this.changes = requireNonNull(changes);
    }

    /** Targets an exact, case-insensitive full name across all saved contacts. */
    public UpdateCommand(Name name, ContactUpdate changes) {
        this.index = null;
        this.name = requireNonNull(name);
        this.changes = requireNonNull(changes);
    }

    @Override
    public boolean modifiesAddressBook() {
        return true;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        logger.fine("Executing UpdateCommand targeting: " + (index != null ? index.getOneBased() : name));
        Person original = resolveTarget(model);
        Person updated = changes.applyTo(original);
        rejectDuplicates(model, original, updated);
        model.setPerson(original, updated);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        String identity = AddCommand.formatIdentity(updated);
        logger.info("Successfully updated contact: " + identity);
        return new CommandResult("Updated " + identity + (identity.endsWith(".") ? "" : "."));
    }

    /** Resolves ambiguity without selecting an arbitrary matching contact. */
    private Person resolveTarget(Model model) throws CommandException {
        if (index != null) {
            assert index.getZeroBased() >= 0 : "Target index must be non-negative";
            List<Person> displayed = model.getFilteredPersonList();
            if (index.getZeroBased() >= displayed.size()) {
                logger.warning("UpdateCommand failed: invalid displayed index " + index.getOneBased());
                throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
            }
            assert index.getZeroBased() < displayed.size() : "Resolved index must be within list bounds";
            Person target = displayed.get(index.getZeroBased());
            logger.fine("Resolved target contact by index " + index.getOneBased() + ": "
                    + AddCommand.formatIdentity(target));
            return target;
        }
        assert name != null : "Target name cannot be null when index is null";
        List<Person> matches = model.getAddressBook().getPersonList().stream()
                .filter(person -> person.getName().fullName.equalsIgnoreCase(name.fullName)).toList();
        if (matches.isEmpty()) {
            logger.warning("UpdateCommand failed: contact not found with name: " + name);
            throw new CommandException(String.format(DeleteCommand.MESSAGE_PERSON_NOT_FOUND, name));
        }
        if (matches.size() > 1) {
            logger.warning("UpdateCommand failed: ambiguous matches found for name: " + name);
            throw new CommandException(String.format(MESSAGE_AMBIGUOUS, name));
        }
        Person target = matches.getFirst();
        logger.fine("Resolved target contact by name " + name + ": " + AddCommand.formatIdentity(target));
        return target;
    }

    /** Includes hidden contacts, excluding only the contact being replaced. */
    private void rejectDuplicates(Model model, Person original, Person updated) throws CommandException {
        for (Person other : model.getAddressBook().getPersonList()) {
            if (!other.equals(original) && other.isSamePerson(updated)) {
                logger.warning("UpdateCommand rejected: conflict with existing contact: "
                        + AddCommand.formatIdentity(other));
                throw new CommandException(String.format(MESSAGE_DUPLICATE, AddCommand.formatIdentity(other)));
            }
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof UpdateCommand otherUpdateCommand)) {
            return false;
        }
        return Objects.equals(index, otherUpdateCommand.index)
                && Objects.equals(name, otherUpdateCommand.name)
                && changes.equals(otherUpdateCommand.changes);
    }
}
