package seedu.boothmanagerpro.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.boothmanagerpro.logic.parser.exceptions.ParseException;
import seedu.boothmanagerpro.model.ModelManager;
import seedu.boothmanagerpro.model.UserPrefs;
import seedu.boothmanagerpro.model.person.Person;
import seedu.boothmanagerpro.storage.JsonAddressBookStorage;
import seedu.boothmanagerpro.testutil.IntegrationTestFixture;
import seedu.boothmanagerpro.testutil.PersonBuilder;

/** Exercises date searches through command routing, sorted indices, updates and actual JSON storage. */
public class FindFollowUpDateIntegrationTest {
    @TempDir
    public Path folder;
    private ModelManager model;
    private LogicManager logic;
    private JsonAddressBookStorage storage;

    @BeforeEach
    public void setUp() throws Exception {
        storage = new JsonAddressBookStorage(folder.resolve("contacts.json"));
        model = new ModelManager();
        logic = createLogic(model);
        logic.execute("add n/Zoe Tan c/TechNova e/zoe@example.com p/91234567 f/20261025 t/vip");
        logic.execute("add n/Alicia Tan c/TechNova e/alicia@example.com p/81234567 f/20261025 t/technology");
        logic.execute("add n/Ben Lim c/GreenWorks e/ben@example.com p/98765432 f/20261026 t/vip");
        logic.execute("add n/No Date c/TechNova e/nodate@example.com p/87654321");
    }

    @Test
    public void find_exactDate_returnsAllMatchesAndExcludesUndatedContacts() throws Exception {
        String output = logic.execute("find f/20261025").getFeedbackToUser();
        assertNames("Zoe Tan", "Alicia Tan");
        assertTrue(output.startsWith("2 contact(s) found.\n\n1. Zoe Tan"));
        assertTrue(output.contains("Follow-up date: 25 Oct 2026"));
        logic.execute("find f/20261026");
        assertNames("Ben Lim"); // Searches all stored contacts, not only the previous results.
        assertEquals("0 contact(s) found.", logic.execute("find f/20261027").getFeedbackToUser());
        assertNames();
        logic.execute("list");
        assertEquals(4, model.getFilteredPersonList().size());
    }

    @Test
    public void find_sameFieldOrDifferentFieldAnd_preservesExistingMatching() throws Exception {
        logic.execute("find f/20261025 f/20261026 f/20261025 t/VIP");
        assertNames("Zoe Tan", "Ben Lim");
        logic.execute("find c/TECHNOVA f/20261025 e/ALICIA@example.com p/8123-4567 n/ALICIA TAN");
        assertNames("Alicia Tan");
        logic.execute("find f/20261026 c/TechNova");
        assertNames();
        logic.execute("find Alicia");
        assertNames("Alicia Tan");
    }

    @Test
    public void find_sortedResults_keepDisplayedIndicesForDateUpdatesAndDeletion() throws Exception {
        logic.execute("list o/name");
        logic.execute("find f/20261025");
        assertNames("Alicia Tan", "Zoe Tan");
        Person original = model.getFilteredPersonList().getFirst();
        logic.execute("update 1 f/20261028");
        Person saved = storage.readAddressBook().orElseThrow().getPersonList().get(1);
        assertEquals(new PersonBuilder(original).withFollowUpDate("20261028").build(), saved);
        logic.execute("find f/20261025");
        assertNames("Zoe Tan");
        logic.execute("delete 1");
        assertEquals(List.of("Alicia Tan", "Ben Lim", "No Date"), storage.readAddressBook().orElseThrow()
                .getPersonList().stream().map(person -> person.getName().fullName).toList());
    }

    @Test
    public void update_dateAndClear_surviveReloadAndAffectDateSearch() throws Exception {
        logic.execute("update Zoe Tan f/20261028");
        model = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        logic = createLogic(model);
        logic.execute("find f/20261028");
        assertNames("Zoe Tan");
        logic.execute("update 1 f/");
        model = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        logic = createLogic(model);
        logic.execute("find f/20261028");
        assertNames();
        logic.execute("find f/20261025");
        assertNames("Alicia Tan");
        assertTrue(storage.readAddressBook().orElseThrow().getPersonList().getFirst().getFollowUpDate().isEmpty());
    }

    @Test
    public void find_invalidDates_preserveContactsCurrentResultsAndSavedJson() throws Exception {
        logic.execute("find f/20261025");
        List<Person> all = List.copyOf(model.getAddressBook().getPersonList());
        List<Person> filtered = List.copyOf(model.getFilteredPersonList());
        String json = Files.readString(storage.getAddressBookFilePath());
        for (String args : List.of("f/", "f/20230229", "f/20261025 f/20260431", "f/none",
                "f/2026-10-25", "f/20261025 m/email", "Alicia f/20261025")) {
            assertThrows(ParseException.class, () -> logic.execute("find " + args));
            assertEquals(all, model.getAddressBook().getPersonList());
            assertEquals(filtered, model.getFilteredPersonList());
            assertEquals(json, Files.readString(storage.getAddressBookFilePath()));
        }
        logic.execute("find f/20261026");
        assertEquals(json, Files.readString(storage.getAddressBookFilePath()));
    }

    @Test
    public void find_legacyRecordWithoutDate_doesNotMatch() throws Exception {
        model.addPerson(new PersonBuilder().withName("Legacy Contact").build());
        logic.execute("find f/20261025 f/20261026");
        assertNames("Zoe Tan", "Alicia Tan", "Ben Lim");
    }

    private LogicManager createLogic(ModelManager target) {
        return IntegrationTestFixture.createLogic(target, storage, folder.resolve("prefs.json"));
    }

    private void assertNames(String... expected) {
        assertEquals(List.of(expected), model.getFilteredPersonList().stream()
                .map(person -> person.getName().fullName).toList());
    }
}
