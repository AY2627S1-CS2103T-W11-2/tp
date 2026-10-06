package seedu.boothmanagerpro.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_COMPANY;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_METHOD;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.Optional;
import java.util.stream.Collectors;

import seedu.boothmanagerpro.commons.util.ToStringBuilder;
import seedu.boothmanagerpro.logic.commands.exceptions.CommandException;
import seedu.boothmanagerpro.model.Model;
import seedu.boothmanagerpro.model.person.Person;

/**
 * Adds an exhibitor contact to the address book.
 */
public class AddCommand extends Command {

    public static final String COMMAND_WORD = "add";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds an exhibitor contact. "
            + "Parameters: "
            + PREFIX_NAME + "NAME "
            + PREFIX_COMPANY + "COMPANY "
            + PREFIX_EMAIL + "EMAIL "
            + PREFIX_PHONE + "PHONE "
            + "[" + PREFIX_METHOD + "METHOD] "
            + "[" + PREFIX_TAG + "TAG]...\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_NAME + "Alicia Tan "
            + PREFIX_COMPANY + "TechNova Pte Ltd "
            + PREFIX_EMAIL + "alicia@technova.com "
            + PREFIX_PHONE + "91234567 "
            + PREFIX_METHOD + "email "
            + PREFIX_TAG + "technology "
            + PREFIX_TAG + "high-priority";

    public static final String MESSAGE_SUCCESS = "New exhibitor contact added:\n%1$s";
    public static final String MESSAGE_DUPLICATE_PERSON = "This contact may already exist: %s.\n"
            + "Use the edit command if you want to update the existing contact.";

    private final Person toAdd;

    /**
     * Creates an AddCommand to add the specified {@code Person}
     */
    public AddCommand(Person person) {
        requireNonNull(person);
        toAdd = person;
    }

    /**
     * Checks all stored contacts, including those hidden by a filter, before adding the contact.
     * A duplicate error identifies the existing record and leaves the model unchanged.
     * Persistence is performed by LogicManager after successful execution.
     *
     * @throws CommandException If an existing contact matches the duplicate rule.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        Optional<Person> duplicate = findDuplicate(model);
        if (duplicate.isPresent()) {
            throw new CommandException(String.format(MESSAGE_DUPLICATE_PERSON, formatIdentity(duplicate.get())));
        }

        model.addPerson(toAdd);
        return new CommandResult(String.format(MESSAGE_SUCCESS, formatContact(toAdd)));
    }

    /** Finds the existing match in one pass over the full collection, independently of the UI filter. */
    private Optional<Person> findDuplicate(Model model) {
        return model.getAddressBook().getPersonList().stream().filter(toAdd::isSamePerson).findFirst();
    }

    /** Formats contact identity for successful additions and duplicate warnings. */
    public static String formatIdentity(Person person) {
        return person.getName() + person.getCompany().map(company -> " at " + company.value).orElse("");
    }

    /** Formats the new exhibitor fields without changing other commands' output. */
    public static String formatContact(Person person) {
        String tags = person.getTags().stream().map(tag -> tag.tagName).collect(Collectors.joining(", "));
        return formatIdentity(person) + "\nEmail: " + person.getEmail() + "\nPhone: " + person.getPhone()
                + "\nContact method: " + person.getContactMethod().map(Object::toString).orElse("Not specified")
                + "\nTags: " + (tags.isEmpty() ? "None" : tags);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddCommand otherAddCommand)) {
            return false;
        }

        return toAdd.equals(otherAddCommand.toAdd);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("toAdd", toAdd)
                .toString();
    }
}
