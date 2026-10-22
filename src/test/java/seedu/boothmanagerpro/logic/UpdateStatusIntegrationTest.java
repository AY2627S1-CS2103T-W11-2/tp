package seedu.boothmanagerpro.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.boothmanagerpro.commons.exceptions.DataLoadingException;
import seedu.boothmanagerpro.logic.commands.exceptions.CommandException;
import seedu.boothmanagerpro.logic.parser.exceptions.ParseException;
import seedu.boothmanagerpro.model.ModelManager;
import seedu.boothmanagerpro.model.UserPrefs;
import seedu.boothmanagerpro.model.person.EnquiryStatus;
import seedu.boothmanagerpro.model.person.Person;
import seedu.boothmanagerpro.storage.JsonAddressBookStorage;
import seedu.boothmanagerpro.storage.JsonUserPrefsStorage;
import seedu.boothmanagerpro.storage.StorageManager;

/** Exercises status updates through parsing, selection, model replacement and persisted JSON. */
public class UpdateStatusIntegrationTest {
    @TempDir
    public Path folder;

    private ModelManager model;
    private JsonAddressBookStorage storage;
    private LogicManager logic;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager();
        storage = new JsonAddressBookStorage(folder.resolve("contacts.json"));
        logic = createLogic(model);
        logic.execute("add n/Alicia Tan c/TechNova e/alicia@example.com p/91234567 m/email t/priority t/tech");
        logic.execute("add n/Ben Lim c/Other e/ben@example.com p/98765432");
    }

    private LogicManager createLogic(ModelManager target) {
        return new LogicManager(target, new StorageManager(storage,
                new JsonUserPrefsStorage(folder.resolve("preferences.json"))));
    }

    @Test
    public void update_allStatuses_roundTripAndPreserveOtherFields() throws Exception {
        Person original = model.getAddressBook().getPersonList().getFirst();
        assertEquals(EnquiryStatus.NEEDS_FOLLOW_UP, original.getEnquiryStatus());
        for (EnquiryStatus status : List.of(EnquiryStatus.IN_PROGRESS, EnquiryStatus.COMPLETED,
                EnquiryStatus.NEEDS_FOLLOW_UP)) {
            String feedback = logic.execute("update 1 s/" + status).getFeedbackToUser();
            assertEquals("Updated Alicia Tan at TechNova.", feedback);
            assertEquals(original.withEnquiryStatus(status), model.getAddressBook().getPersonList().getFirst());
            assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        }
        logic.execute("update 1 s/Completed");
        ModelManager restarted = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        LogicManager restartedLogic = createLogic(restarted);
        assertTrue(restartedLogic.execute("view n/Alicia").getFeedbackToUser().contains("Enquiry status: Completed"));
        restartedLogic.execute("edit 1 p/87654321");
        assertEquals(EnquiryStatus.COMPLETED, storage.readAddressBook().orElseThrow()
                .getPersonList().getFirst().getEnquiryStatus());
        assertTrue(restartedLogic.execute("list").getFeedbackToUser().contains("Enquiry status: Completed"));
        assertTrue(restartedLogic.execute("find Alicia").getFeedbackToUser().contains("Enquiry status: Completed"));
    }

    @Test
    public void update_filteredIndexAndHiddenFullName_selectCorrectContact() throws Exception {
        logic.execute("find Ben");
        logic.execute("update 1 s/cOmPlEtEd");
        assertEquals(EnquiryStatus.COMPLETED, model.getAddressBook().getPersonList().get(1).getEnquiryStatus());
        assertEquals(2, model.getFilteredPersonList().size());
        assertEquals(EnquiryStatus.NEEDS_FOLLOW_UP,
                model.getAddressBook().getPersonList().getFirst().getEnquiryStatus());
        logic.execute("find Ben");
        logic.execute("update alicia tan s/In progress");
        assertEquals(2, model.getFilteredPersonList().size());
        assertEquals(EnquiryStatus.IN_PROGRESS, model.getAddressBook().getPersonList().getFirst().getEnquiryStatus());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void update_invalidRequests_preserveDataFilterAndDisk() throws Exception {
        logic.execute("find Ben");
        var contacts = List.copyOf(model.getAddressBook().getPersonList());
        var filtered = List.copyOf(model.getFilteredPersonList());
        String saved = Files.readString(storage.getAddressBookFilePath());
        for (String input : List.of("update", "update 1", "update 1 confirmed", "update 0 s/Completed",
                "update -1 s/Completed", "update 999999999999999999999 s/Completed", "update 1 s/Completed extra",
                "update 1 Completed", "update n/Alicia Tan s/Completed", "update Completed",
                "update 1 s/", "update 1 s/Cancelled", "update 1 s/Completed s/In progress",
                "update 1 s/Completed p/87654321", "update 1 f/20261025", "update 1 t/")) {
            assertThrows(ParseException.class, () -> logic.execute(input), input);
        }
        for (String input : List.of("update 2 s/Completed", "update Alicia s/Completed", "update Nobody s/Completed")) {
            assertThrows(CommandException.class, () -> logic.execute(input), input);
        }
        assertEquals(contacts, model.getAddressBook().getPersonList());
        assertEquals(filtered, model.getFilteredPersonList());
        assertEquals(saved, Files.readString(storage.getAddressBookFilePath()));
    }

    @Test
    public void update_ambiguousName_requiresDisplayedIndexEvenWhenFiltered() throws Exception {
        logic.execute("add n/Alicia Tan c/Another e/another@example.com p/87654321");
        logic.execute("find c/TechNova");
        String saved = Files.readString(storage.getAddressBookFilePath());
        CommandException error = assertThrows(CommandException.class, () ->
                logic.execute("update Alicia Tan s/Completed"));
        assertTrue(error.getMessage().contains("displayed index"));
        assertEquals(saved, Files.readString(storage.getAddressBookFilePath()));
        logic.execute("update 1 s/Completed");
        assertEquals(EnquiryStatus.COMPLETED, model.getFilteredPersonList().getFirst().getEnquiryStatus());
        assertEquals(EnquiryStatus.NEEDS_FOLLOW_UP,
                model.getAddressBook().getPersonList().getLast().getEnquiryStatus());
    }

    @Test
    public void update_sameStatus_keepsDataAndConfirms() throws Exception {
        String saved = Files.readString(storage.getAddressBookFilePath());
        String result = logic.execute("update Alicia Tan s/Needs follow-up").getFeedbackToUser();
        assertEquals("Updated Alicia Tan at TechNova.", result);
        assertEquals(saved, Files.readString(storage.getAddressBookFilePath()));
    }

    @Test
    public void read_legacyAndInvalidStatus_defaultOrReject() throws Exception {
        String saved = Files.readString(storage.getAddressBookFilePath());
        Files.writeString(storage.getAddressBookFilePath(), saved.replaceAll(
                "(?m)^.*\"enquiryStatus\"\\s*:.*$", "").replaceAll(",\\s*}", "}"));
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        Files.writeString(storage.getAddressBookFilePath(), saved.replace("\"Needs follow-up\"", "null"));
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        for (String invalid : List.of("", "unknown", "confirmed", "Cancelled")) {
            Files.writeString(storage.getAddressBookFilePath(), saved.replace("Needs follow-up", invalid));
            assertThrows(DataLoadingException.class, () -> storage.readAddressBook());
        }
    }

    @Test
    public void statusChange_affectsValueEqualityButNotIdentity() {
        Person original = model.getAddressBook().getPersonList().getFirst();
        Person updated = original.withEnquiryStatus(EnquiryStatus.COMPLETED);
        assertNotEquals(original, updated);
        assertTrue(original.isSamePerson(updated));
        assertEquals(updated, updated.withEnquiryStatus(EnquiryStatus.COMPLETED));
        assertEquals(updated.hashCode(), updated.withEnquiryStatus(EnquiryStatus.COMPLETED).hashCode());
        assertThrows(NullPointerException.class, () -> original.withEnquiryStatus(null));
        assertEquals(EnquiryStatus.IN_PROGRESS, EnquiryStatus.fromString("  IN PROGRESS  "));
        assertThrows(IllegalArgumentException.class, () -> EnquiryStatus.fromString("new"));
    }
}
