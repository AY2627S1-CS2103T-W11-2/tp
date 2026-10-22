package seedu.boothmanagerpro.logic.commands;

import seedu.boothmanagerpro.model.Model;

/**
 * Formats full help instructions for every command for display.
 */
public class HelpCommand extends Command {

    public static final String COMMAND_WORD = "help";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Shows program usage instructions.\n"
            + "Example: " + COMMAND_WORD;

    public static final String SHOWING_HELP_MESSAGE = String.join("\n\n",
            "BoothManagerPro commands\n"
                    + "═════════════════════════════",
            formatCommandHelp(1, AddCommand.COMMAND_WORD,
                    "Adds a new exhibitor contact.",
                    "add n/Alicia Tan c/TechNova e/alicia@example.com p/91234567"),
            formatCommandHelp(2, ListCommand.COMMAND_WORD,
                    "Displays all saved exhibitor contacts.",
                    "list"),
            formatCommandHelp(3, FindCommand.COMMAND_WORD,
                    "Finds contacts using name keywords or exact fields, including follow-up date (f/YYYYMMDD).",
                    "find n/Alicia t/technology"),
            formatCommandHelp(4, ViewCommand.COMMAND_WORD,
                    "Displays the full details of a contact matching the given name.",
                    "view n/Alicia Tan"),
            formatCommandHelp(5, EditCommand.COMMAND_WORD,
                    "Updates one or more fields of a contact at a displayed index.",
                    "edit 1 p/91234567 e/alicia@example.com"),
            formatCommandHelp(6, DeleteCommand.COMMAND_WORD,
                    "Deletes a contact using their full name or displayed index.",
                    "delete Alicia Tan"),
            formatCommandHelp(7, ClearCommand.COMMAND_WORD,
                    "Deletes every contact from the address book.",
                    "clear"),
            formatCommandHelp(8, HelpCommand.COMMAND_WORD,
                    "Displays this list of available commands.",
                    "help"),
            formatCommandHelp(9, ExitCommand.COMMAND_WORD,
                    "Closes BoothManagerPro.",
                    "exit"),
            "─────────────────────────────\n"
                    + "Full User Guide:\n"
                    + "https://github.com/AY2627S1-CS2103T-W11-2/tp/blob/master/docs/UserGuide.md");

    /** Formats one command using the consistent help-entry layout. */
    private static String formatCommandHelp(int number, String function, String description, String example) {
        return number + ". " + function.toUpperCase() + "\n"
                + "   Description: " + description + "\n"
                + "   Example:     " + example;
    }

    @Override
    public CommandResult execute(Model model) {
        return new CommandResult(SHOWING_HELP_MESSAGE);
    }
}
