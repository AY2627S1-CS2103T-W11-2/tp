package seedu.boothmanagerpro.storage;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.boothmanagerpro.model.ReadOnlyAddressBook;
import seedu.boothmanagerpro.model.ReadOnlyUserPrefs;
import seedu.boothmanagerpro.model.UserPrefs;

public class StorageTest {

    @Test
    public void isAddressBookRecoveryPending_defaultImplementation_returnsFalse() {
        assertFalse(new StubStorage().isAddressBookRecoveryPending());
    }

    /** Minimal implementation that deliberately inherits the interface's recovery default. */
    private static class StubStorage implements Storage {
        @Override
        public Path getUserPrefsFilePath() {
            return Path.of("preferences.json");
        }

        @Override
        public Optional<UserPrefs> readUserPrefs() {
            return Optional.empty();
        }

        @Override
        public void saveUserPrefs(ReadOnlyUserPrefs userPrefs) throws IOException {
            // No persistence is needed for the interface default-method test.
        }

        @Override
        public Path getAddressBookFilePath() {
            return Path.of("addressbook.json");
        }

        @Override
        public Optional<ReadOnlyAddressBook> readAddressBook() {
            return Optional.empty();
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
            // No persistence is needed for the interface default-method test.
        }
    }
}
