package seedu.boothmanagerpro.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_COMPANY;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.boothmanagerpro.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.boothmanagerpro.logic.parser.exceptions.ParseException;
import seedu.boothmanagerpro.model.person.Company;
import seedu.boothmanagerpro.model.person.Phone;
import seedu.boothmanagerpro.model.tag.Tag;

/** Tests the shared add and update field conversion and its prefix-specific messages. */
public class ContactFieldParserTest {
    private static final String PHONE_CHARACTERS = "Invalid value for p/: "
            + ParserUtil.MESSAGE_PHONE_CHARACTERS;
    private static final String PHONE_DIGITS =
            "Invalid value for p/: " + ParserUtil.MESSAGE_PHONE_DIGITS;

    @Test
    public void requireValue_blank_failure() {
        for (String value : new String[]{"", "  ", "\t"}) {
            assertThrows(ParseException.class, "Empty field: c/. Supply a value after this prefix.", () ->
                    ContactFieldParser.requireValue(PREFIX_COMPANY, value));
        }
    }

    @Test
    public void parseValue_validValue_returnsTrimmedValue() throws Exception {
        assertEquals(new Company("TechNova"), ContactFieldParser.parseValue(PREFIX_COMPANY, " TechNova ",
                Company::new));
    }

    @Test
    public void parseValue_invalidValue_namesPrefix() {
        assertThrows(ParseException.class, "Invalid value for t/: " + Tag.MESSAGE_CONSTRAINTS, () ->
                ContactFieldParser.parseValue(PREFIX_TAG, "a/b", Tag::new));
        assertThrows(ParseException.class, "Empty field: t/. Supply a value after this prefix.", () ->
                ContactFieldParser.parseValue(PREFIX_TAG, " ", Tag::new));
    }

    @Test
    public void parsePhone_validFormats_preserved() throws Exception {
        assertEquals(new Phone("9123 4567"), ContactFieldParser.parsePhone(" 9123 4567 "));
        assertEquals(new Phone("+65 9123-4567"), ContactFieldParser.parsePhone("+65 9123-4567"));
    }

    @Test
    public void parsePhone_disallowedCharacter_failure() {
        for (String phone : new String[]{"1234 5678 (HP)", "abc1234567", "9123\t4567"}) {
            assertThrows(ParseException.class, PHONE_CHARACTERS, () -> ContactFieldParser.parsePhone(phone));
        }
    }

    @Test
    public void parsePhone_wrongDigitCount_failure() {
        for (String phone : new String[]{"123456", "+123456", "1".repeat(16), "9 1 2 3", "9123456+", "+65+91234567"}) {
            assertThrows(ParseException.class, PHONE_DIGITS, () -> ContactFieldParser.parsePhone(phone));
        }
    }

    @Test
    public void parsePhone_blank_failure() {
        assertThrows(ParseException.class, "Empty field: p/. Supply a value after this prefix.", () ->
                ContactFieldParser.parsePhone("  "));
    }
}
