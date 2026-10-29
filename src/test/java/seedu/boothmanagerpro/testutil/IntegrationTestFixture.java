package seedu.boothmanagerpro.testutil;

import java.nio.file.Path;

import seedu.boothmanagerpro.logic.LogicManager;
import seedu.boothmanagerpro.model.Model;
import seedu.boothmanagerpro.storage.JsonAddressBookStorage;
import seedu.boothmanagerpro.storage.JsonUserPrefsStorage;
import seedu.boothmanagerpro.storage.StorageManager;

/** Wires real command processing and JSON storage while preserving each test's model and temporary paths. */
public final class IntegrationTestFixture {
    private IntegrationTestFixture() {
    }

    /** Creates real storage using the supplied contacts file and isolated preferences path. */
    public static StorageManager createStorage(JsonAddressBookStorage contacts, Path preferences) {
        return new StorageManager(contacts, new JsonUserPrefsStorage(preferences));
    }

    /** Creates command processing backed by the same storage instance the test inspects. */
    public static LogicManager createLogic(Model model, JsonAddressBookStorage contacts, Path preferences) {
        return new LogicManager(model, createStorage(contacts, preferences));
    }
}
