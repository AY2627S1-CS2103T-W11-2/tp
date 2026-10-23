package seedu.boothmanagerpro.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.boothmanagerpro.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

import seedu.boothmanagerpro.commons.core.LogsCenter;
import seedu.boothmanagerpro.logic.commands.FindCommand;
import seedu.boothmanagerpro.logic.parser.FindCommandParser;
import seedu.boothmanagerpro.logic.parser.exceptions.ParseException;
import seedu.boothmanagerpro.model.ModelManager;
import seedu.boothmanagerpro.model.UserPrefs;
import seedu.boothmanagerpro.model.person.Person;

/** Checks search log outcomes without recording contact data or changing global log settings. */
public class FindLoggingTest {
    @Test
    public void parse_validKeywordAndFieldSearch_logsSuccessWithoutInput() throws Exception {
        assertTrue(FindCommandParser.class.desiredAssertionStatus());
        try (LogCapture capture = new LogCapture(FindCommandParser.class)) {
            FindCommandParser parser = new FindCommandParser();
            parser.parse("PrivateName");
            parser.parse("f/20261025");
            assertEquals(List.of("Parsing contact search.", "Contact search parsed successfully.",
                    "Parsing contact search.", "Contact search parsed successfully."), capture.messages);
        }
    }

    @Test
    public void parse_invalidDate_doesNotLogSuccess() {
        try (LogCapture capture = new LogCapture(FindCommandParser.class)) {
            assertThrows(ParseException.class, () -> new FindCommandParser().parse("f/20260230"));
            assertEquals(List.of("Parsing contact search."), capture.messages);
        }
    }

    @Test
    public void execute_success_logsActualCountWithoutContactDetails() {
        ModelManager model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        int contactCount = model.getAddressBook().getPersonList().size();
        try (LogCapture capture = new LogCapture(FindCommand.class)) {
            new FindCommand(person -> false).execute(model);
            new FindCommand(person -> true).execute(model);
            assertEquals(List.of("Contact search completed: 0 matches.",
                    "Contact search completed: " + contactCount + " matches."), capture.messages);
        }
    }

    @Test
    public void execute_filterFails_doesNotLogCompletion() {
        ModelManager model = new ModelManager(getTypicalAddressBook(), new UserPrefs()) {
            @Override
            public void updateFilteredPersonList(Predicate<Person> predicate) {
                throw new IllegalStateException("Simulated filter failure");
            }
        };
        try (LogCapture capture = new LogCapture(FindCommand.class)) {
            assertThrows(IllegalStateException.class, () -> new FindCommand(person -> true).execute(model));
            assertTrue(capture.messages.isEmpty());
        }
    }

    /** Captures one class logger and restores its prior configuration after each test. */
    private static final class LogCapture extends Handler implements AutoCloseable {
        private final List<String> messages = new ArrayList<>();
        private final Logger logger;
        private final Level previousLevel;
        private final boolean previousUseParentHandlers;

        private LogCapture(Class<?> source) {
            logger = LogsCenter.getLogger(source);
            previousLevel = logger.getLevel();
            previousUseParentHandlers = logger.getUseParentHandlers();
            setLevel(Level.ALL);
            logger.setLevel(Level.ALL);
            logger.setUseParentHandlers(false);
            logger.addHandler(this);
        }

        @Override
        public void publish(LogRecord record) {
            messages.add(record.getMessage());
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
            logger.removeHandler(this);
            logger.setLevel(previousLevel);
            logger.setUseParentHandlers(previousUseParentHandlers);
        }
    }
}
