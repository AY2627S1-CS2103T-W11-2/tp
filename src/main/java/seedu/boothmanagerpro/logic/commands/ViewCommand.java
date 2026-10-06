package seedu.boothmanagerpro.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import seedu.boothmanagerpro.commons.util.ToStringBuilder;
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
        model.updateFilteredPersonList(matchesName);
        if (matches.size() == 1) {
            return createDetailsResult(matches.getFirst());
        }
        return createSelectionResult(matches);
    }

    /**
     * Creates a numbered prompt and preserves the matching contacts in their displayed order.
     */
    private CommandResult createSelectionResult(List<Person> matches) {
        assert matches.size() > 1 : "A selection prompt requires multiple matching contacts";
        StringBuilder choices = new StringBuilder(matches.size() + " contacts matching \"" + name + "\" found:\n");
        for (int i = 0; i < matches.size(); i++) {
            Person person = matches.get(i);
            choices.append(i + 1).append(". ").append(person.getName()).append(" at ")
                    .append(displayOptional(person.getCompany())).append("\n");
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
        String tags = person.getTags().stream().map(tag -> tag.tagName).sorted().collect(Collectors.joining(", "));
        String details = person.getName() + " at " + displayOptional(person.getCompany())
                + "\nEmail: " + person.getEmail()
                + "\nPhone: " + person.getPhone()
                + "\nContact method: " + displayOptional(person.getContactMethod())
                + "\nTags: " + (tags.isEmpty() ? "None" : tags);
        return new CommandResult(String.format(MESSAGE_SUCCESS, details), person);
    }

    /**
     * Returns the field value or a placeholder for an omitted legacy field.
     */
    private static String displayOptional(Optional<?> value) {
        return value.map(Object::toString).orElse("Not specified");
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
