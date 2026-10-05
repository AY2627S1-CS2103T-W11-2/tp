package seedu.boothmanagerpro.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.function.Predicate;

import seedu.boothmanagerpro.commons.util.ToStringBuilder;
import seedu.boothmanagerpro.logic.Messages;
import seedu.boothmanagerpro.model.Model;
import seedu.boothmanagerpro.model.person.Person;

/**
 * Finds and lists contacts matching name keywords or exact field criteria.
 */
public class FindCommand extends Command {

    public static final String COMMAND_WORD = "find";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Finds contacts by name keywords or exact fields "
            + "and displays matching contacts with index numbers.\n"
            + "Parameters: KEYWORD [MORE_KEYWORDS]... or [n/NAME] [e/EMAIL] [p/PHONE] [t/TAG]...\n"
            + "Prefixed values match complete fields. Repeat a prefix for alternatives (OR); "
            + "different fields use AND.\n"
            + "Example: " + COMMAND_WORD + " n/Alice Pauline t/friends";

    private final Predicate<Person> predicate;

    public FindCommand(Predicate<Person> predicate) {
        this.predicate = requireNonNull(predicate);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(predicate);
        return new CommandResult(
                String.format(Messages.MESSAGE_PERSONS_LISTED_OVERVIEW, model.getFilteredPersonList().size()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof FindCommand otherFindCommand)) {
            return false;
        }

        return predicate.equals(otherFindCommand.predicate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("predicate", predicate)
                .toString();
    }
}
