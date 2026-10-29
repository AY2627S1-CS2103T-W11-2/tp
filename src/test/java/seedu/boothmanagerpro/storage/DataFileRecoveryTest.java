package seedu.boothmanagerpro.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class DataFileRecoveryTest {
    @TempDir
    public Path folder;

    @Test
    public void removeIncompleteBackup_cleanupFails_addsSuppressedException() throws Exception {
        Path nonEmptyDirectory = Files.createDirectory(folder.resolve("incomplete-backup"));
        Files.writeString(nonEmptyDirectory.resolve("content"), "prevents directory deletion");
        IOException originalFailure = new IOException("copy failed");
        Method cleanup = DataFileRecovery.class.getDeclaredMethod(
                "removeIncompleteBackup", Path.class, IOException.class);
        cleanup.setAccessible(true);

        cleanup.invoke(new DataFileRecovery(), nonEmptyDirectory, originalFailure);

        assertEquals(1, originalFailure.getSuppressed().length);
        assertInstanceOf(DirectoryNotEmptyException.class, originalFailure.getSuppressed()[0]);
    }
}
