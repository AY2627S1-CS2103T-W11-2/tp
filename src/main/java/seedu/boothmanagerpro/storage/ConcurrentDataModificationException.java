package seedu.boothmanagerpro.storage;

import java.io.IOException;

/**
 * Signals that the address book file changed after this application session loaded it.
 */
public class ConcurrentDataModificationException extends IOException {
    public static final String MESSAGE = "The data file was changed by another BoothManagerPro window or process. "
            + "Your command was not saved. Restart BoothManagerPro to load the latest data, then try again.";

    public ConcurrentDataModificationException() {
        super(MESSAGE);
    }
}
