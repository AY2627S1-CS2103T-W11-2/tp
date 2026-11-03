package seedu.boothmanagerpro.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.boothmanagerpro.logic.parser.exceptions.ParseException;
import seedu.boothmanagerpro.model.ModelManager;
import seedu.boothmanagerpro.model.UserPrefs;
import seedu.boothmanagerpro.storage.JsonAddressBookStorage;
import seedu.boothmanagerpro.testutil.IntegrationTestFixture;

/** Keeps full-address search compatible with the mailbox characters accepted by add and storage. */
public class EmailSearchCompatibilityTest {
    @TempDir
    public Path folder;

    @Test
    public void fullEmailWithAcceptedPunctuation_canBeFoundBeforeAndAfterReload() throws Exception {
        JsonAddressBookStorage storage = new JsonAddressBookStorage(folder.resolve("contacts.json"));
        ModelManager model = new ModelManager();
        LogicManager logic = IntegrationTestFixture.createLogic(model, storage, folder.resolve("prefs.json"));
        List<String> emails = List.of("o'connor@example.com", "sales%expo@example.com", "a/b@example.com",
                "a!b@example.com", "a&b@example.com", "a=b@example.com", "a?b@example.com");
        for (int i = 0; i < emails.size(); i++) {
            logic.execute("add n/Contact " + (char) ('A' + i)
                    + " c/Acme e/" + emails.get(i) + " p/91234567");
        }
        String json = Files.readString(storage.getAddressBookFilePath());
        for (int round = 0; round < 2; round++) {
            for (String email : emails) {
                logic.execute("find e/" + email);
                assertEquals(1, model.getFilteredPersonList().size());
                assertEquals(email, model.getFilteredPersonList().getFirst().getEmail().value);
            }
            logic.execute("find e/@example.com");
            assertEquals(emails.size(), model.getFilteredPersonList().size());
            for (String fragment : List.of("'connor", "%expo", "a/b", "A!B")) {
                logic.execute("find e/" + fragment);
                assertEquals(1, model.getFilteredPersonList().size());
            }
            LogicManager current = logic;
            assertThrows(ParseException.class, () -> current.execute("find e/two addresses@example.com"));
            assertThrows(ParseException.class, () -> current.execute("find e/a@@example.com"));
            assertThrows(ParseException.class, () -> current.execute("find e/a@example!.com"));
            assertEquals(json, Files.readString(storage.getAddressBookFilePath()));
            model = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
            logic = IntegrationTestFixture.createLogic(model, storage, folder.resolve("reload-prefs.json"));
        }
    }
}
