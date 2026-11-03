package seedu.boothmanagerpro.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.boothmanagerpro.commons.core.index.Index;
import seedu.boothmanagerpro.logic.Messages;
import seedu.boothmanagerpro.logic.commands.exceptions.CommandException;
import seedu.boothmanagerpro.model.Model;
import seedu.boothmanagerpro.model.person.Name;
import seedu.boothmanagerpro.model.person.Person;

/** Resolves mutation targets consistently against displayed indices or all stored names. */
final class ContactTargetResolver {
    private ContactTargetResolver() {
    }

    /** Resolves exactly one target, preserving the caller's missing-name and ambiguity messages. */
    static Person resolve(Model model, Index index, Name name, String missingMessage, String ambiguousMessage)
            throws CommandException {
        requireNonNull(model);
        assert (index == null) != (name == null) : "A target must specify either an index or a name";
        return index == null ? byName(model, name, missingMessage, ambiguousMessage) : byIndex(model, index);
    }

    /** Uses displayed order, including the active filter and sort comparator. */
    private static Person byIndex(Model model, Index index) throws CommandException {
        List<Person> displayed = model.getFilteredPersonList();
        int position = index.getZeroBased();
        assert position >= 0 : "Target index must be non-negative";
        if (position >= displayed.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        return displayed.get(position);
    }

    /** Includes hidden contacts and rejects ambiguity before any mutation is made. */
    private static Person byName(Model model, Name name, String missingMessage, String ambiguousMessage)
            throws CommandException {
        requireNonNull(name);
        List<Person> matches = model.getAddressBook().getPersonList().stream()
                .filter(person -> person.getName().fullName.equalsIgnoreCase(name.fullName))
                .toList();
        if (matches.isEmpty()) {
            throw new CommandException(String.format(missingMessage, name));
        }
        if (matches.size() > 1) {
            throw new CommandException(String.format(ambiguousMessage, name));
        }
        return matches.getFirst();
    }
}
