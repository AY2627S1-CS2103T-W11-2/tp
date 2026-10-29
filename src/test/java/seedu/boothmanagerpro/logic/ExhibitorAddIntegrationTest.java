package seedu.boothmanagerpro.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    public void add_enquiryMetadata_roundTripsAndSurvivesUpdate() throws Exception {
        logic.execute(ADD + " s/IN PROGRESS f/20240229");
        Person saved = storage.readAddressBook().orElseThrow().getPersonList().getFirst();
        assertEquals("In progress", saved.getEnquiryStatus().toString());
        assertEquals("29 Feb 2024", saved.getFollowUpDate().orElseThrow().toString());
        logic.execute("update 1 p/98765432");
        Person edited = storage.readAddressBook().orElseThrow().getPersonList().getFirst();
        assertEquals(saved.getEnquiryStatus(), edited.getEnquiryStatus());
        assertEquals(saved.getFollowUpDate(), edited.getFollowUpDate());
        String details = logic.execute("view n/Alicia").getFeedbackToUser();
        assertTrue(details.contains("Enquiry status: In progress\nFollow-up date: 29 Feb 2024"));
        assertThrows(CommandException.class, () -> logic.execute(ADD + " s/Completed f/20261025"));
    }

    @Test
    public void add_enquiryDefaultsAndIndependentOptions() throws Exception {
        for (String options : new String[]{"", " s/Completed", " f/20261025"}) {
            logic.execute(ADD + options);
            Person saved = storage.readAddressBook().orElseThrow().getPersonList().getFirst();
            assertEquals(options.contains("s/") ? "Completed" : "Needs follow-up",
                    saved.getEnquiryStatus().toString());
            assertEquals(options.contains("f/"), saved.getFollowUpDate().isPresent());
            logic.execute("clear");
        }
    }

    @Test
    public void add_invalidEnquiryFields_preservesDiskAndContacts() throws Exception {
        logic.execute(ADD);
        String before = Files.readString(storage.getAddressBookFilePath());
        for (String options : new String[]{" s/", " f/", " s/new", " f/20230229", " f/2026-10-25",
            " s/Completed s/In progress", " f/20261025 f/20261026"}) {
            assertThrows(ParseException.class, () -> logic.execute(ADD + options));
            assertEquals(before, Files.readString(storage.getAddressBookFilePath()));
            assertEquals(1, model.getAddressBook().getPersonList().size());
        }
    }

    @Test
    public void add_twoDifferentDuplicateMatches_reportsBothWithoutMutation() throws Exception {
        logic.execute(ADD);
        logic.execute("add n/Ben Lim c/Other e/ben@example.com p/98765432");
        logic.execute("find Nobody");
        String before = Files.readString(storage.getAddressBookFilePath());
        var contacts = java.util.List.copyOf(model.getAddressBook().getPersonList());
        CommandException error = assertThrows(CommandException.class, () ->
                logic.execute(ADD.replace("alicia@technova.com", "BEN@example.com")));
        assertEquals("This contact conflicts with multiple existing contacts:\n"
                + "Alicia Tan at TechNova Pte Ltd (alicia@technova.com)\n"
                + "Ben Lim at Other (ben@example.com)\n"
                + "Check the email and name/company fields. No contact was added.", error.getMessage());
        assertEquals(contacts, model.getAddressBook().getPersonList());
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(before, Files.readString(storage.getAddressBookFilePath()));
    }

    @Test
    public void add_rejectedInputs_preserveExistingDataAndFilter() throws Exception {
        logic.execute(ADD);
        logic.execute("find Nobody");
        String before = Files.readString(storage.getAddressBookFilePath());
        var contacts = java.util.List.copyOf(model.getAddressBook().getPersonList());
        for (String input : new String[]{"add", ADD + " n/Other", ADD + " x/value",
            ADD + " m/", ADD + " m/fax", ADD.replace("91234567", "1234\t567")}) {
            assertThrows(ParseException.class, () -> logic.execute(input));
            assertEquals(contacts, model.getAddressBook().getPersonList());
            assertTrue(model.getFilteredPersonList().isEmpty());
            assertEquals(before, Files.readString(storage.getAddressBookFilePath()));
        }
    }

    @Test
    public void addFindViewListDelete_preserveSharedContactAttributes() throws Exception {
        logic.execute(ADD + " m/EMAIL t/high-priority");
        logic.execute("add n/Alicia Tan c/Other Ltd e/other@example.com p/87654321");
        logic.execute("find c/ TECHNOVA PTE LTD c/technova pte ltd t/HIGH-PRIORITY");
        assertEquals(1, model.getFilteredPersonList().size());
        Person contact = model.getFilteredPersonList().getFirst();
        assertEquals("TechNova Pte Ltd", contact.getCompany().orElseThrow().value);
        assertEquals(ContactMethod.EMAIL, contact.getContactMethod().orElseThrow());
        logic.execute("find c/TechNova");
        assertTrue(model.getFilteredPersonList().isEmpty());
        logic.execute("find c/TechNova Pte Ltd c/Other Ltd");
        assertEquals(2, model.getFilteredPersonList().size());
        String view = logic.execute("view n/Alicia Tan").getFeedbackToUser();
        assertTrue(view.contains("TechNova Pte Ltd"));
        assertTrue(view.contains("Other Ltd"));
        assertTrue(logic.execute("1").getFeedbackToUser().contains("Contact method: email"));
        String list = logic.execute("list").getFeedbackToUser();
        assertTrue(list.contains("Total contacts: 2"));
        assertTrue(list.contains("Contact method: Not specified"));
        assertThrows(CommandException.class, () -> logic.execute("delete Alicia Tan"));
        logic.execute("find c/TechNova Pte Ltd");
        String deleted = logic.execute("delete 1").getFeedbackToUser();
        assertTrue(deleted.contains("TechNova Pte Ltd"));
        assertTrue(deleted.contains("Contact method: email"));
        assertEquals(1, storage.readAddressBook().orElseThrow().getPersonList().size());
        assertEquals("Other Ltd", model.getAddressBook().getPersonList().getFirst()
                .getCompany().orElseThrow().value);
    }

    @Test
    public void findCompany_legacyContactDoesNotMatch() throws Exception {
        model.addPerson(new PersonBuilder().build());
        logic.execute("find c/TechNova");
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(1, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void add_saveReloadAndUpdate_preservesExhibitorFields() throws Exception {
        logic.execute(ADD + " m/email t/high-priority t/technology");
        ModelManager reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(model.getAddressBook(), reloaded.getAddressBook());
        logic.execute("update 1 p/+65 9876-5432");
        Person edited = storage.readAddressBook().orElseThrow().getPersonList().getFirst();
        assertEquals("TechNova Pte Ltd", edited.getCompany().orElseThrow().value);
        assertEquals(ContactMethod.EMAIL, edited.getContactMethod().orElseThrow());
        assertEquals("+6598765432", edited.getPhone().value);
        assertEquals(2, edited.getTags().size());
    }

    @Test
    public void update_tags_keepInputOrderAcrossFeedbackAndReload() throws Exception {
        logic.execute(ADD);
        String feedback = logic.execute("update 1 t/technology t/high-priority t/technology").getFeedbackToUser();
        Person reloaded = storage.readAddressBook().orElseThrow().getPersonList().getFirst();
        assertEquals(List.of("technology", "high-priority"),
                reloaded.getTags().stream().map(tag -> tag.tagName).toList());
        assertTrue(feedback.contains("Updated Alicia Tan at TechNova Pte Ltd."));
        assertTrue(logic.execute("list").getFeedbackToUser().contains("Tags: technology, high-priority"));
        assertTrue(logic.execute("view n/Alicia Tan").getFeedbackToUser()
                .contains("Tags: technology, high-priority"));
    }

    @Test
    public void update_unknownField_preservesContactAndSavedData() throws Exception {
        logic.execute(ADD);
        String before = Files.readString(storage.getAddressBookFilePath());
        Person original = model.getAddressBook().getPersonList().getFirst();
        assertThrows(ParseException.class, () -> logic.execute("update 1 a/123 Main Street"));
        assertEquals(original, model.getAddressBook().getPersonList().getFirst());
        assertEquals(before, Files.readString(storage.getAddressBookFilePath()));
    }

    @Test
    public void update_emptyFields_useSharedMessagesAndPreserveData() throws Exception {
        logic.execute(ADD);
        Person original = model.getAddressBook().getPersonList().getFirst();
        String before = Files.readString(storage.getAddressBookFilePath());
        String[][] cases = {
            {"update 1 n/", "n/"},
            {"update 1 c/", "c/"},
            {"update 1 t/ t/new", "t/"}
        };

        for (String[] testCase : cases) {
            ParseException error = assertThrows(ParseException.class, () -> logic.execute(testCase[0]));
            assertEquals("Empty field: " + testCase[1] + ". Supply a value after this prefix.", error.getMessage());
            assertEquals(original, model.getAddressBook().getPersonList().getFirst());
            assertEquals(before, Files.readString(storage.getAddressBookFilePath()));
        }
    }

    @Test
    public void add_withoutOptionalFields_roundTrips() throws Exception {
        String feedback = logic.execute(ADD).getFeedbackToUser();
        Person reloaded = storage.readAddressBook().orElseThrow().getPersonList().getFirst();
        assertTrue(reloaded.getContactMethod().isEmpty());
        assertTrue(reloaded.getTags().isEmpty());
        assertEquals("Added Alicia Tan at TechNova Pte Ltd.", feedback);
    }

    @Test
    public void add_duplicateEmailOutsideFilteredList_rejectedWithoutChangingDisk() throws Exception {
        logic.execute(ADD);
        logic.execute("find Nobody");
        String before = Files.readString(storage.getAddressBookFilePath());
        CommandException error = assertThrows(CommandException.class, () -> logic.execute(
                "add n/Someone Else c/Other Company e/ALICIA@TECHNOVA.COM p/98765432"));
        assertEquals("This contact may already exist: Alicia Tan at TechNova Pte Ltd.\n"
                + "Use the update command if you want to change the existing contact.", error.getMessage());
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
    public void add_sameNameAndCompanyWithRepeatedSpaces_rejected() throws Exception {
        logic.execute("add n/Ben Lim c/Acme e/ben@acme.com p/98765432");
        String before = Files.readString(storage.getAddressBookFilePath());

        assertThrows(CommandException.class, () -> logic.execute(
                "add n/Ben  Lim c/Acme e/ben2@acme.com p/91234561"));

        assertEquals(1, model.getAddressBook().getPersonList().size());
        assertEquals(before, Files.readString(storage.getAddressBookFilePath()));
    }

    @Test
    public void add_sameNameDifferentCompanyAndEmail_allowed() throws Exception {
        logic.execute(ADD);
        logic.execute(ADD.replace("TechNova Pte Ltd", "Other Company")
                .replace("alicia@technova.com", "other@example.com"));
        assertEquals(2, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void update_duplicateEmailWhileNameAndCompanyUnchanged_rejected() throws Exception {
        logic.execute(ADD);
        logic.execute("add n/Ben Lim c/Other e/ben@example.com p/98765432");
        CommandException error = assertThrows(CommandException.class, () ->
                logic.execute("update 1 e/ben@example.com"));
        assertEquals("This update conflicts with another contact: Ben Lim at Other. "
                + "Check the email and name/company fields. No contact was updated.", error.getMessage());
        Person original = model.getAddressBook().getPersonList().getFirst();
        Person conflicting = new PersonBuilder(original).withEmail("ben@example.com").build();
        assertThrows(DuplicatePersonException.class, () -> model.setPerson(original, conflicting));
        assertEquals("alicia@technova.com", model.getAddressBook().getPersonList().getFirst().getEmail().value);
    }

    @Test
    public void duplicateFeedback_identifiesConflictAndRecommendsUpdate() throws Exception {
        logic.execute("add n/Ben Lim c/Acme e/ben@acme.com p/98765432");
        logic.execute("add n/Chloe Ng c/Acme e/chloe@acme.com p/97776666");
        logic.execute("add n/John Tan c/GreenWorks e/john1@gw.com p/81234567");

        CommandException updateError = assertThrows(CommandException.class, () ->
                logic.execute("update 1 e/chloe@acme.com"));
        assertEquals("This update conflicts with another contact: Chloe Ng at Acme. "
                + "Check the email and name/company fields. No contact was updated.", updateError.getMessage());

        CommandException addError = assertThrows(CommandException.class, () ->
                logic.execute("add n/john tan c/greenworks e/new@x.com p/91234560"));
        assertEquals("This contact may already exist: John Tan at GreenWorks.\n"
                + "Use the update command if you want to change the existing contact.", addError.getMessage());
        assertEquals(3, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void add_invalidCommand_doesNotWriteOrMutateModel() {
        assertThrows(ParseException.class, () -> logic.execute(ADD + " m/fax"));
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertFalse(Files.exists(storage.getAddressBookFilePath()));
    }

    @Test
    public void update_nameCompanyCollisionOutsideFilter_rejectedWithoutChangingDisk() throws Exception {
        logic.execute(ADD);
        logic.execute("add n/Ben Lim c/TechNova Pte Ltd e/ben@example.com p/98765432");
        logic.execute("find Ben");
        String before = Files.readString(storage.getAddressBookFilePath());
        assertThrows(CommandException.class, () -> logic.execute("update 1 n/Alicia Tan"));
        assertEquals(before, Files.readString(storage.getAddressBookFilePath()));
        assertEquals("Ben Lim", model.getFilteredPersonList().getFirst().getName().fullName);
    }

    @Test
    public void update_nameCompanyCollisionWithCaseOrRepeatedSpaces_rejected() throws Exception {
        logic.execute("add n/Ben Lim c/Acme e/ben@acme.com p/98765432");
        logic.execute("add n/Chloe Ng c/Acme e/chloe@acme.com p/97776666");
        String before = Files.readString(storage.getAddressBookFilePath());

        for (String command : List.of("update 1 n/CHLOE NG", "update 1 n/chloe  ng")) {
            assertThrows(CommandException.class, () -> logic.execute(command));
            assertEquals(before, Files.readString(storage.getAddressBookFilePath()));
        }

        assertEquals(List.of("Ben Lim", "Chloe Ng"), model.getAddressBook().getPersonList().stream()
                .map(person -> person.getName().fullName).toList());
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

    // Unicode NFC normalisation

    @Test
    public void add_unicodeNfdName_rejectedAsDuplicateOfNfcContact() throws Exception {
        // First contact: name contains é as precomposed U+00E9 (NFC form).
        logic.execute("add n/Jos\u00E9 Tan c/Unicode Co e/jose1@example.com p/81234567");
        assertEquals(1, model.getAddressBook().getPersonList().size());

        // Second add: visually identical name using NFD encoding (e + combining acute U+0301).
        // After NFC normalisation both names are identical, so the add must be rejected.
        assertThrows(CommandException.class, () ->
                logic.execute("add n/Jose\u0301 Tan c/Unicode Co e/jose2@example.com p/82345678"));
        assertEquals(1, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void add_unicodeNfdCompany_rejectedAsDuplicateOfNfcContact() throws Exception {
        // First contact: company contains NFC precomposed accent.
        logic.execute("add n/Alice Tan c/Caf\u00E9 Co e/alice1@example.com p/81234567");
        assertEquals(1, model.getAddressBook().getPersonList().size());

        // Second add: same company name but NFD combining accent — visually identical, must be rejected.
        assertThrows(CommandException.class, () ->
                logic.execute("add n/Alice Tan c/Cafe\u0301 Co e/alice2@example.com p/82345678"));
        assertEquals(1, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void add_saveFailure_rollsBackInMemoryModelAndPreventsPersistence() throws Exception {
        // Save initial state with one contact
        logic.execute(ADD);
        assertEquals(1, model.getAddressBook().getPersonList().size());
        Path filePath = storage.getAddressBookFilePath();

        // Make file read-only to simulate save failure
        boolean readOnlySet = filePath.toFile().setReadOnly();
        assertTrue(readOnlySet, "File should be marked read-only");

        try {
            assertThrows(CommandException.class, () ->
                    logic.execute("add n/Write Failure c/Test Co e/writefail@example.com p/81234567"));

            // Contact must NOT be in memory
            assertEquals(1, model.getAddressBook().getPersonList().size());
            assertFalse(model.hasPerson(new PersonBuilder().withName("Write Failure")
                    .withCompany("Test Co").withEmail("writefail@example.com").withPhone("81234567").build()));

            // view command must not find Write Failure even without restoring permissions
            assertThrows(CommandException.class, () -> logic.execute("view n/Write Failure"));
        } finally {
            filePath.toFile().setWritable(true);
        }
    }
}
