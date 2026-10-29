package seedu.boothmanagerpro.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.boothmanagerpro.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.boothmanagerpro.logic.commands.CommandResult;
import seedu.boothmanagerpro.logic.commands.HelpCommand;
import seedu.boothmanagerpro.logic.commands.ViewCommand;
import seedu.boothmanagerpro.logic.commands.exceptions.CommandException;
import seedu.boothmanagerpro.logic.parser.exceptions.ParseException;
import seedu.boothmanagerpro.model.AddressBook;
import seedu.boothmanagerpro.model.ModelManager;
import seedu.boothmanagerpro.model.UserPrefs;
import seedu.boothmanagerpro.model.person.Person;
import seedu.boothmanagerpro.storage.JsonAddressBookStorage;
import seedu.boothmanagerpro.testutil.IntegrationTestFixture;
import seedu.boothmanagerpro.testutil.PersonBuilder;

/**
 * Exercises view requests and numbered replies using persisted exhibitor records.
 */
public class ViewCommandIntegrationTest {
    @TempDir
    public Path folder;

    private final Person first = new PersonBuilder().withName("Alicia Tan").withCompany("TechNova Pte Ltd")
            .withEmail("alicia@technova.com").withContactMethod("EMAIL").withTags("high-priority").build();
    private final Person second = new PersonBuilder(first).withCompany("TechIndustries Pte Ltd")
            .withEmail("alicia@techindustries.com").withContactMethod("phone").build();
    private ModelManager model;
    private Logic logic;
    private JsonAddressBookStorage addressStorage;

    @BeforeEach
    public void setUp() throws Exception {
        AddressBook book = new AddressBook();
        book.setPersons(List.of(first, second));
        addressStorage = new JsonAddressBookStorage(folder.resolve("contacts.json"));
        addressStorage.saveAddressBook(book);
        model = new ModelManager(addressStorage.readAddressBook().orElseThrow(), new UserPrefs());
        logic = IntegrationTestFixture.createLogic(model, addressStorage, folder.resolve("prefs.json"));
    }

    @Test
    public void execute_viewAndNumberedReply_displaysAndFiltersToChosenRecordWithoutWriting() throws Exception {
        String originalFile = Files.readString(folder.resolve("contacts.json"));
        model.updateFilteredPersonList(person -> false);
        assertEquals(List.of(first, second), logic.execute("view n/alicia tan").getViewChoices());
        assertEquals(ViewCommand.createDetailsResult(second), logic.execute("2"));
        assertEquals(List.of(second), model.getFilteredPersonList());
        assertEquals(List.of(first, second), model.getAddressBook().getPersonList());
        assertEquals(originalFile, Files.readString(folder.resolve("contacts.json")));
        assertThrows(ParseException.class, Messages.MESSAGE_UNKNOWN_COMMAND, () -> logic.execute("2"));
    }

    @Test
    public void execute_viewSelectionThenUpdate_updatesOnlySelectedContact() throws Exception {
        List<Person> contacts = setUpOverlappingViewChoices();
        Person alicia = contacts.get(0);
        Person johnGreenWorks = contacts.get(1);
        Person johnBlueSky = contacts.get(2);

        assertEquals(contacts, logic.execute("view n/John Tan").getViewChoices());
        assertEquals(ViewCommand.createDetailsResult(johnGreenWorks), logic.execute("2"));
        assertEquals(List.of(johnGreenWorks), model.getFilteredPersonList());

        logic.execute("update 1 p/90000001");

        Person editedJohn = new PersonBuilder(johnGreenWorks).withPhone("90000001").build();
        assertEquals(List.of(alicia, editedJohn, johnBlueSky), model.getAddressBook().getPersonList());
        assertEquals(List.of(alicia, editedJohn, johnBlueSky),
                addressStorage.readAddressBook().orElseThrow().getPersonList());
    }

    @Test
    public void execute_viewSelectionThenDelete_deletesOnlySelectedContact() throws Exception {
        List<Person> contacts = setUpOverlappingViewChoices();
        Person alicia = contacts.get(0);
        Person johnGreenWorks = contacts.get(1);
        Person johnBlueSky = contacts.get(2);

        assertEquals(contacts, logic.execute("view n/John Tan").getViewChoices());
        assertEquals(ViewCommand.createDetailsResult(johnGreenWorks), logic.execute("2"));
        assertEquals(List.of(johnGreenWorks), model.getFilteredPersonList());

        logic.execute("delete 1");

        assertEquals(List.of(alicia, johnBlueSky), model.getAddressBook().getPersonList());
        assertEquals(List.of(alicia, johnBlueSky), addressStorage.readAddressBook().orElseThrow().getPersonList());
    }

    @Test
    public void execute_invalidReplies_keepChoicesForRetry() throws Exception {
        String prompt = logic.execute("view n/Alicia Tan").getFeedbackToUser();
        for (String input : new String[]{"0", "-1", "3", "2147483648", "1.5", "abc", "1 2"}) {
            // The numbered choices are repeated so the user can see which number to retry with.
            String expected = String.format(LogicManager.MESSAGE_VIEW_SELECTION, 2) + "\n" + prompt;
            assertThrows(CommandException.class, expected, () -> logic.execute(input));
        }
        assertEquals(ViewCommand.createDetailsResult(first), logic.execute("1"));
    }

    @Test
    public void execute_anotherCommand_cancelsPendingChoices() throws Exception {
        logic.execute("view n/Alicia Tan");
        logic.execute("list");
        assertFalse(logic.isAwaitingViewSelection());
        assertThrows(ParseException.class, Messages.MESSAGE_UNKNOWN_COMMAND, () -> logic.execute("1"));
    }

