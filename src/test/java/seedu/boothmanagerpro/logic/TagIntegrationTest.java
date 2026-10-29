package seedu.boothmanagerpro.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.boothmanagerpro.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.boothmanagerpro.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.boothmanagerpro.logic.commands.TagCommand;
import seedu.boothmanagerpro.logic.commands.exceptions.CommandException;
import seedu.boothmanagerpro.logic.parser.exceptions.ParseException;
import seedu.boothmanagerpro.model.ModelManager;
import seedu.boothmanagerpro.model.person.Name;
import seedu.boothmanagerpro.model.person.Person;
import seedu.boothmanagerpro.model.tag.Tag;
import seedu.boothmanagerpro.storage.JsonAddressBookStorage;
import seedu.boothmanagerpro.testutil.IntegrationTestFixture;

/** Exercises real command routing, tag toggling, target resolution, and persistence together. */
public class TagIntegrationTest {
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
                + "m/email s/In progress f/20261025 t/technology t/vip");
        logic.execute("add n/Ben Lim c/GreenWorks e/ben@example.com p/98765432");
    }

    @Test
    public void tag_addAndRemoveTags_togglesAndReportsExactChanges() throws Exception {
        Person original = saved().getFirst();
        assertEquals(
                "Updated tags for Alicia Tan at TechNova. Added: priority, sponsor. "
                        + "Removed: vip. Current tags: technology, priority, sponsor.",
                logic.execute("tag alicia tan t/vip t/priority t/sponsor t/priority").getFeedbackToUser());

        Person updated = saved().getFirst();
        assertEquals(original.getName(), updated.getName());
        assertEquals(original.getPhone(), updated.getPhone());
        assertEquals(original.getEmail(), updated.getEmail());
        assertEquals(original.getAddress(), updated.getAddress());
        assertEquals(original.getCompany(), updated.getCompany());
        assertEquals(original.getContactMethod(), updated.getContactMethod());
        assertEquals(original.getEnquiryStatus(), updated.getEnquiryStatus());
        assertEquals(original.getFollowUpDate(), updated.getFollowUpDate());
        assertEquals("technology, priority, sponsor", Messages.formatTags(updated));

        assertEquals(
                "Updated tags for Alicia Tan at TechNova. Added: None. "
                        + "Removed: technology, priority, sponsor. Current tags: None.",
                logic.execute("tag 1 t/technology t/priority t/sponsor").getFeedbackToUser());
        assertTrue(saved().getFirst().getTags().isEmpty());

        assertEquals(
                "Updated tags for Alicia Tan at TechNova. Added: VIP. Removed: None. Current tags: VIP.",
                logic.execute("tag 1 t/VIP").getFeedbackToUser());
        assertEquals(
                "Updated tags for Alicia Tan at TechNova. Added: high priority. Removed: VIP. "
                        + "Current tags: high priority.",
                logic.execute("tag 1 t/vip t/high  priority").getFeedbackToUser());
        logic.execute("find t/high  priority");
        assertEquals(1, model.getFilteredPersonList().size());
    }

    @Test
    public void tag_unicodeWhitespaceAndCanonicalEquivalence_normalisesAndToggles() throws Exception {
        assertEquals(
                "Updated tags for Alicia Tan at TechNova. Added: r\u00E9sum\u00E9. Removed: vip. "
                        + "Current tags: technology, r\u00E9sum\u00E9.",
                logic.execute("tag 1 t/\u2003vip\u2003 t/re\u0301sume\u0301").getFeedbackToUser());
        logic.execute("find t/r\u00E9sum\u00E9");
        assertEquals(1, model.getFilteredPersonList().size());

        assertEquals(
                "Updated tags for Alicia Tan at TechNova. Added: None. Removed: r\u00E9sum\u00E9. "
                        + "Current tags: technology.",
                logic.execute("tag 1 t/\u2003r\u00E9sum\u00E9\u2003").getFeedbackToUser());
    }

    @Test
    public void tag_filteredIndexAndHiddenName_resolveCorrectContactAndResetFilter() throws Exception {
        logic.execute("find c/GreenWorks");
        assertEquals(
                "Updated tags for Ben Lim at GreenWorks. Added: eco. Removed: None. Current tags: eco.",
                logic.execute("tag 1 t/eco").getFeedbackToUser());
        assertEquals("eco", Messages.formatTags(saved().get(1)));
        assertEquals(2, model.getFilteredPersonList().size());

        logic.execute("find c/GreenWorks");
        assertEquals(
                "Updated tags for Alicia Tan at TechNova. Added: keynote. Removed: technology. "
                        + "Current tags: vip, keynote.",
                logic.execute("tag Alicia Tan t/technology t/keynote").getFeedbackToUser());
        assertEquals("vip, keynote", Messages.formatTags(saved().getFirst()));
        assertEquals(2, model.getFilteredPersonList().size());
    }

    @Test
    public void tag_ambiguousName_requiresDisplayedIndex() throws Exception {
        logic.execute("add n/Alicia Tan c/Other e/other@example.com p/88888888");
        CommandException error = assertRejected("tag Alicia Tan t/new-tag", CommandException.class);
        assertTrue(error.getMessage().contains("Use list or find"));

        logic.execute("find c/Other");
        assertEquals(
                "Updated tags for Alicia Tan at Other. Added: new-tag. Removed: None. Current tags: new-tag.",
                logic.execute("tag 1 t/new-tag").getFeedbackToUser());
        assertEquals("new-tag", Messages.formatTags(saved().get(2)));
        assertEquals("technology, vip", Messages.formatTags(saved().getFirst()));
    }

    @Test
    public void tag_invalidTargetOrSyntax_preservesContactsFilterAndDisk() throws Exception {
        logic.execute("find c/TechNova");
        for (String command : new String[]{"tag 2 t/vip", "tag Nobody t/vip"}) {
            assertRejected(command, CommandException.class);
        }
        for (String args : new String[]{"", "Alicia Tan", "t/vip", "0 t/vip", "-1 t/vip",
            "999999999999999999 t/vip", "Alicia Tan s/Completed", "Alicia Tan t/vip c/Other",
            "Alicia Tan /t/vip", "Alicia Tan t/", "Alicia Tan t/valid t/", "Alicia Tan t/a/b",
            "Alicia Tan t/1234567890123456789012345678901", "123InvalidName t/vip"}) {
            assertRejected("tag " + args, ParseException.class);
        }
    }

    @Test
    public void equals_variousCombinations_returnsExpected() {
        Set<Tag> firstTags = Set.of(new Tag("vip"));
        Set<Tag> secondTags = Set.of(new Tag("sponsor"));
        TagCommand byFirstIndex = new TagCommand(INDEX_FIRST_PERSON, firstTags);
        TagCommand bySecondIndex = new TagCommand(INDEX_SECOND_PERSON, firstTags);
        TagCommand byName = new TagCommand(new Name("Alicia Tan"), firstTags);

        assertEquals(byFirstIndex, byFirstIndex);
        assertEquals(byFirstIndex, new TagCommand(INDEX_FIRST_PERSON, firstTags));
        assertEquals(byName, new TagCommand(new Name("Alicia Tan"), firstTags));
        assertNotEquals(byFirstIndex, null);
        assertNotEquals(byFirstIndex, "not a command");
        assertNotEquals(byFirstIndex, bySecondIndex);
        assertNotEquals(byFirstIndex, byName);
        assertNotEquals(byName, new TagCommand(new Name("Ben Lim"), firstTags));
        assertNotEquals(byFirstIndex, new TagCommand(INDEX_FIRST_PERSON, secondTags));
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
