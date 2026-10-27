package seedu.boothmanagerpro.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import seedu.boothmanagerpro.model.person.SortField;
import seedu.boothmanagerpro.model.tag.Tag;
import seedu.boothmanagerpro.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for ListCommand.
 */
public class ListCommandTest {

    private static final Person ALICIA = new PersonBuilder().withName("Alicia Tan")
            .withCompany("TechNova Pte Ltd").withEmail("alicia@technova.com").build();
    private static final Person DAVID = new PersonBuilder().withName("David Li")
            .withEmail("david@example.com").build();
    private static final Person ZOE = new PersonBuilder().withName("zoe lee")
            .withCompany("Green Goods Collective").withEmail("zoe@greengoods.com").build();

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
                CommandResult.showingContactList(ListCommand.formatContactList(expectedModel.getFilteredPersonList(),
                        ListCommand.MESSAGE_HEADER),
                        expectedModel.getFilteredPersonList()),
                expectedModel);
    }

    @Test
    public void execute_listIsFiltered_showsEverything() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandSuccess(new ListCommand(), model,
                CommandResult.showingContactList(ListCommand.formatContactList(expectedModel.getFilteredPersonList(),
                        ListCommand.MESSAGE_HEADER),
                        expectedModel.getFilteredPersonList()),
                expectedModel);
    }

    @Test
    public void execute_emptyAddressBook_showsNoContactsMessage() {
        Model emptyModel = new ModelManager(new AddressBook(), new UserPrefs());
        assertCommandSuccess(new ListCommand(), emptyModel,
                CommandResult.showingContactList("No contacts found.\n"
                        + "Use the 'add' command to add a new exhibitor contact.", List.of()),
                new ModelManager(new AddressBook(), new UserPrefs()));
    }

    @Test
    public void execute_sortByName_showsContactsSortedByName() {
        Model unsortedModel = modelWith(ZOE, DAVID, ALICIA);
        Model expected = modelWith(ZOE, DAVID, ALICIA);
        expected.updateSortedPersonList(SortField.NAME.getComparator());
        List<Person> sorted = List.of(ALICIA, DAVID, ZOE);
        assertCommandSuccess(new ListCommand(SortField.NAME), unsortedModel,
                CommandResult.showingContactList(ListCommand.formatContactList(sorted,
                        "Exhibitor Contact List (sorted by name)"), sorted), expected);
        assertEquals(sorted, unsortedModel.getFilteredPersonList());
    }

    @Test
    public void execute_sortByCompany_putsContactsWithoutCompanyLast() {
        Model unsortedModel = modelWith(DAVID, ZOE, ALICIA);
        new ListCommand(SortField.COMPANY).execute(unsortedModel);
        assertEquals(List.of(ZOE, ALICIA, DAVID), unsortedModel.getFilteredPersonList());
    }

    @Test
    public void execute_sortByStatus_showsContactsNeedingFollowUpFirst() {
        Person completed = new PersonBuilder(ALICIA).withEnquiryStatus("Completed").build();
        Person inProgress = new PersonBuilder(DAVID).withEnquiryStatus("In progress").build();
        Person needsFollowUp = new PersonBuilder(ZOE).withEnquiryStatus("Needs follow-up").build();
        Person cancelled = new PersonBuilder().withName("Aaron Lee").withEmail("aaron@example.com")
                .withEnquiryStatus("Cancelled").build();
        Model unsortedModel = modelWith(cancelled, completed, inProgress, needsFollowUp);
        CommandResult result = new ListCommand(SortField.STATUS).execute(unsortedModel);
        assertEquals(List.of(needsFollowUp, inProgress, completed, cancelled), unsortedModel.getFilteredPersonList());
        assertTrue(result.getFeedbackToUser().startsWith("Exhibitor Contact List (sorted by status)"));
    }

    @Test
    public void execute_plainListAfterSort_restoresStoredOrder() {
        Model sortedModel = modelWith(ZOE, DAVID, ALICIA);
        new ListCommand(SortField.NAME).execute(sortedModel);
        new ListCommand().execute(sortedModel);
        assertEquals(List.of(ZOE, DAVID, ALICIA), sortedModel.getFilteredPersonList());
    }

    @Test
    public void execute_sortThenDelete_deletesFirstSortedContact() throws Exception {
        Model sortedModel = modelWith(ZOE, DAVID, ALICIA);
        new ListCommand(SortField.NAME).execute(sortedModel);
        new DeleteCommand(INDEX_FIRST_PERSON).execute(sortedModel);
        assertEquals(List.of(ZOE, DAVID), sortedModel.getAddressBook().getPersonList());
        assertEquals(List.of(DAVID, ZOE), sortedModel.getFilteredPersonList());
    }

    @Test
    public void execute_sort_doesNotChangeStoredOrder() {
        Model sortedModel = modelWith(ZOE, DAVID, ALICIA);
        new ListCommand(SortField.NAME).execute(sortedModel);
        assertEquals(List.of(ZOE, DAVID, ALICIA), sortedModel.getAddressBook().getPersonList());
    }

    @Test
    public void equals() {
        ListCommand storedOrder = new ListCommand();
        ListCommand byName = new ListCommand(SortField.NAME);

        assertTrue(storedOrder.equals(storedOrder));
        assertTrue(storedOrder.equals(new ListCommand()));
        assertTrue(byName.equals(new ListCommand(SortField.NAME)));
        assertEquals(byName.hashCode(), new ListCommand(SortField.NAME).hashCode());

        assertFalse(storedOrder.equals(byName));
        assertFalse(byName.equals(new ListCommand(SortField.COMPANY)));
        assertFalse(byName.equals(null));
        assertFalse(byName.equals(1));
    }

    @Test
    public void toStringMethod() {
        assertEquals(ListCommand.class.getCanonicalName() + "{sortField=Optional[name]}",
                new ListCommand(SortField.NAME).toString());
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
                + "1. Alicia Tan at TechNova Pte Ltd\nEmail: alicia@technova.com\nPhone: 91234567\n"
                + "Contact method: email"
                + "\nEnquiry status: Needs follow-up\nFollow-up date: None\nTags: technology, high-priority\n\n"
                + "2. John Smith at GlobalTech Inc\nEmail: john@globaltech.com\nPhone: +6562345678\n"
                + "Contact method: phone"
                + "\nEnquiry status: Needs follow-up\nFollow-up date: None\nTags: hardware\n\n"
                + divider + "\n"
                + "Total contacts: 2";
        assertEquals(expected, ListCommand.formatContactList(List.of(alicia, john), ListCommand.MESSAGE_HEADER));
    }

    @Test
    public void formatContactList_omittedFields_showsPlaceholders() {
        Person legacy = new PersonBuilder().withTags().build();
        String result = ListCommand.formatContactList(List.of(legacy), ListCommand.MESSAGE_HEADER);
        assertEquals("Exhibitor Contact List\n" + ListCommand.MESSAGE_DIVIDER + "\n\n"
                + "1. " + legacy.getName() + " at Not specified\nEmail: " + legacy.getEmail()
                + "\nPhone: " + legacy.getPhone() + "\nContact method: Not specified"
                + "\nEnquiry status: Needs follow-up\nFollow-up date: None\nTags: None\n\n"
                + ListCommand.MESSAGE_DIVIDER + "\nTotal contacts: 1", result);
    }

    private static Model modelWith(Person... persons) {
        AddressBook addressBook = new AddressBook();
        for (Person person : persons) {
            addressBook.addPerson(person);
        }
        return new ModelManager(addressBook, new UserPrefs());
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
