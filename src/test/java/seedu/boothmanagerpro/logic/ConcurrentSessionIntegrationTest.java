package seedu.boothmanagerpro.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.boothmanagerpro.logic.commands.ListCommand;
import seedu.boothmanagerpro.logic.commands.exceptions.CommandException;
import seedu.boothmanagerpro.model.AddressBook;
import seedu.boothmanagerpro.model.ModelManager;
import seedu.boothmanagerpro.model.UserPrefs;
import seedu.boothmanagerpro.storage.ConcurrentDataModificationException;
import seedu.boothmanagerpro.storage.JsonAddressBookStorage;
import seedu.boothmanagerpro.storage.JsonUserPrefsStorage;
import seedu.boothmanagerpro.storage.StorageManager;

/** Exercises independent application sessions sharing one address book file. */
public class ConcurrentSessionIntegrationTest {
    @TempDir
    public Path folder;

    @Test
    public void staleSession_readOnlyThenMutation_doesNotOverwriteNewerData() throws Exception {
        Path addressBookPath = folder.resolve("data").resolve("addressbook.json");
        new JsonAddressBookStorage(addressBookPath).saveAddressBook(new AddressBook());

        StorageManager firstStorage = createStorage(addressBookPath, "first-preferences.json");
        StorageManager staleStorage = createStorage(addressBookPath, "stale-preferences.json");
        ModelManager firstModel = new ModelManager(firstStorage.readAddressBook().orElseThrow(), new UserPrefs());
        ModelManager staleModel = new ModelManager(staleStorage.readAddressBook().orElseThrow(), new UserPrefs());
        LogicManager firstSession = new LogicManager(firstModel, firstStorage);
        LogicManager staleSession = new LogicManager(staleModel, staleStorage);

        firstSession.execute("add n/Session Alpha c/Audit Co e/alpha@example.com p/91234567");

        assertEquals(ListCommand.MESSAGE_EMPTY, staleSession.execute("list").getFeedbackToUser());
        assertEquals(1, readFromDisk(addressBookPath).getPersonList().size());

        String staleAddCommand = "add n/Session Beta c/Audit Co e/beta@example.com p/81234567";
        CommandException conflict = assertThrows(CommandException.class, () -> staleSession.execute(staleAddCommand));
        assertEquals(String.format(LogicManager.FILE_OPS_ERROR_FORMAT,
                ConcurrentDataModificationException.MESSAGE), conflict.getMessage());
        assertEquals(0, staleModel.getAddressBook().getPersonList().size());
        assertEquals("Session Alpha", readFromDisk(addressBookPath).getPersonList().getFirst().getName().fullName);
    }

    private StorageManager createStorage(Path addressBookPath, String preferencesFileName) {
        return new StorageManager(new JsonAddressBookStorage(addressBookPath),
                new JsonUserPrefsStorage(folder.resolve(preferencesFileName)));
    }

    private AddressBook readFromDisk(Path addressBookPath) throws Exception {
        return new AddressBook(new JsonAddressBookStorage(addressBookPath).readAddressBook().orElseThrow());
    }
}
