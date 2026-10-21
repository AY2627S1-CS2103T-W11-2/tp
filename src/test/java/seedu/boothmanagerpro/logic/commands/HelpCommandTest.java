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
                + "   Example:     add n/Alicia Tan c/TechNova e/alicia@example.com p/91234567"));
        assertTrue(SHOWING_HELP_MESSAGE.contains("4. VIEW\n"
                + "   Description: Displays the full details of a contact matching the given name.\n"
                + "   Example:     view n/Alicia Tan"));
        assertTrue(SHOWING_HELP_MESSAGE.contains("9. EXIT\n"
                + "   Description: Closes BoothManagerPro.\n"
                + "   Example:     exit"));
        assertFalse(SHOWING_HELP_MESSAGE.contains("Keyboard shortcuts"));
    }
}
