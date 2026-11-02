package seedu.boothmanagerpro.logic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.boothmanagerpro.logic.commands.exceptions.CommandException;
import seedu.boothmanagerpro.logic.parser.exceptions.ParseException;
import seedu.boothmanagerpro.model.ModelManager;
import seedu.boothmanagerpro.storage.JsonAddressBookStorage;
import seedu.boothmanagerpro.testutil.IntegrationTestFixture;

/** Checks application command logs at every level without changing feedback or persisted contact data. */
public class ContactLoggingPrivacyTest {
    @TempDir
    public Path folder;

    @Test
    public void commandLogs_keepDiagnosticsWithoutContactFields() throws Exception {
        List<String> messages = new ArrayList<>();
        Handler capture = new Handler() {
            @Override
            public void publish(LogRecord record) {
                messages.add(record.getMessage());
            }

            @Override
            public void flush() {}

            @Override
            public void close() {}
        };
        Map<Logger, Level> levels = new HashMap<>();
        seedu.boothmanagerpro.commons.core.LogsCenter.getLogger(ContactLoggingPrivacyTest.class);
        Logger parent = Logger.getLogger("ab3");
        parent.addHandler(capture);
        for (String name : List.of("logic.LogicManager", "logic.parser.AddressBookParser",
                "logic.parser.AddCommandParser", "logic.parser.UpdateCommandParser",
                "logic.commands.AddCommand", "logic.commands.UpdateCommand", "logic.commands.FindCommand")) {
            Logger logger = Logger.getLogger("ab3." + name.substring(name.lastIndexOf('.') + 1));
            levels.put(logger, logger.getLevel());
            logger.setLevel(Level.ALL);
        }
        try {
            ModelManager model = new ModelManager();
            JsonAddressBookStorage storage = new JsonAddressBookStorage(folder.resolve("contacts.json"));
            LogicManager logic = IntegrationTestFixture.createLogic(model, storage, folder.resolve("prefs.json"));
            String add = "add n/Private Person c/Secret Expo e/private@example.com p/91234567 t/private-tag";
            logic.execute(add);
            assertThrows(CommandException.class, () -> logic.execute(add));
            logic.execute("find e/private@example.com");
            logic.execute("update Private Person p/98765432");
            assertThrows(CommandException.class, () -> logic.execute("update Private Person p/98765432"));
            assertThrows(CommandException.class, () -> logic.execute("update Unknown Person s/Completed"));
            assertThrows(ParseException.class, () -> logic.execute("private@example.com"));
            assertTrue(logic.execute("view n/Private Person").getFeedbackToUser().contains("98765432"));
            assertTrue(storage.readAddressBook().orElseThrow().getPersonList().getFirst()
                    .getEmail().value.equals("private@example.com"));
            String text = String.join("\n", messages);
            for (String sensitive : List.of("Private Person", "Secret Expo", "private@example.com", "91234567",
                    "98765432", "private-tag", "Unknown Person")) {
                assertFalse(text.contains(sensitive), "Contact data leaked into log: " + sensitive);
            }
            assertTrue(text.contains("added"));
            assertTrue(text.contains("updated"));
            assertTrue(text.contains("duplicate"));
        } finally {
            parent.removeHandler(capture);
            levels.forEach(Logger::setLevel);
        }
    }

    @Test
    public void malformedSavedData_reportsFailureWithoutCopyingFileContentsIntoLogs() throws Exception {
        String privateValue = "privateContactMarker";
        String contents = "{\"persons\": " + privateValue + "}";
        Path dataFile = folder.resolve("malformed.json");
        java.nio.file.Files.writeString(dataFile, contents);
        List<String> messages = new ArrayList<>();
        seedu.boothmanagerpro.commons.core.LogsCenter.getLogger(ContactLoggingPrivacyTest.class);
        Logger parent = Logger.getLogger("ab3");
        Handler capture = new Handler() {
            @Override
            public void publish(LogRecord record) {
                messages.add(record.getMessage());
            }

            @Override
            public void flush() {}

            @Override
            public void close() {}
        };
        parent.addHandler(capture);
        try {
            JsonAddressBookStorage storage = new JsonAddressBookStorage(dataFile);
            assertThrows(seedu.boothmanagerpro.commons.exceptions.DataLoadingException.class,
                    storage::readAddressBook);
            assertTrue(storage.isRecoveryPending());
            assertTrue(java.nio.file.Files.readString(dataFile).equals(contents));
            String logged = String.join("\n", messages);
            assertFalse(logged.contains(privateValue));
            assertTrue(logged.contains(dataFile.toString()));
        } finally {
            parent.removeHandler(capture);
        }
    }
}
