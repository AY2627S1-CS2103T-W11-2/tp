package seedu.boothmanagerpro.logic.parser;

import static seedu.boothmanagerpro.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.boothmanagerpro.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.boothmanagerpro.logic.commands.ViewCommand;
import seedu.boothmanagerpro.model.person.Name;

/**
 * Tests the view syntax and name validation from the MVP specification.
 */
public class ViewCommandParserTest {
    private final ViewCommandParser parser = new ViewCommandParser();

    @Test
    public void parse_nameRules_matchSharedModel() {
        String unicodeName = "\uD801\uDC00".repeat(80);
        assertParseSuccess(parser, "n/" + unicodeName, new ViewCommand(new Name(unicodeName)));
        assertParseFailure(parser, "n/" + unicodeName + "A", Name.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "n/Alicia2", Name.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_validName_success() {
        for (String name : new String[]{"Alicia Tan", "Anne-Marie O'Neil", "Dr. Lee", "O’Neil", "Élodie", "'Ali",
            "Raj s/o Kumar", "Priya d/o Raman", "Raj S/O Kumar", "A".repeat(80)}) {
            assertParseSuccess(parser, " n/ " + name + "  ", new ViewCommand(new Name(name)));
        }
    }

    @Test
    public void parse_missingOrWrongField_failure() {
        for (String args : new String[]{"", " ", "c/TechNova", "p/91234567", "s/Completed"}) {
            assertParseFailure(parser, args, ViewCommandParser.MESSAGE_REQUIRED_NAME);
        }
    }

    @Test
    public void parse_repeatedName_failure() {
        assertParseFailure(parser, "n/Alicia n/Tan", ViewCommandParser.MESSAGE_DUPLICATE_FIELD);
        assertParseFailure(parser, "n/Alicia\tn/Alicia", ViewCommandParser.MESSAGE_DUPLICATE_FIELD);
    }

    @Test
    public void parse_unknownPrefixOrUnprefixedValue_failure() {
        for (String args : new String[]{"x/test", "n/Alice c/Acme",
            "n/Alicia p/12345678", "m/email n/Alicia", "n/Alicia x/test", "n/Alicia i/1",
            "n/Raj s/o Kumar c/Acme", "n/Raj s/o", "n/Raj s/o/Kumar"}) {
            assertParseFailure(parser, args, ViewCommandParser.MESSAGE_UNKNOWN_FIELD);
        }
    }

    @Test
    public void parse_missingNamePrefix_failure() {
        for (String args : new String[]{"alex", "Alicia Tan", "1", "Alicia n/Tan"}) {
            assertParseFailure(parser, args,
                    "view needs the n/ prefix before the name. Use: view n/NAME, e.g. view n/Alicia Tan");
        }
    }

    @Test
    public void parse_emptyName_failure() {
        for (String args : new String[]{"n/", " n/ ", "n/\t"}) {
            assertParseFailure(parser, args, "Name cannot be empty. Use: view n/NAME, e.g. view n/Alicia Tan");
        }
    }

    @Test
    public void parse_invalidName_failure() {
        for (String name : new String[]{"123", "Alicia2", "...", "---", "Alicia!",
            "John/Doe", "Alicia/extra", "/", "Raj s/o Kumar Jr/Sr", "Raj2 s/o Kumar", "A".repeat(81)}) {
            assertParseFailure(parser, "n/" + name, ViewCommandParser.MESSAGE_INVALID_NAME);
        }
    }
}
