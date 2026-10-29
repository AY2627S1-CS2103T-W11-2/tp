package seedu.boothmanagerpro.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.boothmanagerpro.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.boothmanagerpro.commons.exceptions.DataLoadingException;
import seedu.boothmanagerpro.commons.util.JsonUtil;
import seedu.boothmanagerpro.logic.LogicManager;
import seedu.boothmanagerpro.logic.commands.exceptions.CommandException;
import seedu.boothmanagerpro.model.AddressBook;
import seedu.boothmanagerpro.model.ModelManager;
import seedu.boothmanagerpro.testutil.IntegrationTestFixture;

/** Exercises malformed saved data and recovery through the real storage and command layers. */
public class StorageRecoveryTest {
    @TempDir
    public Path folder;

    @Test
    public void read_nullValues_reportLoadingErrorsWithoutChangingSource() throws Exception {
        String contact = JsonUtil.toJsonString(new JsonAdaptedPerson(getTypicalAddressBook()
                .getPersonList().getFirst()));
        for (String invalid : List.of("null", "{\"persons\":[null]}", "{\"persons\":null}", "{}",
                "{\"persons\":[" + contact.replaceFirst("\"tags\"\\s*:\\s*\\[[^]]*]",
                        "\"tags\":[null]") + "]}")) {
            Path path = folder.resolve("contacts.json");
            Files.writeString(path, invalid);
            JsonAddressBookStorage storage = new JsonAddressBookStorage(path);
            assertThrows(DataLoadingException.class, storage::readAddressBook, invalid);
            assertEquals(invalid, Files.readString(path));
            assertTrue(backups().isEmpty(), "Reading alone must not create or replace files");
        }
    }

    @Test
    public void read_nullPreferences_reportsControlledLoadingError() throws Exception {
        Path path = folder.resolve("preferences.json");
        Files.writeString(path, "null");
        assertThrows(DataLoadingException.class, new JsonUserPrefsStorage(path)::readUserPrefs);
        assertEquals("null", Files.readString(path));
    }

    @Test
    public void save_afterFailedLoad_preservesExactBytesAndReloadsNewData() throws Exception {
        Path path = folder.resolve("contacts.json");
        byte[] original = "{broken JSON\r\n\u03a9".getBytes(StandardCharsets.UTF_8);
        Files.write(path, original);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(path);
        assertThrows(DataLoadingException.class, storage::readAddressBook);
        storage.saveAddressBook(getTypicalAddressBook());
        assertEquals(1, backups().size());
        assertArrayEquals(original, Files.readAllBytes(backups().getFirst()));
        assertEquals(getTypicalAddressBook(), new JsonAddressBookStorage(path).readAddressBook().orElseThrow());
        storage.saveAddressBook(new AddressBook());
        assertEquals(1, backups().size(), "Do not back up the recovered file on every save");
        assertArrayEquals(original, Files.readAllBytes(backups().getFirst()));
    }

    @Test
    public void save_anotherRecovery_keepsEarlierBackup() throws Exception {
        Path path = folder.resolve("contacts.json");
        for (String invalid : List.of("first broken file", "second broken file")) {
            Files.writeString(path, invalid);
            JsonAddressBookStorage storage = new JsonAddressBookStorage(path);
            assertThrows(DataLoadingException.class, storage::readAddressBook);
            storage.saveAddressBook(new AddressBook());
        }
        assertEquals(2, backups().size());
        List<String> contents = new java.util.ArrayList<>();
        for (Path backup : backups()) {
            contents.add(Files.readString(backup));
        }
        assertTrue(contents.containsAll(List.of("first broken file", "second broken file")));
    }

