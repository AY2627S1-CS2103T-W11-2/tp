package seedu.boothmanagerpro.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import seedu.boothmanagerpro.commons.core.LogsCenter;

/** Preserves files that failed to load before a later save can replace their contents. */
class DataFileRecovery {
    private static final Logger logger = LogsCenter.getLogger(DataFileRecovery.class);
    private final Set<Path> failedFiles = new HashSet<>();

    /** Records a failed load without modifying the original file. */
    void markFailed(Path file) {
        failedFiles.add(file.toAbsolutePath().normalize());
    }

    /** A successful reload no longer needs recovery protection. */
    void markLoaded(Path file) {
        failedFiles.remove(file.toAbsolutePath().normalize());
    }

    /** Returns whether a failed load still requires the original file to be preserved. */
    boolean isPending(Path file) {
        return failedFiles.contains(file.toAbsolutePath().normalize());
    }

    /**
     * Makes a unique byte-for-byte backup beside the original before permitting a save.
     * Failure leaves the path protected, so retrying cannot bypass the backup requirement.
     */
    void preserveBeforeSave(Path file) throws IOException {
        Path original = file.toAbsolutePath().normalize();
        if (!failedFiles.contains(original)) {
            return;
        }
        Path backup = null;
        try {
            backup = Files.createTempFile(original.getParent(),
                    original.getFileName() + ".recovery-", ".json");
            Files.copy(original, backup, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            removeIncompleteBackup(backup, e);
            throw new IOException("Could not preserve the original data file. No data was saved.", e);
        }
        failedFiles.remove(original);
        logger.warning("Preserved unreadable contact data at " + backup);
    }

    private void removeIncompleteBackup(Path backup, IOException failure) {
        if (backup == null) {
            return;
        }
        try {
            Files.deleteIfExists(backup);
        } catch (IOException cleanupFailure) {
            failure.addSuppressed(cleanupFailure);
        }
    }
}
