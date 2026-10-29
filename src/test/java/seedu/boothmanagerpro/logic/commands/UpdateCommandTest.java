package seedu.boothmanagerpro.logic.commands;

import static seedu.boothmanagerpro.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.boothmanagerpro.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.boothmanagerpro.testutil.Assert.assertThrows;
import static seedu.boothmanagerpro.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.boothmanagerpro.testutil.TypicalPersons.BENSON;
import static seedu.boothmanagerpro.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.boothmanagerpro.commons.core.index.Index;
import seedu.boothmanagerpro.logic.Messages;
import seedu.boothmanagerpro.model.Model;
import seedu.boothmanagerpro.model.ModelManager;
import seedu.boothmanagerpro.model.UserPrefs;
import seedu.boothmanagerpro.model.person.Company;
import seedu.boothmanagerpro.model.person.ContactUpdate;
import seedu.boothmanagerpro.model.person.Email;
import seedu.boothmanagerpro.model.person.EnquiryStatus;
import seedu.boothmanagerpro.model.person.Name;
import seedu.boothmanagerpro.model.person.Person;
import seedu.boothmanagerpro.testutil.PersonBuilder;

public class UpdateCommandTest {

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    private static ContactUpdate statusAndCompanyUpdate(String company, EnquiryStatus status) {
        return new ContactUpdate(
                Optional.empty(),
                Optional.of(new Company(company)),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(status),
                Optional.empty(),
                Optional.empty());
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        ContactUpdate update = statusAndCompanyUpdate("Acme Corp", EnquiryStatus.IN_PROGRESS);
        assertThrows(NullPointerException.class, () -> new UpdateCommand((Index) null, update));
        assertThrows(NullPointerException.class, () -> new UpdateCommand((Name) null, update));
        assertThrows(NullPointerException.class, () -> new UpdateCommand(INDEX_FIRST_PERSON, null));
        assertThrows(NullPointerException.class, () -> new UpdateCommand(new Name("Alice Pauline"), null));
    }

    @Test
    public void execute_validIndexCompanyEndingWithPeriod_updatesWithoutDoubleFullStop() {
        Person target = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        ContactUpdate changes = statusAndCompanyUpdate("Acme Pte. Ltd.", EnquiryStatus.IN_PROGRESS);
        UpdateCommand updateCommand = new UpdateCommand(INDEX_FIRST_PERSON, changes);

        Person updatedPerson = new PersonBuilder(target)
                .withCompany("Acme Pte. Ltd.")
                .withEnquiryStatus("In progress")
                .build();
        String expectedMessage = "Updated " + AddCommand.formatIdentity(updatedPerson);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(target, updatedPerson);

        assertCommandSuccess(updateCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_validNameCaseInsensitive_updatesSuccess() {
        Person target = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        ContactUpdate changes = statusAndCompanyUpdate("TechNova", EnquiryStatus.COMPLETED);
        UpdateCommand updateCommand = new UpdateCommand(new Name("alice pauline"), changes);

        Person updatedPerson = new PersonBuilder(target)
                .withCompany("TechNova")
                .withEnquiryStatus("Completed")
                .build();
        String expectedMessage = "Updated " + AddCommand.formatIdentity(updatedPerson) + ".";

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(target, updatedPerson);

        assertCommandSuccess(updateCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndex_throwsCommandException() {
        Index outOfBound = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        UpdateCommand updateCommand = new UpdateCommand(
                outOfBound, statusAndCompanyUpdate("TechNova", EnquiryStatus.IN_PROGRESS));

        assertCommandFailure(updateCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_nameNotFound_throwsCommandException() {
        Name unknown = new Name("Unknown Contact");
        UpdateCommand updateCommand = new UpdateCommand(
                unknown, statusAndCompanyUpdate("TechNova", EnquiryStatus.IN_PROGRESS));

        assertCommandFailure(updateCommand, model,
                String.format(DeleteCommand.MESSAGE_PERSON_NOT_FOUND, unknown));
    }

    @Test
    public void execute_ambiguousName_throwsCommandException() {
        Person secondAlice = new PersonBuilder()
                .withName("Alice Pauline")
                .withCompany("Second Co")
                .withEmail("alice2@second.com")
                .withPhone("82223333")
                .build();
        model.addPerson(secondAlice);

        Name ambiguous = new Name("Alice Pauline");
        UpdateCommand updateCommand = new UpdateCommand(
                ambiguous, statusAndCompanyUpdate("TechNova", EnquiryStatus.IN_PROGRESS));

        assertCommandFailure(updateCommand, model,
                String.format(UpdateCommand.MESSAGE_AMBIGUOUS, ambiguous));
    }

    @Test
    public void execute_duplicateEmailConflict_throwsCommandException() {
        ContactUpdate conflictingEmail = new ContactUpdate(
                Optional.empty(),
                Optional.empty(),
                Optional.of(new Email(BENSON.getEmail().value)),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
        UpdateCommand updateCommand = new UpdateCommand(INDEX_FIRST_PERSON, conflictingEmail);

        assertCommandFailure(updateCommand, model,
                String.format(UpdateCommand.MESSAGE_DUPLICATE, AddCommand.formatIdentity(BENSON)));
    }
}

