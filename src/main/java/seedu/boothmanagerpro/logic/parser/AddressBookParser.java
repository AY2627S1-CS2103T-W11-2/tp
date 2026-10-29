package seedu.boothmanagerpro.logic.parser;

import static seedu.boothmanagerpro.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.boothmanagerpro.logic.Messages.MESSAGE_UNKNOWN_COMMAND;

import java.util.Locale;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.boothmanagerpro.commons.core.LogsCenter;
import seedu.boothmanagerpro.logic.commands.AddCommand;
import seedu.boothmanagerpro.logic.commands.ClearCommand;
import seedu.boothmanagerpro.logic.commands.Command;
import seedu.boothmanagerpro.logic.commands.DeleteCommand;
import seedu.boothmanagerpro.logic.commands.ExitCommand;
import seedu.boothmanagerpro.logic.commands.FindCommand;
import seedu.boothmanagerpro.logic.commands.HelpCommand;
import seedu.boothmanagerpro.logic.commands.ListCommand;
import seedu.boothmanagerpro.logic.commands.TagCommand;
import seedu.boothmanagerpro.logic.commands.UpdateCommand;
import seedu.boothmanagerpro.logic.commands.ViewCommand;
import seedu.boothmanagerpro.logic.parser.exceptions.ParseException;

/**
 * Parses user input.
 */
public class AddressBookParser {

    /**
     * Used for initial separation of command word and args.
     */
    private static final Pattern BASIC_COMMAND_FORMAT = Pattern.compile("(?s)(?<commandWord>\\S+)(?<arguments>.*)");
    private static final Logger logger = LogsCenter.getLogger(AddressBookParser.class);

    /**
     * Parses user input into command for execution.
     *
     * @param userInput full user input string
     * @return the command based on the user input
     * @throws ParseException if the user input does not conform to the expected format
     */
    public Command parseCommand(String userInput) throws ParseException {
        final Matcher matcher = BASIC_COMMAND_FORMAT.matcher(userInput.trim());
        if (!matcher.matches()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE));
        }

        final String commandWord = matcher.group("commandWord").toLowerCase(Locale.ROOT);
        final String arguments = matcher.group("arguments");

        // Note to developers: Change LOG_LEVEL in LogsCenter to enable lower level (i.e., FINE, FINER and lower)
        // log messages such as the one below.
        // Lower level log messages are used sparingly to minimize noise in the code.
        logger.fine("Command word: " + commandWord + "; Arguments: " + arguments);

        return switch (commandWord) {
            case AddCommand.COMMAND_WORD -> new AddCommandParser().parse(arguments);
            case UpdateCommand.COMMAND_WORD -> new UpdateCommandParser().parse(arguments);
            case TagCommand.COMMAND_WORD -> new TagCommandParser().parse(arguments);
            case DeleteCommand.COMMAND_WORD -> new DeleteCommandParser().parse(arguments);
            case ClearCommand.COMMAND_WORD -> new ClearCommandParser().parse(arguments);
            case FindCommand.COMMAND_WORD -> new FindCommandParser().parse(arguments);
            case ListCommand.COMMAND_WORD -> new ListCommandParser().parse(arguments);
            case ViewCommand.COMMAND_WORD -> new ViewCommandParser().parse(arguments);
            case ExitCommand.COMMAND_WORD -> parseNoArgumentCommand(arguments, ExitCommand.MESSAGE_USAGE,
                    new ExitCommand());
            case HelpCommand.COMMAND_WORD -> parseNoArgumentCommand(arguments, HelpCommand.MESSAGE_USAGE,
                    new HelpCommand());
            default -> {
                logger.finer("This user input caused a ParseException: " + userInput);
                throw new ParseException(MESSAGE_UNKNOWN_COMMAND);
            }
        };
    }

    private Command parseNoArgumentCommand(String arguments, String usage, Command command) throws ParseException {
        if (!arguments.isBlank()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, usage));
        }
        return command;
    }

}
