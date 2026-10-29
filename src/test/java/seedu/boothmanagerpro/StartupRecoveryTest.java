package seedu.boothmanagerpro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.boothmanagerpro.model.Model;
import seedu.boothmanagerpro.model.UserPrefs;
import seedu.boothmanagerpro.storage.JsonAddressBookStorage;
import seedu.boothmanagerpro.storage.JsonUserPrefsStorage;
import seedu.boothmanagerpro.storage.StorageManager;

/** Checks actual application startup fallbacks without opening a JavaFX window. */
public class StartupRecoveryTest {
    @TempDir
    public Path folder;

    @Test
    public void initModel_nullContactData_startsEmptyAndProtectsOriginal() throws Exception {
        Path path = folder.resolve("contacts.json");
        Files.writeString(path, "null");
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(path),
                new JsonUserPrefsStorage(folder.resolve("preferences.json")));
        Model model = new MainApp().initModelManager(storage, new UserPrefs());
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertEquals("null", Files.readString(path));
        storage.saveAddressBook(model.getAddressBook());
        try (var files = Files.list(folder)) {
            Path backup = files.filter(file -> file.getFileName().toString().contains(".recovery-"))
                    .findFirst().orElseThrow();
            assertEquals("null", Files.readString(backup));
        }
    }

    @Test
    public void initPrefs_nullGuiSettings_usesDefaultsAndWritesValidPreferences() throws Exception {
        Path path = folder.resolve("preferences.json");
        Files.writeString(path, "{\"guiSettings\": null}");
        JsonUserPrefsStorage storage = new JsonUserPrefsStorage(path);
        assertEquals(new UserPrefs(), new MainApp().initPrefs(storage));
        assertEquals(new UserPrefs(), storage.readUserPrefs().orElseThrow());
    }

    @Test
    public void initPrefs_nestedMissingFolder_createsPreferencesFile() throws Exception {
        Path path = folder.resolve("config/nested/preferences.json");
        JsonUserPrefsStorage storage = new JsonUserPrefsStorage(path);
        assertEquals(new UserPrefs(), new MainApp().initPrefs(storage));
        assertEquals(new UserPrefs(), storage.readUserPrefs().orElseThrow());
    }

    @Test
    public void initPrefs_nullPreferences_usesDefaultsAndWritesValidPreferences() throws Exception {
        Path path = folder.resolve("preferences.json");
        Files.writeString(path, "null");
        JsonUserPrefsStorage storage = new JsonUserPrefsStorage(path);
        assertEquals(new UserPrefs(), new MainApp().initPrefs(storage));
        assertEquals(new UserPrefs(), storage.readUserPrefs().orElseThrow());
    }
}
