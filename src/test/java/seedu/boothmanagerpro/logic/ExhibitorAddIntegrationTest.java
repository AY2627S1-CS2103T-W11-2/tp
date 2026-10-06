package seedu.boothmanagerpro.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.boothmanagerpro.logic.commands.exceptions.CommandException;
import seedu.boothmanagerpro.logic.parser.exceptions.ParseException;
import seedu.boothmanagerpro.model.ModelManager;
import seedu.boothmanagerpro.model.UserPrefs;
import seedu.boothmanagerpro.model.person.ContactMethod;
import seedu.boothmanagerpro.model.person.Person;
import seedu.boothmanagerpro.model.person.exceptions.DuplicatePersonException;
import seedu.boothmanagerpro.storage.JsonAddressBookStorage;
import seedu.boothmanagerpro.storage.JsonUserPrefsStorage;
import seedu.boothmanagerpro.storage.StorageManager;
import seedu.boothmanagerpro.testutil.PersonBuilder;

/** Verifies the command-to-disk flow without JavaFX or a display server. */
public class ExhibitorAddIntegrationTest {
    private static final String ADD = "add n/Alicia Tan c/TechNova Pte Ltd e/alicia@technova.com p/91234567";

    @TempDir
    public Path folder;

    private ModelManager model;
    private JsonAddressBookStorage storage;
    private LogicManager logic;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        storage = new JsonAddressBookStorage(folder.resolve("contacts.json"));
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(folder.resolve("preferences.json"))));
    }

    @Test
    public void add_saveReloadAndEdit_preservesExhibitorFields() throws Exception {
        logic.execute(ADD + " m/email t/high-priority t/technology");
        ModelManager reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(model.getAddressBook(), reloaded.getAddressBook());
        logic.execute("edit 1 p/+65 9876-5432");
        Person edited = storage.readAddressBook().orElseThrow().getPersonList().getFirst();
        assertEquals("TechNova Pte Ltd", edited.getCompany().orElseThrow().value);
        assertEquals(ContactMethod.EMAIL, edited.getContactMethod().orElseThrow());
        assertEquals("+6598765432", edited.getPhone().value);
        assertEquals(2, edited.getTags().size());
    }

    @Test
    public void add_withoutOptionalFields_roundTrips() throws Exception {
        String feedback = logic.execute(ADD).getFeedbackToUser();
        Person reloaded = storage.readAddressBook().orElseThrow().getPersonList().getFirst();
        assertTrue(reloaded.getContactMethod().isEmpty());
        assertTrue(reloaded.getTags().isEmpty());
        assertTrue(feedback.contains("Contact method: Not specified\nTags: None"));
    }

    @Test
    public void add_duplicateEmailOutsideFilteredList_rejectedWithoutChangingDisk() throws Exception {
        logic.execute(ADD);
        logic.execute("find Nobody");
        String before = Files.readString(storage.getAddressBookFilePath());
        CommandException error = assertThrows(CommandException.class, () -> logic.execute(
                "add n/Someone Else c/Other Company e/ALICIA@TECHNOVA.COM p/98765432"));
        assertEquals("This contact may already exist: Alicia Tan at TechNova Pte Ltd.\n"
                + "Use the edit command if you want to update the existing contact.", error.getMessage());
        assertEquals(before, Files.readString(storage.getAddressBookFilePath()));
        assertEquals(1, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void add_sameNameAndCompanyDifferentEmail_rejected() throws Exception {
        logic.execute(ADD);
        assertThrows(CommandException.class, () -> logic.execute(ADD.replace("Alicia Tan", "alicia tan")
                .replace("TechNova Pte Ltd", "TECHNOVA PTE LTD").replace("alicia@technova.com", "other@example.com")));
        assertEquals(1, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void add_sameNameDifferentCompanyAndEmail_allowed() throws Exception {
        logic.execute(ADD);
        logic.execute(ADD.replace("TechNova Pte Ltd", "Other Company")
                .replace("alicia@technova.com", "other@example.com"));
        assertEquals(2, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void edit_duplicateEmailWhileNameAndCompanyUnchanged_rejected() throws Exception {
        logic.execute(ADD);
        logic.execute("add n/Ben Lim c/Other e/ben@example.com p/98765432");
        assertThrows(CommandException.class, () -> logic.execute("edit 1 e/ben@example.com"));
        Person original = model.getAddressBook().getPersonList().getFirst();
        Person conflicting = new PersonBuilder(original).withEmail("ben@example.com").build();
        assertThrows(DuplicatePersonException.class, () -> model.setPerson(original, conflicting));
        assertEquals("alicia@technova.com", model.getAddressBook().getPersonList().getFirst().getEmail().value);
    }

    @Test
    public void add_invalidCommand_doesNotWriteOrMutateModel() {
        assertThrows(ParseException.class, () -> logic.execute(ADD + " m/fax"));
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertFalse(Files.exists(storage.getAddressBookFilePath()));
    }

    @Test
    public void edit_nameCompanyCollisionOutsideFilter_rejectedWithoutChangingDisk() throws Exception {
        logic.execute(ADD);
        logic.execute("add n/Ben Lim c/TechNova Pte Ltd e/ben@example.com p/98765432");
        logic.execute("find Ben");
        String before = Files.readString(storage.getAddressBookFilePath());
        assertThrows(CommandException.class, () -> logic.execute("edit 1 n/Alicia Tan"));
        assertEquals(before, Files.readString(storage.getAddressBookFilePath()));
        assertEquals("Ben Lim", model.getFilteredPersonList().getFirst().getName().fullName);
    }

    @Test
    public void add_normalisedDuplicateAfterReload_rejected() throws Exception {
        logic.execute(ADD + " m/phone");
        ModelManager reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        LogicManager reloadedLogic = new LogicManager(reloaded, new StorageManager(storage,
                new JsonUserPrefsStorage(folder.resolve("preferences.json"))));
        assertThrows(CommandException.class, () -> reloadedLogic.execute(
                ADD.replace("alicia@technova.com", "ALICIA@TECHNOVA.COM")));
        assertEquals(1, reloaded.getAddressBook().getPersonList().size());
    }

    @Test
    public void read_legacyContact_hasAbsentExhibitorFields() throws Exception {
        model.addPerson(new PersonBuilder().build());
        storage.saveAddressBook(model.getAddressBook());
        String json = Files.readString(storage.getAddressBookFilePath());
        // Legacy files have neither field; null fields must also remain readable.
        Files.writeString(storage.getAddressBookFilePath(), json.replaceAll(
                "(?m)^.*\"(?:company|contactMethod)\"\\s*:\\s*null,?\\s*$", "")
                .replaceAll(",\\s*}", "}"));
        Person legacy = storage.readAddressBook().orElseThrow().getPersonList().getFirst();
        assertTrue(legacy.getCompany().isEmpty());
        assertTrue(legacy.getContactMethod().isEmpty());
    }
}
