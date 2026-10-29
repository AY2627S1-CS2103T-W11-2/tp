package seedu.boothmanagerpro.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Checks complete replacement and failure behavior using actual filesystem paths. */
public class AtomicFileWriterTest {
    @TempDir
    public Path folder;

    @Test
    public void write_existingFile_replacesEntireContentAndRemovesTemporaryFile() throws Exception {
        Path target = folder.resolve("contacts.json");
        Files.writeString(target, "old contact data that is longer than the replacement");
        AtomicFileWriter.write(target, "Café");
        assertEquals("Café", Files.readString(target));
        assertOnlyFile(target);
    }

    @Test
    public void write_newFile_createsCompleteFile() throws Exception {
        Path target = folder.resolve("new.json");
        AtomicFileWriter.write(target, "{\"persons\":[]}");
        assertEquals("{\"persons\":[]}", Files.readString(target));
        assertOnlyFile(target);
    }

    @Test
    public void write_directoryTarget_preservesExistingContents() throws Exception {
        Path target = Files.createDirectory(folder.resolve("contacts.json"));
        Path existing = target.resolve("original.json");
        Files.writeString(existing, "original");
        assertThrows(IOException.class, () -> AtomicFileWriter.write(target, "replacement"));
        assertEquals("original", Files.readString(existing));
        assertOnlyFile(target);
    }

    @Test
    public void write_missingParents_createsFoldersAndFile() throws Exception {
        Path target = folder.resolve("missing/nested/preferences.json");
        AtomicFileWriter.write(target, "replacement");
        assertEquals("replacement", Files.readString(target));
        try (var files = Files.list(target.getParent())) {
            assertEquals(List.of(target), files.toList(), "No temporary file is left behind");
        }
    }

    @Test
    public void write_encodingFailure_preservesOriginalAndRemovesTemporaryFile() throws Exception {
        Path target = folder.resolve("contacts.json");
        Files.writeString(target, "original contacts");
        String malformedText = String.valueOf((char) 0xD800);
        assertThrows(IOException.class, () -> AtomicFileWriter.write(target, malformedText));
        assertEquals("original contacts", Files.readString(target));
        assertOnlyFile(target);
    }

    private void assertOnlyFile(Path expected) throws IOException {
        try (var files = Files.list(folder)) {
            assertEquals(List.of(expected), files.toList());
        }
    }

    @Test
    public void cleanup_failure_preservesOriginalExceptionAndRecordsCleanupFailure() throws Exception {
        Path temporary = Files.createDirectory(folder.resolve("incomplete"));
        Files.writeString(temporary.resolve("child"), "cannot delete a nonempty directory");
        IOException original = new IOException("save failed");
        AtomicFileWriter.cleanupAfterFailure(temporary, original);
        assertEquals("save failed", original.getMessage());
        assertEquals(1, original.getSuppressed().length);
        assertEquals(true, original.getSuppressed()[0] instanceof IOException);
        assertEquals("cannot delete a nonempty directory", Files.readString(temporary.resolve("child")));
    }
}
