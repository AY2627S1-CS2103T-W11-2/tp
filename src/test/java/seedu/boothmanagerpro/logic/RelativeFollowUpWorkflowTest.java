package seedu.boothmanagerpro.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.boothmanagerpro.logic.commands.exceptions.CommandException;
import seedu.boothmanagerpro.logic.parser.exceptions.ParseException;
import seedu.boothmanagerpro.model.ModelManager;
import seedu.boothmanagerpro.model.ReadOnlyAddressBook;
import seedu.boothmanagerpro.model.UserPrefs;
import seedu.boothmanagerpro.model.person.Person;
import seedu.boothmanagerpro.storage.JsonAddressBookStorage;
import seedu.boothmanagerpro.testutil.IntegrationTestFixture;

/** Checks relative date filters across mutations, failed saves, view selection and storage reloads. */
public class RelativeFollowUpWorkflowTest {
    @TempDir
    public Path folder;
    private final LocalDate today = LocalDate.now();
    private JsonAddressBookStorage storage;
    private ModelManager model;
    private LogicManager logic;

    @BeforeEach
    public void setUp() throws Exception {
        storage = new JsonAddressBookStorage(folder.resolve("contacts.json"));
        model = new ModelManager();
        logic = IntegrationTestFixture.createLogic(model, storage, folder.resolve("prefs.json"));
        logic.execute("add n/Cora Lim c/Acme e/cora@example.com p/91234567 s/Completed f/" + date(1));
        logic.execute("add n/Ben Lim c/Acme e/ben@example.com p/81234567 s/In progress f/" + date(0) + " t/vip");
        logic.execute("add n/Dora Lim c/Acme e/dora@example.com p/82345678");
        logic.execute("add n/Alicia Tan c/Acme e/alicia@example.com p/83456789 f/" + date(-1));
    }

    @Test
    public void addAndUpdate_relativeKeywords_preserveExistingContactsAndFilter() throws Exception {
        logic.execute("find f/today");
        List<Person> all = List.copyOf(model.getAddressBook().getPersonList());
        List<Person> displayed = List.copyOf(model.getFilteredPersonList());
        String json = Files.readString(storage.getAddressBookFilePath());
        for (String keyword : List.of("today", "due", "overdue")) {
            assertThrows(ParseException.class, () -> logic.execute(
                    "add n/Extra Contact c/Acme e/extra@example.com p/84567890 f/" + keyword));
            assertThrows(ParseException.class, () -> logic.execute("update 1 f/" + keyword));
            assertEquals(all, model.getAddressBook().getPersonList());
            assertEquals(displayed, model.getFilteredPersonList());
            assertEquals(json, Files.readString(storage.getAddressBookFilePath()));
        }
    }

    @Test
    public void find_invalidRelativeValues_preservesPreviousDateResults() throws Exception {
        logic.execute("find f/due");
        List<Person> displayed = List.copyOf(model.getFilteredPersonList());
        String json = Files.readString(storage.getAddressBookFilePath());
        for (String value : List.of("tomorrow", "over-due", "today extra", "due soon", "before:" + date(0))) {
            assertThrows(ParseException.class, () -> logic.execute("find f/" + value));
            assertEquals(displayed, model.getFilteredPersonList());
            assertEquals(json, Files.readString(storage.getAddressBookFilePath()));
        }
    }

    @Test
    public void find_sortedDueResults_updatesContactAtDisplayedIndex() throws Exception {
        logic.execute("list o/name");
        logic.execute("find f/due");
        assertNames("Alicia Tan", "Ben Lim");
        logic.execute("update 2 p/89999999");
        assertEquals("89999999", saved("Ben Lim").getPhone().value);
        assertEquals("83456789", saved("Alicia Tan").getPhone().value);
        logic.execute("find f/due");
        assertNames("Alicia Tan", "Ben Lim");
        assertEquals(4, storage.readAddressBook().orElseThrow().getPersonList().size());
    }

    @Test
    public void failedSave_restoresRelativeFilterOrderAndStoredBytes() throws Exception {
        logic.execute("list o/name");
        logic.execute("find f/due");
        List<Person> displayed = List.copyOf(model.getFilteredPersonList());
        List<Person> all = List.copyOf(model.getAddressBook().getPersonList());
        String json = Files.readString(storage.getAddressBookFilePath());
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(storage.getAddressBookFilePath()) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw new IOException("Simulated save failure");
            }
        };
        LogicManager failingLogic = IntegrationTestFixture.createLogic(
                model, failingStorage, folder.resolve("failing-prefs.json"));
        assertThrows(CommandException.class, () -> failingLogic.execute("update 1 f/" + date(1)));
        assertEquals(all, model.getAddressBook().getPersonList());
        assertEquals(displayed, model.getFilteredPersonList());
        assertEquals(json, Files.readString(storage.getAddressBookFilePath()));
        assertNames("Alicia Tan", "Ben Lim");
    }

    @Test
    public void dateUpdateAndClearing_surviveReloadAndRefreshRelativeSearch() throws Exception {
        logic.execute("find f/today");
        assertNames("Ben Lim");
        logic.execute("update 1 f/" + date(1));
        logic.execute("find f/overdue");
        logic.execute("update 1 f/");
        model = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        logic = IntegrationTestFixture.createLogic(model, storage, folder.resolve("reloaded-prefs.json"));
        logic.execute("find f/due");
        assertNames();
        logic.execute("find f/" + date(1));
        assertNames("Cora Lim", "Ben Lim");
        assertEquals("In progress", saved("Ben Lim").getEnquiryStatus().toString());
        assertEquals(1, saved("Ben Lim").getTags().size());
    }

    @Test
    public void dateAndStatusCriteria_selectIndependently() throws Exception {
        logic.execute("update Cora Lim f/" + date(-1));
        logic.execute("find f/overdue");
        assertNames("Cora Lim", "Alicia Tan");
        logic.execute("find f/overdue s/Needs follow-up");
        assertNames("Alicia Tan");
        logic.execute("find f/due s/Completed");
        assertNames("Cora Lim");
    }

    @Test
    public void relativeSearch_cancelsPendingViewChoice() throws Exception {
        logic.execute("view n/Lim");
        String json = Files.readString(storage.getAddressBookFilePath());
        logic.execute("find f/today");
        assertNames("Ben Lim");
        assertThrows(ParseException.class, () -> logic.execute("2"));
        assertNames("Ben Lim");
        assertEquals(json, Files.readString(storage.getAddressBookFilePath()));
    }

    @Test
    public void tagAfterRelativeSearch_changesOnlyDisplayedContact() throws Exception {
        logic.execute("find f/overdue");
        assertNames("Alicia Tan");
        logic.execute("tag 1 t/urgent");
        assertEquals(List.of("urgent"), saved("Alicia Tan").getTags().stream().map(tag -> tag.tagName).toList());
        assertEquals(List.of("vip"), saved("Ben Lim").getTags().stream().map(tag -> tag.tagName).toList());
        logic.execute("find f/due t/urgent");
        assertNames("Alicia Tan");
    }

    private String date(int offset) {
        return today.plusDays(offset).format(DateTimeFormatter.BASIC_ISO_DATE);
    }

    private Person saved(String name) throws Exception {
        return storage.readAddressBook().orElseThrow().getPersonList().stream()
                .filter(person -> person.getName().fullName.equals(name)).findFirst().orElseThrow();
    }

    private void assertNames(String... names) {
        assertEquals(List.of(names), model.getFilteredPersonList().stream()
                .map(person -> person.getName().fullName).toList());
    }
}
