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
import seedu.boothmanagerpro.testutil.IntegrationTestFixture;
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
        logic = IntegrationTestFixture.createLogic(model, storage, folder.resolve("prefs.json"));
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
        assertEquals("+65 8123-4567", updated.getPhone().value);
        assertEquals("phone", updated.getContactMethod().orElseThrow().toString());
        assertEquals("Completed", updated.getEnquiryStatus().toString());
        assertEquals("29 Feb 2028", updated.getFollowUpDate().orElseThrow().toString());
        assertEquals("new, priority", Messages.formatTags(updated));
        assertTrue(logic.execute("view n/Alice Tan").getFeedbackToUser().contains("Follow-up date: 29 Feb 2028"));
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
    public void update_nameTargetWithExtraSpaces_resolvesCanonicalName() throws Exception {
        assertEquals("Updated Alicia Tan at TechNova.",
                logic.execute("update   Alicia   Tan   s/Cancelled").getFeedbackToUser());
        assertEquals("Alicia Tan", saved().getFirst().getName().fullName);
        assertEquals("Cancelled", saved().getFirst().getEnquiryStatus().toString());
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
        logic.execute("update 1 m/ f/ t/");
        assertTrue(saved().getFirst().getContactMethod().isEmpty());
        assertTrue(logic.execute("view n/Alicia Tan").getFeedbackToUser().contains("Contact method: Not specified"));
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
        assertRejected("update Alicia Tan e/ALICIA@example.com", CommandException.class); // No stored change.
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
            "Alicia Tan e/", "Alicia Tan p/"}) {
            assertRejected("update " + args, ParseException.class);
        }
        for (String field : new String[]{"n/Alice", "c/Other", "e/a@example.com", "p/88888888",
            "m/email", "s/Completed", "f/20261025"}) {
            ParseException error = assertRejected("update Alicia Tan " + field + " " + field, ParseException.class);
            assertTrue(error.getMessage().contains("Repeated fields:"));
        }
    }

    @Test
    public void patronymicName_allCommands_roundTripsConsistently() throws Exception {
        assertEquals("Added Raj s/o Kumar at Acme.",
                logic.execute("add n/Raj s/o Kumar c/Acme e/raj@example.com p/91112222").getFeedbackToUser());
        assertTrue(logic.execute("view n/Raj s/o Kumar").getFeedbackToUser().contains("Raj s/o Kumar at Acme"));
        logic.execute("find n/Raj s/o Kumar");
        assertEquals(1, model.getFilteredPersonList().size());
        logic.execute("find Raj s/o Kumar");
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals("Updated Raj s/o Kumar at Acme.",
                logic.execute("update Raj s/o Kumar s/Completed").getFeedbackToUser());
        assertEquals("Completed", saved().get(2).getEnquiryStatus().toString());
        assertTrue(logic.execute("tag Raj s/o Kumar t/vip").getFeedbackToUser()
                .contains("Updated tags for Raj s/o Kumar at Acme. Added: vip."));
        logic.execute("list");
        logic.execute("update 3 n/Priya d/o Raman");
        assertEquals("Priya d/o Raman", saved().get(2).getName().fullName);
        assertTrue(logic.execute("delete Priya d/o Raman").getFeedbackToUser()
                .contains("Priya d/o Raman"));
        assertEquals(2, saved().size());
    }

    @Test
    public void update_unchangedValues_preservesFilterOrderAndDisk() throws Exception {
        logic.execute("list o/company");
        logic.execute("find n/Alicia");
        for (String fields : List.of("n/Alicia\tTan", "c/TechNova", "e/ALICIA@example.com", "p/91234567",
                "m/EMAIL", "s/In progress", "f/20261025", "t/old", "e/ALICIA@example.com s/In progress")) {
            for (String target : List.of("1", "alicia tan")) {
                CommandException error = assertRejected("update " + target + " " + fields, CommandException.class);
                assertEquals("No changes made: Alicia Tan at TechNova already has the supplied values.",
                        error.getMessage());
            }
        }
        assertRejected("update Ben Lim f/ t/", CommandException.class);
        logic.execute("find n/Alicia n/Ben");
        assertEquals(List.of("Ben Lim", "Alicia Tan"), model.getFilteredPersonList().stream()
                .map(person -> person.getName().fullName).toList());
    }

    @Test
    public void update_displayOnlyChanges_persistsNewFormattingAndTagOrder() throws Exception {
        logic.execute("update 1 t/OLD");
        assertEquals("OLD", Messages.formatTags(saved().getFirst()));
        logic.execute("update 1 t/OLD t/new");
        logic.execute("update 1 t/new t/OLD");
        assertEquals("new, OLD", Messages.formatTags(saved().getFirst()));
        logic.execute("update 1 n/ALICIA TAN c/TECHNOVA p/9123-4567");
        Person updated = saved().getFirst();
        assertEquals("ALICIA TAN", updated.getName().fullName);
        assertEquals("TECHNOVA", updated.getCompany().orElseThrow().value);
        assertEquals("9123-4567", updated.getPhone().value);
    }

    @Test
    public void expandedNamesAndPhones_roundTripAcrossCommands() throws Exception {
        for (String name : List.of("Raj a/l Kumar", "Priya A/P Raman", "John @ Johnny",
                "John Smith, Jr., PhD", "John (Johnny) Smith", "Peter the 2nd", "Henry VIII")) {
            String phone = "(65) 6123-4567 ext 88 / +65 8765-4321";
            logic.execute("add n/" + name.replace(" ", "\t") + " c/Acme e/new@example.com p/" + phone);
            assertEquals(name, saved().get(2).getName().fullName);
            assertEquals(phone, saved().get(2).getPhone().value);
            assertTrue(logic.execute("view n/" + name).getFeedbackToUser().contains("Phone: " + phone));
            for (String query : List.of("n/" + name, name, "p/(65) 6123.4567", "p/8765-4321")) {
                logic.execute("find " + query);
                assertEquals(List.of(name), model.getFilteredPersonList().stream()
                        .map(person -> person.getName().fullName).toList());
            }
            logic.execute("update " + name + " s/Completed");
            assertEquals("Completed", saved().get(2).getEnquiryStatus().toString());
            logic.execute("tag " + name + " t/vip");
            assertEquals("vip", Messages.formatTags(saved().get(2)));
            logic.execute("delete " + name);
            assertEquals(2, saved().size());
        }
    }

    @Test
    public void update_companyWithFinalPeriod_doesNotDuplicatePunctuation() throws Exception {
        assertEquals("Updated Alicia Tan at Acme Inc.",
                logic.execute("update 1 c/Acme Inc.").getFeedbackToUser());
        assertEquals("Acme Inc.", saved().getFirst().getCompany().orElseThrow().value);
    }

    @Test
    public void companySlashAbbreviations_addFindAndUpdate_roundTripsWithoutSplitting() throws Exception {

        assertEquals("Added Priya Nair at Acme Pte Ltd t/a Acme Events.",
                logic.execute("add n/Priya Nair c/Acme Pte Ltd t/a Acme Events e/priya@acme.com p/92223333")
                        .getFeedbackToUser());
        assertEquals("Acme Pte Ltd t/a Acme Events", saved().get(2).getCompany().orElseThrow().value);
        assertTrue(saved().get(2).getTags().isEmpty());

        logic.execute("find c/Acme Pte Ltd t/a Acme Events");
        assertEquals(1, model.getFilteredPersonList().size());

        assertEquals("Updated Priya Nair at Holdings c/o M/s Lim S/B.",
                logic.execute("update Priya Nair c/Holdings c/o M/s Lim S/B").getFeedbackToUser());
        assertEquals("Holdings c/o M/s Lim S/B", saved().get(2).getCompany().orElseThrow().value);
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
