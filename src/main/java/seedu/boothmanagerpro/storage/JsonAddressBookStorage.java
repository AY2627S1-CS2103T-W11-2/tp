package seedu.boothmanagerpro.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Optional;
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

    public JsonAddressBookStorage(Path filePath) {
        this.filePath = filePath;
    }

    public Path getAddressBookFilePath() {
        return filePath;
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
                FileState fileState = FileState.capture(normalisedPath);
                lastKnownFileState = fileState;
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
            FileUtil.createIfMissing(normalisedPath);
            JsonUtil.saveJsonFile(new JsonSerializableAddressBook(addressBook), normalisedPath);
            return;
        }
        synchronized (getJvmFileLock(normalisedPath)) {
            try (FileChannel lockChannel = openLockChannel(normalisedPath);
                    FileLock ignored = lockChannel.lock()) {
                FileState currentFileState = FileState.capture(normalisedPath);
                if (lastKnownFileState != null && !lastKnownFileState.equals(currentFileState)) {
                    throw new ConcurrentDataModificationException();
                }

                FileUtil.createIfMissing(normalisedPath);
                JsonUtil.saveJsonFile(new JsonSerializableAddressBook(addressBook), normalisedPath);
                lastKnownFileState = FileState.capture(normalisedPath);
            }
        }
    }

    /** Reads and converts one address book file without changing concurrency state. */
    private Optional<ReadOnlyAddressBook> readAddressBookFile(Path path) throws DataLoadingException {
        Optional<JsonSerializableAddressBook> jsonAddressBook = JsonUtil.readJsonFile(
                path, JsonSerializableAddressBook.class);
        if (jsonAddressBook.isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(jsonAddressBook.get().toModelType());
        } catch (IllegalValueException ive) {
            logger.info("Illegal values found in " + path + ": " + ive.getMessage());
            throw new DataLoadingException(ive);
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

    /** Opens the persistent sibling lock file shared by every application instance using this data path. */
    private static FileChannel openLockChannel(Path addressBookPath) throws IOException {
        Path lockPath = addressBookPath.resolveSibling("." + addressBookPath.getFileName() + ".lock");
        Path parent = lockPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
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

        @Override
        public boolean equals(Object other) {
            return other == this || other instanceof FileState state
                    && exists == state.exists && Arrays.equals(contents, state.contents);
        }

        @Override
        public int hashCode() {
            return 31 * Boolean.hashCode(exists) + Arrays.hashCode(contents);
        }
    }

}
