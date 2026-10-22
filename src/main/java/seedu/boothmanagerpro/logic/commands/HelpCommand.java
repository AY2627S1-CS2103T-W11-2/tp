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
            "BoothManagerPro commands",
            AddCommand.MESSAGE_USAGE, ListCommand.MESSAGE_USAGE, FindCommand.MESSAGE_USAGE,
            ViewCommand.MESSAGE_USAGE, UpdateCommand.MESSAGE_USAGE, TagCommand.MESSAGE_USAGE,
            EditCommand.MESSAGE_USAGE, DeleteCommand.MESSAGE_USAGE,
            "clear: Deletes all contacts.", HelpCommand.MESSAGE_USAGE, "exit: Exits the application.",
            "Enter runs a command. Ctrl+L (Command+L on macOS) focuses command input. F1 shows help.",
            "User Guide: https://github.com/AY2627S1-CS2103T-W11-2/tp/blob/master/docs/UserGuide.md");

    @Override
    public CommandResult execute(Model model) {
        return new CommandResult(SHOWING_HELP_MESSAGE);
    }
}
