package seedu.boothmanagerpro.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import seedu.boothmanagerpro.commons.exceptions.DataLoadingException;
import seedu.boothmanagerpro.commons.util.JsonUtil;
import seedu.boothmanagerpro.model.ReadOnlyUserPrefs;
import seedu.boothmanagerpro.model.UserPrefs;

/**
 * A class to access UserPrefs stored on the hard disk as a JSON file
 */
public class JsonUserPrefsStorage {

    private Path filePath;

    public JsonUserPrefsStorage(Path filePath) {
        this.filePath = filePath;
    }

    public Path getUserPrefsFilePath() {
        return filePath;
    }

    /**
     * Returns UserPrefs data from storage.
     * Returns {@code Optional.empty()} if storage file is not found.
     *
     * @throws DataLoadingException if the loading of data from preference file failed.
     */
    public Optional<UserPrefs> readUserPrefs() throws DataLoadingException {
        return readUserPrefs(filePath);
    }

    /**
     * Similar to {@link #readUserPrefs()}
     * @param prefsFilePath location of the data. Cannot be null.
     * @throws DataLoadingException if the file format is not as expected.
     */
    public Optional<UserPrefs> readUserPrefs(Path prefsFilePath) throws DataLoadingException {
        Optional<UserPrefs> prefs = JsonUtil.readJsonFile(prefsFilePath, UserPrefs.class);
        // Fields are filled in directly, bypassing setter checks, so a null section must be rejected here.
        if (prefs.isPresent() && prefs.get().getGuiSettings() == null) {
            throw new DataLoadingException(new IOException("Preference file has no GUI settings."));
        }
        return prefs;
    }

    /**
     * Saves the given {@link seedu.boothmanagerpro.model.ReadOnlyUserPrefs} to the storage.
     * @param userPrefs cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    public void saveUserPrefs(ReadOnlyUserPrefs userPrefs) throws IOException {
        JsonUtil.saveJsonFile(userPrefs, filePath);
    }

}
