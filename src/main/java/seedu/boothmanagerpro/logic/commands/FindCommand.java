package seedu.boothmanagerpro.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Logger;

import seedu.boothmanagerpro.commons.core.LogsCenter;
import seedu.boothmanagerpro.commons.util.ToStringBuilder;
import seedu.boothmanagerpro.logic.Messages;
import seedu.boothmanagerpro.model.Model;
import seedu.boothmanagerpro.model.person.Person;

/**
 * Finds and lists contacts matching name words, full names, partial or full companies/emails/phones,
 * or complete method, status, tag, and follow-up date criteria.
 */
public class FindCommand extends Command {
    public static final String COMMAND_WORD = "find";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Finds contacts by name words or field criteria "
            + "and displays matching contacts with index numbers.\n"
            + "Parameters: KEYWORD [MORE_KEYWORDS]... or [n/NAME] [c/COMPANY] [e/EMAIL] [p/PHONE] "
            + "[m/METHOD] [s/STATUS] [t/TAG] [f/YYYYMMDD]...\n"
            + "Names match a complete name or a whole name word; companies, emails, and phones match full or partial "
            + "values; methods, statuses, tags, and dates match complete values. "
            + "Repeat a prefix for alternatives (OR); different fields use AND.\n"
            + "Example: " + COMMAND_WORD + " n/Alice Pauline t/friends";

    private static final Logger logger = LogsCenter.getLogger(FindCommand.class);

    private final Predicate<Person> predicate;

    public FindCommand(Predicate<Person> predicate) {
        this.predicate = requireNonNull(predicate);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(predicate);
        List<Person> matches = model.getFilteredPersonList();
        String summary = String.format(Messages.MESSAGE_PERSONS_LISTED_OVERVIEW, matches.size());
        CommandResult result = CommandResult.showingContacts(matches.isEmpty() ? summary
                : summary + "\n\n" + Messages.formatNumberedContacts(matches), matches);
        logger.info("Contact search completed: " + matches.size() + " matches.");
        return result;
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