    @Test
    public void save_backupFails_blocksWriteAndAllowsRetry() throws Exception {
        Path path = folder.resolve("contacts.json");
        Files.writeString(path, "broken file");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(path);
        assertThrows(DataLoadingException.class, storage::readAddressBook);
        Files.delete(path);
        IOException error = assertThrows(IOException.class, () -> storage.saveAddressBook(new AddressBook()));
        assertTrue(error.getMessage().contains("No data was saved"));
        assertFalse(Files.exists(path));
        assertTrue(backups().isEmpty(), "Remove incomplete backup files");
        Files.writeString(path, "broken file");
        storage.saveAddressBook(new AddressBook());
        assertEquals("broken file", Files.readString(backups().getFirst()));
    }

    @Test
    public void save_backupDirectoryMissing_preservesProtectionUntilRetry() throws Exception {
        Path directory = Files.createDirectory(folder.resolve("data"));
        Path path = directory.resolve("contacts.json");
        Files.writeString(path, "broken file");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(path);
        assertThrows(DataLoadingException.class, storage::readAddressBook);
        Files.delete(path);
        Files.delete(directory);
        IOException error = assertThrows(IOException.class, () -> storage.saveAddressBook(new AddressBook()));
        assertTrue(error.getMessage().contains("No data was saved"));
        assertFalse(Files.exists(directory), "A failed backup must not create replacement data");
        Files.createDirectory(directory);
        Files.writeString(path, "broken file");
        storage.saveAddressBook(new AddressBook());
        try (var files = Files.list(directory)) {
            Path backup = files.filter(file -> file.getFileName().toString().contains(".recovery-"))
                    .findFirst().orElseThrow();
            assertEquals("broken file", Files.readString(backup));
        }
    }

    @Test
    public void save_validOrMissingFile_doesNotCreateRecoveryBackup() throws Exception {
        Path path = folder.resolve("contacts.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(path);
        assertTrue(storage.readAddressBook().isEmpty());
        storage.saveAddressBook(getTypicalAddressBook());
        assertEquals(getTypicalAddressBook(), storage.readAddressBook().orElseThrow());
        storage.saveAddressBook(new AddressBook());
        assertTrue(backups().isEmpty());
    }

    @Test
    public void read_afterManualRepair_clearsRecoveryProtection() throws Exception {
        Path path = folder.resolve("contacts.json");
        Files.writeString(path, "broken file");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(path);
        assertThrows(DataLoadingException.class, storage::readAddressBook);
        new JsonAddressBookStorage(path).saveAddressBook(getTypicalAddressBook());
        assertEquals(getTypicalAddressBook(), storage.readAddressBook().orElseThrow());
        storage.saveAddressBook(new AddressBook());
        assertTrue(backups().isEmpty());
    }

    @Test
    public void execute_listAfterFailedLoad_preservesOriginalBeforeSaving() throws Exception {
        Path path = folder.resolve("contacts.json");
        Files.writeString(path, "null");
        StorageManager storage = storage(path);
        assertThrows(DataLoadingException.class, storage::readAddressBook);
        new LogicManager(new ModelManager(), storage).execute("list");
        assertEquals("null", Files.readString(backups().getFirst()));
        assertEquals(new AddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_backupFails_reportsSaveErrorWithoutCreatingDataFile() throws Exception {
        Path path = folder.resolve("contacts.json");
        Files.writeString(path, "null");
        StorageManager storage = storage(path);
        assertThrows(DataLoadingException.class, storage::readAddressBook);
        Files.delete(path);
        LogicManager logic = new LogicManager(new ModelManager(), storage);
        CommandException error = assertThrows(CommandException.class, () -> logic.execute("list"));
        assertTrue(error.getMessage().contains("No data was saved"));
        assertFalse(Files.exists(path));
    }

    private StorageManager storage(Path path) {
        return IntegrationTestFixture.createStorage(new JsonAddressBookStorage(path),
                folder.resolve("preferences.json"));
    }

    private List<Path> backups() throws IOException {
        try (var paths = Files.list(folder)) {
            return paths.filter(path -> path.getFileName().toString().contains(".recovery-")).toList();
        }
    }
}
