package seedu.boothmanagerpro.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

import seedu.boothmanagerpro.commons.core.LogsCenter;
import seedu.boothmanagerpro.commons.exceptions.DataLoadingException;
import seedu.boothmanagerpro.commons.exceptions.IllegalValueException;
import seedu.boothmanagerpro.commons.util.FileUtil;
import seedu.boothmanagerpro.commons.util.JsonUtil;
import seedu.boothmanagerpro.model.ReadOnlyAddressBook;

/**
 * A class to access AddressBook data stored as a JSON file on the hard disk.
 */
public class JsonAddressBookStorage {

    private static final Logger logger = LogsCenter.getLogger(JsonAddressBookStorage.class);
    private static final ConcurrentMap<Path, Object> JVM_FILE_LOCKS = new ConcurrentHashMap<>();

    private Path filePath;
    private FileState lastKnownFileState;
    private final DataFileRecovery recovery = new DataFileRecovery();

    public JsonAddressBookStorage(Path filePath) {
        this.filePath = filePath;
    }

    public Path getAddressBookFilePath() {
        return filePath;
    }

    /** Returns whether the configured path needs its failed-load contents preserved before saving. */
    public boolean isRecoveryPending() {
        return recovery.isPending(filePath);
    }

    /**
     * Returns AddressBook data as a {@link ReadOnlyAddressBook}.
     * Returns {@code Optional.empty()} if storage file is not found.
     *
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyAddressBook> readAddressBook() throws DataLoadingException {
        return readAddressBook(filePath);
    }

    /**
     * Similar to {@link #readAddressBook()}.
     *
     * @param filePath location of the data. Cannot be null.
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyAddressBook> readAddressBook(Path filePath) throws DataLoadingException {
        requireNonNull(filePath);

        Path normalisedPath = normalise(filePath);
        if (!isConfiguredPath(normalisedPath)) {
            return readAddressBookFile(normalisedPath);
        }
        synchronized (getJvmFileLock(normalisedPath)) {
            try (FileChannel lockChannel = openLockChannel(normalisedPath);
                    FileLock ignored = lockChannel.lock()) {
                lastKnownFileState = FileState.capture(normalisedPath);
                return readAddressBookFile(normalisedPath);
            } catch (IOException ioe) {
                throw new DataLoadingException(ioe);
            }
        }
    }

    /**
     * Saves the given {@link ReadOnlyAddressBook} to the storage.
     * @param addressBook cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
        saveAddressBook(addressBook, filePath);
    }

    /**
     * Similar to {@link #saveAddressBook(ReadOnlyAddressBook)}.
     *
     * @param filePath location of the data. Cannot be null.
     */
    public void saveAddressBook(ReadOnlyAddressBook addressBook, Path filePath) throws IOException {
        requireNonNull(addressBook);
        requireNonNull(filePath);

        Path normalisedPath = normalise(filePath);
        if (!isConfiguredPath(normalisedPath)) {
            writeAfterPreserving(addressBook, normalisedPath);
            return;
        }
        synchronized (getJvmFileLock(normalisedPath)) {
            try (FileChannel lockChannel = openLockChannel(normalisedPath);
                    FileLock ignored = lockChannel.lock()) {
                FileState currentFileState = FileState.capture(normalisedPath);
                if (lastKnownFileState != null && !lastKnownFileState.hasSameContents(currentFileState)) {
                    throw new ConcurrentDataModificationException();
                }
                writeAfterPreserving(addressBook, normalisedPath);
                lastKnownFileState = FileState.capture(normalisedPath);
            }
        }
    }

    /**
     * Backs up a file that failed to load, then writes the address book atomically. Only missing parent folders are
     * created beforehand, so a failed write never leaves an empty data file behind.
     */
    private void writeAfterPreserving(ReadOnlyAddressBook addressBook, Path path) throws IOException {
        recovery.preserveBeforeSave(path);
        FileUtil.createParentDirsOfFile(path);
        JsonUtil.saveJsonFile(new JsonSerializableAddressBook(addressBook), path);
    }

    /** Reads and converts one address book file while tracking whether failed data needs recovery protection. */
    private Optional<ReadOnlyAddressBook> readAddressBookFile(Path path) throws DataLoadingException {
        try {
            Optional<JsonSerializableAddressBook> jsonAddressBook = JsonUtil.readJsonFile(
                    path, JsonSerializableAddressBook.class);
            Optional<ReadOnlyAddressBook> loaded = jsonAddressBook.isEmpty()
                    ? Optional.empty() : Optional.of(jsonAddressBook.get().toModelType());
            recovery.markLoaded(path);
            return loaded;
        } catch (IllegalValueException ive) {
            recovery.markFailed(path);
            logger.info("Illegal values found in " + path + ": " + ive.getMessage());
            throw new DataLoadingException(ive);
        } catch (DataLoadingException e) {
            recovery.markFailed(path);
            throw e;
        }
    }

    /** Returns a stable absolute form so separate storage objects coordinate on the same path. */
    private static Path normalise(Path path) {
        return path.toAbsolutePath().normalize();
    }

    private boolean isConfiguredPath(Path path) {
        return normalise(filePath).equals(path);
    }

    /** Serializes access within one JVM before the operating-system file lock coordinates separate processes. */
    private static Object getJvmFileLock(Path path) {
        return JVM_FILE_LOCKS.computeIfAbsent(path, unused -> new Object());
    }

    /** Opens the process lock shared by every application instance using this data path. */
    private static FileChannel openLockChannel(Path addressBookPath) throws IOException {
        Path lockDirectory = Path.of(System.getProperty("java.io.tmpdir"), "boothmanagerpro-locks");
        Files.createDirectories(lockDirectory);
        String lockName = UUID.nameUUIDFromBytes(addressBookPath.toString()
                .getBytes(StandardCharsets.UTF_8)) + ".lock";
        Path lockPath = lockDirectory.resolve(lockName);
        return FileChannel.open(lockPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    }

    /** Immutable byte-for-byte snapshot used to detect external changes without relying on timestamps. */
    private static final class FileState {
        private final boolean exists;
        private final byte[] contents;

        private FileState(boolean exists, byte[] contents) {
            this.exists = exists;
            this.contents = contents;
        }

        private static FileState capture(Path path) throws IOException {
            return Files.exists(path)
                    ? new FileState(true, Files.readAllBytes(path))
                    : new FileState(false, new byte[0]);
        }

        private boolean hasSameContents(FileState other) {
            return exists == other.exists && Arrays.equals(contents, other.contents);
        }
    }

}
