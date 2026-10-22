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

import seedu.boothmanagerpro.logic.commands.exceptions.CommandException;
import seedu.boothmanagerpro.logic.parser.exceptions.ParseException;
import seedu.boothmanagerpro.model.ModelManager;
import seedu.boothmanagerpro.model.person.Person;
import seedu.boothmanagerpro.storage.JsonAddressBookStorage;
import seedu.boothmanagerpro.storage.JsonUserPrefsStorage;
import seedu.boothmanagerpro.storage.StorageManager;
import seedu.boothmanagerpro.testutil.PersonBuilder;

/** Exercises real command routing, partial updates, duplicate checks and persistence together. */
public class UpdateIntegrationTest {
    @TempDir
    public Path folder;
    private ModelManager model;
    private LogicManager logic;
    private JsonAddressBookStorage storage;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager();
        storage = new JsonAddressBookStorage(folder.resolve("contacts.json"));
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(folder.resolve("prefs.json"))));
        logic.execute("add n/Alicia Tan c/TechNova e/alicia@example.com p/91234567 "
                + "m/email s/In progress f/20261025 t/old");
        logic.execute("add n/Ben Lim c/GreenWorks e/ben@example.com p/98765432");
    }

    @Test
    public void update_allFields_normalisesAndPersists() throws Exception {
        assertEquals("Updated Alice Tan at New Company.", logic.execute("update alicia tan n/Alice Tan "
                + "c/New Company e/ALICE@example.com p/+65 8123-4567 m/PHONE s/COMPLETED "
                + "f/20280229 t/new t/priority t/new").getFeedbackToUser());
        Person updated = saved().getFirst();
        assertEquals("Alice Tan", updated.getName().fullName);
        assertEquals("New Company", updated.getCompany().orElseThrow().value);
        assertEquals("alice@example.com", updated.getEmail().value);
        assertEquals("+6581234567", updated.getPhone().value);
        assertEquals("phone", updated.getContactMethod().orElseThrow().toString());
        assertEquals("Completed", updated.getEnquiryStatus().toString());
        assertEquals("20280229", updated.getFollowUpDate().orElseThrow().toString());
        assertEquals("new, priority", Messages.formatTags(updated));
        assertTrue(logic.execute("view n/Alice Tan").getFeedbackToUser().contains("Follow-up date: 20280229"));
    }

    @Test
    public void update_cancelled_preservesOtherFieldsAndCanReopen() throws Exception {
        Person original = saved().getFirst();
        assertEquals("Updated Alicia Tan at TechNova.",
                logic.execute("update alicia tan s/cAnCeLlEd").getFeedbackToUser());
        assertEquals(new PersonBuilder(original).withEnquiryStatus("Cancelled").build(), saved().getFirst());
        assertTrue(logic.execute("view n/Alicia").getFeedbackToUser().contains("Enquiry status: Cancelled"));
        for (String status : List.of("Needs follow-up", "In progress", "Completed")) {
            logic.execute("update 1 s/" + status);
            assertEquals(new PersonBuilder(original).withEnquiryStatus(status).build(), saved().getFirst());
            logic.execute("update 1 s/Cancelled");
            assertEquals(new PersonBuilder(original).withEnquiryStatus("Cancelled").build(), saved().getFirst());
        }
    }

    @Test
    public void update_partialAndClearing_preservesOmittedFields() throws Exception {
        Person original = saved().getFirst();
        logic.execute("update 1 s/Completed");
        Person updated = saved().getFirst();
        assertEquals(original.getName(), updated.getName());
        assertEquals(original.getPhone(), updated.getPhone());
        assertEquals(original.getEmail(), updated.getEmail());
        assertEquals(original.getAddress(), updated.getAddress());
        assertEquals(original.getCompany(), updated.getCompany());
        assertEquals(original.getContactMethod(), updated.getContactMethod());
        assertEquals(original.getFollowUpDate(), updated.getFollowUpDate());
        assertEquals(original.getTags(), updated.getTags());
        logic.execute("update 1 f/ t/");
        assertTrue(saved().getFirst().getFollowUpDate().isEmpty());
        assertTrue(saved().getFirst().getTags().isEmpty());
        logic.execute("update 1 s/Needs follow-up");
        assertEquals("Needs follow-up", saved().getFirst().getEnquiryStatus().toString());
    }

    @Test
    public void update_filteredIndexAndHiddenName_resolveCorrectContact() throws Exception {
        logic.execute("find c/GreenWorks");
        logic.execute("update 1 p/88888888");
        assertEquals("88888888", saved().get(1).getPhone().value);
        assertEquals(2, model.getFilteredPersonList().size());
        logic.execute("find c/GreenWorks");
        logic.execute("update Alicia Tan s/Completed");
        assertEquals("Completed", saved().getFirst().getEnquiryStatus().toString());
        logic.execute("update Alicia Tan e/ALICIA@example.com"); // Same normalised identity is allowed.
    }

    @Test
    public void update_ambiguousName_requiresDisplayedIndex() throws Exception {
        logic.execute("add n/Alicia Tan c/Other e/other@example.com p/88888888");
        CommandException error = assertRejected("update Alicia Tan s/Completed", CommandException.class);
        assertTrue(error.getMessage().contains("Use list or find"));
        logic.execute("find c/Other");
        logic.execute("update 1 s/Completed");
        assertEquals("Completed", saved().get(2).getEnquiryStatus().toString());
        assertEquals("In progress", saved().getFirst().getEnquiryStatus().toString());
    }

    @Test
    public void update_invalidTargetOrDuplicate_preservesContactsFilterAndDisk() throws Exception {
        logic.execute("find c/TechNova");
        for (String command : new String[]{"update 2 s/Completed", "update Nobody s/Completed",
            "update Alicia Tan e/BEN@example.com", "update Alicia Tan n/Ben Lim c/GreenWorks"}) {
            assertRejected(command, CommandException.class);
        }
    }

    @Test
    public void update_invalidSyntaxAndValues_preservesContactsFilterAndDisk() throws Exception {
        logic.execute("find c/TechNova");
        for (String args : new String[]{"", "Alicia Tan", "s/Completed", "0 s/Completed", "-1 s/Completed",
            "999999999999999999 s/Completed", "Alicia Tan x/value", "Alicia Tan /s/Completed",
            "Alicia Tan n/123", "Alicia Tan c/", "Alicia Tan e/bad", "Alicia Tan p/123",
            "Alicia Tan m/fax", "Alicia Tan s/done", "Alicia Tan f/20230229", "Alicia Tan f/none",
            "Alicia Tan t/ t/valid", "Alicia Tan t/a/b", "Alicia Tan s/", "Alicia Tan n/",
            "Alicia Tan e/", "Alicia Tan p/", "Alicia Tan m/"}) {
            assertRejected("update " + args, ParseException.class);
        }
        for (String field : new String[]{"n/Alice", "c/Other", "e/a@example.com", "p/88888888",
            "m/email", "s/Completed", "f/20261025"}) {
            ParseException error = assertRejected("update Alicia Tan " + field + " " + field, ParseException.class);
            assertTrue(error.getMessage().contains("Repeated fields:"));
        }
    }

    private List<Person> saved() throws Exception {
        return storage.readAddressBook().orElseThrow().getPersonList();
    }

    /** Every rejection must leave both memory and the actual persisted JSON untouched. */
    private <T extends Exception> T assertRejected(String command, Class<T> type) throws Exception {
        List<Person> before = List.copyOf(model.getAddressBook().getPersonList());
        List<Person> filtered = List.copyOf(model.getFilteredPersonList());
        String json = Files.readString(storage.getAddressBookFilePath());
        T error = assertThrows(type, () -> logic.execute(command), command);
        assertEquals(before, model.getAddressBook().getPersonList(), command);
        assertEquals(filtered, model.getFilteredPersonList(), command);
        assertEquals(json, Files.readString(storage.getAddressBookFilePath()), command);
        return error;
    }
}
