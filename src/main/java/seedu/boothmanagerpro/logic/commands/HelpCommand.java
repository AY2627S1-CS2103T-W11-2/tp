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
                    + "═════════════════════════════\n"
                    + "[ ] optional  |  ... repeatable\n"
                    + "Use an exact full name or a displayed number for NAME_OR_INDEX.",
            formatCommandHelp(1, AddCommand.COMMAND_WORD,
                    "Adds a new exhibitor contact.",
                    "add n/NAME c/COMPANY e/EMAIL p/PHONE [m/METHOD] [s/STATUS] [f/YYYYMMDD] [t/TAG]...",
                    "add n/Alicia Tan c/TechNova e/alicia@example.com p/91234567 "
                            + "m/email s/In progress f/20261025 t/high-priority"),
            formatCommandHelp(2, ListCommand.COMMAND_WORD,
                    "Displays all contacts, optionally sorted by name, company, or enquiry status.",
                    "list [o/name|o/company|o/status]",
                    "list o/status"),
            formatCommandHelp(3, FindCommand.COMMAND_WORD,
                    "Finds contacts by name words or field criteria. Repeated fields are alternatives (OR); "
                            + "different fields must all match (AND).",
                    "find KEYWORD... or find [n/NAME] [c/COMPANY] [e/EMAIL] [p/PHONE] [m/METHOD] "
                            + "[s/STATUS] [t/TAG] [f/YYYYMMDD]...",
                    "find f/20261025 t/high-priority"),
            formatCommandHelp(4, ViewCommand.COMMAND_WORD,
                    "Displays full contact details. If several contacts match, reply with a displayed number.",
                    "view n/NAME",
                    "view n/Alicia Tan"),
            formatCommandHelp(5, UpdateCommand.COMMAND_WORD,
                    "Updates supplied fields. Empty m/ clears the contact method; "
                            + "empty f/ clears the follow-up date; empty t/ clears all tags.",
                    "update NAME_OR_INDEX [n/NAME] [c/COMPANY] [e/EMAIL] [p/PHONE] [m/METHOD] "
                            + "[s/STATUS] [f/YYYYMMDD] [t/TAG]...",
                    "update Alicia Tan s/Completed f/20261028"),
            formatCommandHelp(6, TagCommand.COMMAND_WORD,
                    "Toggles one or more tags on a contact using their full name or displayed index.",
                    "tag NAME_OR_INDEX t/TAG [t/TAG]...",
                    "tag Alicia Tan t/high-priority t/sponsor"),
            formatCommandHelp(7, DeleteCommand.COMMAND_WORD,
                    "Deletes a contact using their full name or displayed index.",
                    "delete NAME_OR_INDEX",
                    "delete Alicia Tan"),
            formatCommandHelp(8, ClearCommand.COMMAND_WORD,
                    "Deletes every contact only after the explicit confirm keyword.",
                    "clear confirm",
                    "clear confirm"),
            formatCommandHelp(9, HelpCommand.COMMAND_WORD,
                    "Displays this list of available commands.",
                    "help",
                    "help"),
            formatCommandHelp(10, ExitCommand.COMMAND_WORD,
                    "Closes BoothManagerPro.",
                    "exit",
                    "exit"),
            "Quick controls\n"
                    + "─────────────────────────────\n"
                    + "Enter: run command  |  Tab: autofill or focus output\n"
                    + "F1: show help       |  Ctrl/Cmd+L: focus command box\n"
                    + "Use numbers from list, find, or view for update, tag, and delete.\n\n"
                    + "Full User Guide:\n"
                    + "https://github.com/AY2627S1-CS2103T-W11-2/tp/blob/master/docs/UserGuide.md");

    /** Formats one command using the consistent help-entry layout. */
    private static String formatCommandHelp(int number, String function, String description, String format,
            String example) {
        return number + ". " + function.toUpperCase() + "\n"
                + "   Description: " + description + "\n"
                + "   Format:      " + format + "\n"
                + "   Example:     " + example;
    }

    @Override
    public CommandResult execute(Model model) {
        return new CommandResult(SHOWING_HELP_MESSAGE);
    }
}
