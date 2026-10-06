package seedu.boothmanagerpro.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.boothmanagerpro.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.boothmanagerpro.logic.commands.ViewCommand;
import seedu.boothmanagerpro.logic.commands.exceptions.CommandException;
import seedu.boothmanagerpro.logic.parser.exceptions.ParseException;
import seedu.boothmanagerpro.model.AddressBook;
import seedu.boothmanagerpro.model.ModelManager;
import seedu.boothmanagerpro.model.UserPrefs;
import seedu.boothmanagerpro.model.person.Person;
import seedu.boothmanagerpro.storage.JsonAddressBookStorage;
import seedu.boothmanagerpro.storage.JsonUserPrefsStorage;
import seedu.boothmanagerpro.storage.StorageManager;
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
        logic = new LogicManager(model, new StorageManager(addressStorage,
                new JsonUserPrefsStorage(folder.resolve("prefs.json"))));
    }

    @Test
    public void execute_viewAndNumberedReply_displaysChosenRecordAndFiltersWithoutWriting() throws Exception {
        String originalFile = Files.readString(folder.resolve("contacts.json"));
        model.updateFilteredPersonList(person -> false);
        assertEquals(List.of(first, second), logic.execute("view n/alicia tan").getViewChoices());
        assertEquals(ViewCommand.createDetailsResult(second), logic.execute("2"));
        assertEquals(List.of(first, second), model.getFilteredPersonList());
        assertEquals(List.of(first, second), model.getAddressBook().getPersonList());
        assertEquals(originalFile, Files.readString(folder.resolve("contacts.json")));
        assertThrows(ParseException.class, Messages.MESSAGE_UNKNOWN_COMMAND, () -> logic.execute("2"));
    }

    @Test
    public void execute_invalidReplies_keepChoicesForRetry() throws Exception {
        logic.execute("view n/Alicia Tan");
        for (String input : new String[]{"0", "-1", "3", "2147483648", "1.5", "abc", "1 2"}) {
            String expected = String.format(LogicManager.MESSAGE_VIEW_SELECTION, 2);
            assertThrows(CommandException.class, expected, () -> logic.execute(input));
        }
        assertEquals(ViewCommand.createDetailsResult(first), logic.execute("1"));
    }

    @Test
    public void execute_anotherCommand_cancelsPendingChoices() throws Exception {
        logic.execute("view n/Alicia Tan");
        logic.execute("list");
        assertThrows(ParseException.class, Messages.MESSAGE_UNKNOWN_COMMAND, () -> logic.execute("1"));
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
    public void execute_editExistingField_preservesExhibitorFieldsAcrossSave() throws Exception {
        logic.execute("edit 1 p/91234567");
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
    public void execute_editToOtherContactsEmail_rejectsDuplicateAndPreservesData() {
        assertThrows(CommandException.class, () -> logic.execute("edit 1 e/alicia@techindustries.com"));
        assertEquals(List.of(first, second), model.getAddressBook().getPersonList());
    }
}
