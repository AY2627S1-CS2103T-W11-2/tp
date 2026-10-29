package seedu.boothmanagerpro.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.boothmanagerpro.model.ModelManager;
import seedu.boothmanagerpro.model.UserPrefs;
import seedu.boothmanagerpro.storage.JsonAddressBookStorage;
import seedu.boothmanagerpro.testutil.IntegrationTestFixture;

/** Checks whole-word name searches through routing, sorting, combined criteria and persisted data. */
public class FindNameMatchingIntegrationTest {
    @TempDir
    public Path folder;
    private ModelManager model;
    private LogicManager logic;
    private JsonAddressBookStorage storage;

    @BeforeEach
    public void setUp() throws Exception {
        storage = new JsonAddressBookStorage(folder.resolve("contacts.json"));
        model = new ModelManager();
        logic = createLogic();
        logic.execute("add n/Alex Yeo c/Acme e/yeo@example.com p/91234567 f/20261029 t/vip");
        logic.execute("add n/Alex Yeoh c/Other Co e/yeoh@example.com p/81234567 f/20261030 t/vip");
        logic.execute("add n/Alexandra Tan c/Acme e/alexandra@example.com p/82345678 t/vip");
        logic.execute("add n/John Alex c/Acme e/john@example.com p/83456789 f/20261029 t/retail");
    }

    @Test
    public void find_nameWord_matchesAllNamesButNotSubstrings() throws Exception {
        logic.execute("find n/alex");
        assertNames("Alex Yeo", "Alex Yeoh", "John Alex");
        logic.execute("find n/Ale");
        assertNames();
        logic.execute("find n/alex yeo");
        assertNames("Alex Yeo");
        logic.execute("find n/Alex   Yeoh");
        assertNames("Alex Yeoh");
        logic.execute("find n/YEO");
        assertNames("Alex Yeo");
    }

    @Test
    public void find_nameAlternatives_combineWithDateAndTagAndSearchAllContacts() throws Exception {
        logic.execute("find n/Alex n/Alexandra c/Acme t/vip");
        assertNames("Alex Yeo", "Alexandra Tan");
        logic.execute("find n/Alex f/20261029 t/vip");
        assertNames("Alex Yeo");
        logic.execute("find n/Alex f/20261030");
        assertNames("Alex Yeoh");
    }

    @Test
    public void find_sortedIndices_updateIntendedContactAndPreserveStoredData() throws Exception {
        logic.execute("list o/name");
        String before = Files.readString(storage.getAddressBookFilePath());
        logic.execute("find n/Alex");
        assertNames("Alex Yeo", "Alex Yeoh", "John Alex");
        assertEquals(before, Files.readString(storage.getAddressBookFilePath()));
        logic.execute("update 2 p/89999999");
        assertEquals("89999999", storage.readAddressBook().orElseThrow().getPersonList().get(1).getPhone().value);
        assertEquals("91234567", storage.readAddressBook().orElseThrow().getPersonList().getFirst().getPhone().value);
        model = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        logic = createLogic();
        logic.execute("find n/Alex");
        assertNames("Alex Yeo", "Alex Yeoh", "John Alex");
    }

    private LogicManager createLogic() {
        return IntegrationTestFixture.createLogic(model, storage, folder.resolve("prefs.json"));
    }

    private void assertNames(String... expected) {
        assertEquals(List.of(expected), model.getFilteredPersonList().stream()
                .map(person -> person.getName().fullName).toList());
    }
}