    @Test
    public void execute_helpDuringPendingChoices_preservesPromptAndSelection() throws Exception {
        String originalFile = Files.readString(folder.resolve("contacts.json"));
        String prompt = logic.execute("view n/Alicia Tan").getFeedbackToUser();
        assertTrue(logic.isAwaitingViewSelection());

        CommandResult helpResult = logic.execute("help");
        assertTrue(helpResult.getFeedbackToUser().startsWith(HelpCommand.SHOWING_HELP_MESSAGE));
        assertTrue(helpResult.getFeedbackToUser().endsWith("\n\n" + prompt));
        assertTrue(logic.isAwaitingViewSelection());

        assertEquals(ViewCommand.createDetailsResult(second), logic.execute("2"));
        assertFalse(logic.isAwaitingViewSelection());
        assertEquals(originalFile, Files.readString(folder.resolve("contacts.json")));
    }

    @Test
    public void execute_newViewWithoutMatches_cancelsOldChoices() throws Exception {
        logic.execute("view n/Alicia Tan");
        assertThrows(CommandException.class, () -> logic.execute("view n/Nobody"));
        assertThrows(ParseException.class, Messages.MESSAGE_UNKNOWN_COMMAND, () -> logic.execute("1"));
    }

    @Test
    public void execute_contactChangesDuringSelection_rejectsStaleChoice() throws Exception {
        logic.execute("view n/Alicia Tan");
        model.deletePerson(second);
        assertThrows(CommandException.class, LogicManager.MESSAGE_VIEW_CONTACT_CHANGED, () -> logic.execute("2"));
        assertThrows(ParseException.class, Messages.MESSAGE_UNKNOWN_COMMAND, () -> logic.execute("1"));
    }

    @Test
    public void execute_updateExistingField_preservesExhibitorFieldsAcrossSave() throws Exception {
        logic.execute("update 1 p/91234567");
        Person reloaded = addressStorage.readAddressBook().orElseThrow().getPersonList().getFirst();
        assertEquals(first.getCompany(), reloaded.getCompany());
        assertEquals("email", reloaded.getContactMethod().orElseThrow().toString());
        assertEquals("91234567", reloaded.getPhone().value);
    }

    @Test
    public void execute_punctuationInName_isViewable() throws Exception {
        Person person = new PersonBuilder(first).withName("Anne-Marie O'Neil").build();
        model.setPerson(first, person);
        assertEquals(ViewCommand.createDetailsResult(person), logic.execute("view n/anne-marie o'neil"));
    }

    @Test
    public void execute_updateToOtherContactsEmail_rejectsDuplicateAndPreservesData() {
        assertThrows(CommandException.class, () -> logic.execute("update 1 e/alicia@techindustries.com"));
        assertEquals(List.of(first, second), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_sameNameAfterSortByCompany_choicesMatchDisplayedIndices() throws Exception {
        List<Person> contacts = setUpOverlappingViewChoices();
        Person alicia = contacts.get(0);
        Person johnGreenWorks = contacts.get(1);
        Person johnBlueSky = contacts.get(2);
        logic.execute("list o/company");

        CommandResult result = logic.execute("view n/John Tan");
        // Sorted by company, so the numbering differs from the stored order of alicia, johnGreenWorks, johnBlueSky.
        assertEquals(List.of(johnBlueSky, johnGreenWorks, alicia), result.getViewChoices());
        assertEquals(result.getViewChoices(), model.getFilteredPersonList());

        logic.execute("update 1 s/Completed");
        Person updatedJohn = new PersonBuilder(johnBlueSky).withEnquiryStatus("Completed").build();
        assertEquals(List.of(alicia, johnGreenWorks, updatedJohn), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_viewNameWithPatronymic_doesNotMatchUnrelatedPatronymicContacts() throws Exception {
        logic.execute("add n/Raj s/o Kumar c/TechNova e/raj@example.com p/91234567");
        logic.execute("add n/Arun s/o Mohan c/Other e/arun@example.com p/92345678");
        logic.execute("add n/Devi d/o Raman c/Other2 e/devi@example.com p/93456789");

        // Exact match for Raj s/o Kumar displays Raj's card immediately without showing selection choices for Arun
        CommandResult result = logic.execute("view n/Raj s/o Kumar");
        assertEquals("Exhibitor contact found:\nRaj s/o Kumar at TechNova\nEmail: raj@example.com\n"
                + "Phone: 91234567\nContact method: Not specified\nEnquiry status: Needs follow-up\n"
                + "Follow-up date: None\nTags: None", result.getFeedbackToUser());
        assertTrue(result.getViewChoices().isEmpty());

        // Searching for a non-existent name with s/o does not match Arun s/o Mohan
        String expectedMessage = String.format(ViewCommand.MESSAGE_NO_MATCH, "Suresh s/o Velu");
        assertThrows(CommandException.class, expectedMessage, () -> logic.execute("view n/Suresh s/o Velu"));
    }

    private List<Person> setUpOverlappingViewChoices() {
        Person alicia = new PersonBuilder().withName("Alicia Tan").withCompany("TechNova")
                .withEmail("alicia@technova.com").withPhone("91234567").build();
        Person johnGreenWorks = new PersonBuilder().withName("John Tan").withCompany("GreenWorks")
                .withEmail("john1@gw.com").withPhone("81234567").build();
        Person johnBlueSky = new PersonBuilder().withName("John Tan").withCompany("BlueSky")
                .withEmail("john2@bs.com").withPhone("81234568").build();
        List<Person> contacts = List.of(alicia, johnGreenWorks, johnBlueSky);
        AddressBook book = new AddressBook();
        book.setPersons(contacts);
        model.setAddressBook(book);
        return contacts;
    }
}
