package seedu.boothmanagerpro.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.boothmanagerpro.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.boothmanagerpro.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.boothmanagerpro.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.boothmanagerpro.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.boothmanagerpro.model.AddressBook;
import seedu.boothmanagerpro.model.Model;
import seedu.boothmanagerpro.model.ModelManager;
import seedu.boothmanagerpro.model.UserPrefs;
import seedu.boothmanagerpro.model.person.Company;
import seedu.boothmanagerpro.model.person.ContactMethod;
import seedu.boothmanagerpro.model.person.Email;
import seedu.boothmanagerpro.model.person.Name;
import seedu.boothmanagerpro.model.person.Person;
import seedu.boothmanagerpro.model.person.Phone;
import seedu.boothmanagerpro.model.tag.Tag;
import seedu.boothmanagerpro.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for ListCommand.
 */
public class ListCommandTest {

    private Model model;
    private Model expectedModel;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_listIsNotFiltered_showsSameList() {
        assertCommandSuccess(new ListCommand(), model,
                ListCommand.formatContactList(expectedModel.getFilteredPersonList()), expectedModel);
    }

    @Test
    public void execute_listIsFiltered_showsEverything() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandSuccess(new ListCommand(), model,
                ListCommand.formatContactList(expectedModel.getFilteredPersonList()), expectedModel);
    }

    @Test
    public void execute_emptyAddressBook_showsNoContactsMessage() {
        Model emptyModel = new ModelManager(new AddressBook(), new UserPrefs());
        assertCommandSuccess(new ListCommand(), emptyModel,
                "No contacts found.\nUse the 'add' command to add a new exhibitor contact.",
                new ModelManager(new AddressBook(), new UserPrefs()));
    }

    @Test
    public void formatContactList_exhibitors_showsDocumentedFormat() {
        Person alicia = createExhibitor("Alicia Tan", "TechNova Pte Ltd", "alicia@technova.com", "91234567",
                ContactMethod.EMAIL, "technology", "high-priority");
        Person john = createExhibitor("John Smith", "GlobalTech Inc", "john@globaltech.com", "+65-6234-5678",
                ContactMethod.PHONE, "hardware");

        String divider = "──────────────────────────────────────";
        String expected = "Exhibitor Contact List\n"
                + divider + "\n\n"
                + "Alicia Tan at TechNova Pte Ltd\nEmail: alicia@technova.com\nPhone: 91234567\n"
                + "Contact method: email\nTags: technology, high-priority\n\n"
                + "John Smith at GlobalTech Inc\nEmail: john@globaltech.com\nPhone: +6562345678\n"
                + "Contact method: phone\nTags: hardware\n\n"
                + divider + "\n"
                + "Total contacts: 2";
        assertEquals(expected, ListCommand.formatContactList(List.of(alicia, john)));
    }

    @Test
    public void formatContactList_omittedFields_showsPlaceholders() {
        Person legacy = new PersonBuilder().withTags().build();
        String result = ListCommand.formatContactList(List.of(legacy));
        assertEquals("Exhibitor Contact List\n" + ListCommand.MESSAGE_DIVIDER + "\n\n"
                + legacy.getName() + " at Not specified\nEmail: " + legacy.getEmail()
                + "\nPhone: " + legacy.getPhone() + "\nContact method: Not specified\nTags: None\n\n"
                + ListCommand.MESSAGE_DIVIDER + "\nTotal contacts: 1", result);
    }

    private static Person createExhibitor(String name, String company, String email, String phone,
            ContactMethod contactMethod, String... tags) {
        Set<Tag> tagSet = new LinkedHashSet<>();
        for (String tag : tags) {
            tagSet.add(new Tag(tag));
        }
        return Person.createExhibitor(new Name(name), new Company(company), new Email(email), new Phone(phone),
                Optional.of(contactMethod), tagSet);
    }
}
