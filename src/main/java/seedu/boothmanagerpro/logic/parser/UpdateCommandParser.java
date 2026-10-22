package seedu.boothmanagerpro.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_STATUS;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.boothmanagerpro.logic.commands.UpdateCommand;
import seedu.boothmanagerpro.logic.parser.exceptions.ParseException;
import seedu.boothmanagerpro.model.person.EnquiryStatus;

/** Parses a full name or displayed index followed by one enquiry status field. */
public class UpdateCommandParser implements Parser<UpdateCommand> {
    private static final Pattern FIELD_PREFIX = Pattern.compile("(?<!\\S)([^\\s/]+/)");

    @Override
    public UpdateCommand parse(String args) throws ParseException {
        requireNonNull(args);
        Matcher prefixes = FIELD_PREFIX.matcher(args);
        while (prefixes.find()) {
            if (!PREFIX_STATUS.getPrefix().equals(prefixes.group(1))) {
                throw new ParseException("Unknown field prefix: " + prefixes.group(1) + ". Use s/STATUS.");
            }
        }
        ArgumentMultimap fields = ArgumentTokenizer.tokenize(args, PREFIX_STATUS);
        fields.verifyNoDuplicatePrefixesFor(PREFIX_STATUS);
        String target = fields.getPreamble();
        if (target.isBlank() || fields.getValue(PREFIX_STATUS).isEmpty()) {
            throw new ParseException("Specify a contact name or displayed index and s/STATUS.\n"
                    + UpdateCommand.MESSAGE_USAGE);
        }
        EnquiryStatus status;
        try {
            status = EnquiryStatus.fromString(fields.getValue(PREFIX_STATUS).orElseThrow());
        } catch (IllegalArgumentException e) {
            throw new ParseException("Invalid value for s/: " + e.getMessage(), e);
        }
        return target.matches("[0-9]+")
                ? new UpdateCommand(ParserUtil.parseIndex(target), status)
                : new UpdateCommand(ParserUtil.parseName(target), status);
    }
}
