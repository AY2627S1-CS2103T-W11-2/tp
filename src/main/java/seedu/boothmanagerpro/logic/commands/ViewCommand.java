package seedu.boothmanagerpro.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.boothmanagerpro.commons.util.ToStringBuilder;
import seedu.boothmanagerpro.logic.Messages;
import seedu.boothmanagerpro.logic.commands.exceptions.CommandException;
import seedu.boothmanagerpro.model.Model;
import seedu.boothmanagerpro.model.person.Name;
import seedu.boothmanagerpro.model.person.NameContainsKeywordsPredicate;
import seedu.boothmanagerpro.model.person.Person;

/**
 * Displays contacts using the same name-keyword matching as find, then prompts for a choice when needed.
 */
public class ViewCommand extends Command {
    public static final String COMMAND_WORD = "view";
    public static final String MESSAGE_USAGE = "view n/NAME";
    public static final String MESSAGE_NO_MATCH = "No exhibitor contact found with the name: %s";
    public static final String MESSAGE_SUCCESS = "Exhibitor contact found:\n%s";

    private final Name name;

    /**
     * Creates a command that matches any whole word in the given name query, ignoring case.
     */
    public ViewCommand(Name name) {
        this.name = requireNonNull(name);
    }

    /**
     * Filters the contact list and returns either one contact's details or a numbered selection prompt.
     *
     * @throws CommandException If no contact matches the name query.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        NameContainsKeywordsPredicate matchesName =
                new NameContainsKeywordsPredicate(List.of(name.fullName.strip().split("\\s+")));
        List<Person> matches = model.getAddressBook().getPersonList().stream().filter(matchesName).toList();
        if (matches.isEmpty()) {
            throw new CommandException(String.format(MESSAGE_NO_MATCH, name));
        }
        // An exact full name picks out its contact, even when other names share a word with it.
        List<Person> exactMatches = matches.stream()
                .filter(person -> person.getName().fullName.equalsIgnoreCase(name.fullName)).toList();
        if (exactMatches.size() == 1) {
            Person exactMatch = exactMatches.getFirst();
            model.updateFilteredPersonList(person -> person.equals(exactMatch));
            return createDetailsResult(exactMatch);
        }
        model.updateFilteredPersonList(matchesName);
        if (matches.size() == 1) {
            return createDetailsResult(matches.getFirst());
        }
        // Number the choices in displayed order so they agree with the indices used by edit, update and delete.
        return createSelectionResult(List.copyOf(model.getFilteredPersonList()));
    }

    /**
     * Creates a numbered prompt listing the matching contacts in their displayed order.
     */
    private CommandResult createSelectionResult(List<Person> matches) {
        assert matches.size() > 1 : "A selection prompt requires multiple matching contacts";
        StringBuilder choices = new StringBuilder(matches.size() + " contacts matching \"" + name + "\" found:\n");
        for (int i = 0; i < matches.size(); i++) {
            Person person = matches.get(i);
            choices.append(i + 1).append(". ").append(person.getName()).append(" at ")
                    .append(Messages.formatOptional(person.getCompany())).append("\n");
        }
        choices.append("Please input the corresponding number (1 to ").append(matches.size())
                .append(") for the contact you would like to view.");
        return new CommandResult(choices.toString(), matches);
    }

    /**
     * Returns the documented details of a selected exhibitor contact.
     */
    public static CommandResult createDetailsResult(Person person) {
        requireNonNull(person);
        return CommandResult.showingContact(
                String.format(MESSAGE_SUCCESS, Messages.formatContactDetails(person)), person);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ViewCommand otherCommand && name.equals(otherCommand.name);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("name", name).toString();
    }
}
