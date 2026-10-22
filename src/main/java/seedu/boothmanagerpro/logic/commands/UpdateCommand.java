package seedu.boothmanagerpro.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.boothmanagerpro.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.List;

import seedu.boothmanagerpro.commons.core.index.Index;
import seedu.boothmanagerpro.logic.Messages;
import seedu.boothmanagerpro.logic.commands.exceptions.CommandException;
import seedu.boothmanagerpro.model.Model;
import seedu.boothmanagerpro.model.person.EnquiryStatus;
import seedu.boothmanagerpro.model.person.Name;
import seedu.boothmanagerpro.model.person.Person;

/** Updates a uniquely named contact or a displayed index, changing only enquiry status. */
public class UpdateCommand extends Command {
    public static final String COMMAND_WORD = "update";
    public static final String MESSAGE_USAGE = "update NAME_OR_INDEX s/STATUS\n"
            + EnquiryStatus.MESSAGE_CONSTRAINTS + "\nExample: update Alicia Tan s/In progress";
    public static final String MESSAGE_AMBIGUOUS = "More than one contact has the name %s. "
            + "Use list or find, then update the contact by its displayed index. No contact was updated.";
    private final Index index;
    private final Name name;
    private final EnquiryStatus status;

    /** Targets the current filtered list using a one-based displayed index. */
    public UpdateCommand(Index index, EnquiryStatus status) {
        this.index = requireNonNull(index);
        this.name = null;
        this.status = requireNonNull(status);
    }

    /** Targets an exact, case-insensitive full name across all saved contacts. */
    public UpdateCommand(Name name, EnquiryStatus status) {
        this.index = null;
        this.name = requireNonNull(name);
        this.status = requireNonNull(status);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Person original = resolveTarget(model);
        Person updated = original.withEnquiryStatus(status);
        model.setPerson(original, updated);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        return new CommandResult("Updated " + AddCommand.formatIdentity(updated) + ".");
    }

    /** Resolves ambiguity without selecting an arbitrary matching contact. */
    private Person resolveTarget(Model model) throws CommandException {
        if (index != null) {
            List<Person> displayed = model.getFilteredPersonList();
            if (index.getZeroBased() >= displayed.size()) {
                throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
            }
            return displayed.get(index.getZeroBased());
        }
        List<Person> matches = model.getAddressBook().getPersonList().stream()
                .filter(person -> person.getName().fullName.equalsIgnoreCase(name.fullName)).toList();
        if (matches.isEmpty()) {
            throw new CommandException(String.format(DeleteCommand.MESSAGE_PERSON_NOT_FOUND, name));
        }
        if (matches.size() > 1) {
            throw new CommandException(String.format(MESSAGE_AMBIGUOUS, name));
        }
        return matches.getFirst();
    }

}
