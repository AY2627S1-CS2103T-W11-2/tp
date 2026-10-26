package seedu.boothmanagerpro.logic.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.boothmanagerpro.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.boothmanagerpro.logic.commands.HelpCommand.SHOWING_HELP_MESSAGE;

import org.junit.jupiter.api.Test;

import seedu.boothmanagerpro.model.Model;
import seedu.boothmanagerpro.model.ModelManager;

public class HelpCommandTest {
    private Model model = new ModelManager();
    private Model expectedModel = new ModelManager();

    @Test
    public void execute_help_success() {
        CommandResult expectedCommandResult = new CommandResult(SHOWING_HELP_MESSAGE);
        assertCommandSuccess(new HelpCommand(), model, expectedCommandResult, expectedModel);
    }

    @Test
    public void showingHelpMessage_usesConsistentEntryFormat() {
        assertTrue(SHOWING_HELP_MESSAGE.contains("1. ADD\n"
                + "   Description: Adds a new exhibitor contact.\n"
                + "   Format:      add n/NAME c/COMPANY e/EMAIL p/PHONE [m/METHOD] [s/STATUS] [f/YYYYMMDD] "
                + "[t/TAG]...\n"
                + "   Example:     add n/Alicia Tan c/TechNova e/alicia@example.com p/91234567 "
                + "m/email s/In progress f/20261025 t/high-priority"));
        assertTrue(SHOWING_HELP_MESSAGE.contains("4. VIEW\n"
                + "   Description: Displays the full details of a contact matching the given name.\n"
                + "   Format:      view n/NAME\n"
                + "   Example:     view n/Alicia Tan"));
        assertTrue(SHOWING_HELP_MESSAGE.contains("6. TAG\n"
                + "   Description: Toggles one or more tags on a contact using their full name or displayed index.\n"
                + "   Format:      tag NAME_OR_INDEX t/TAG [t/TAG]...\n"
                + "   Example:     tag Alicia Tan t/high-priority t/sponsor"));
        assertTrue(SHOWING_HELP_MESSAGE.contains("11. EXIT\n"
                + "   Description: Closes BoothManagerPro.\n"
                + "   Format:      exit\n"
                + "   Example:     exit"));
        assertTrue(SHOWING_HELP_MESSAGE.contains("list [o/name|o/company|o/status]"));
        assertTrue(SHOWING_HELP_MESSAGE.contains("find f/20261025 t/high-priority"));
        assertTrue(SHOWING_HELP_MESSAGE.contains("update Alicia Tan s/Completed f/20261028"));
        assertFalse(SHOWING_HELP_MESSAGE.contains("Keyboard shortcuts"));
    }
}
