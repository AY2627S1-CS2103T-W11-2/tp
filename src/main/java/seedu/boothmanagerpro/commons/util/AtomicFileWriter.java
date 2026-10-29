package seedu.boothmanagerpro.commons.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Writes complete data beside its destination before replacing the existing file. */
final class AtomicFileWriter {
    private AtomicFileWriter() {
    }

    /**
     * Leaves the destination untouched if writing or atomic replacement fails.
     * An unsupported atomic move is reported as a save failure rather than risking a partial overwrite.
     */
    static void write(Path destination, String content) throws IOException {
        Path target = destination.toAbsolutePath().normalize();
        if (Files.exists(target)) {
            target = target.toRealPath();
            if (!Files.isWritable(target) || !Files.isRegularFile(target)) {
                throw new AccessDeniedException(target.toString());
            }
        }
        FileUtil.createParentDirsOfFile(target);
        Path temporary = Files.createTempFile(target.getParent(), ".boothmanagerpro-save-", ".tmp");
        try {
            Files.writeString(temporary, content, StandardCharsets.UTF_8);
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException failure) {
            cleanupAfterFailure(temporary, failure);
            throw failure;
        }
    }

    /** Retains the original save failure even if an incomplete temporary file cannot be removed. */
    static void cleanupAfterFailure(Path temporary, IOException failure) {
        try {
            Files.deleteIfExists(temporary);
        } catch (IOException cleanupFailure) {
            failure.addSuppressed(cleanupFailure);
        }
    }
}
