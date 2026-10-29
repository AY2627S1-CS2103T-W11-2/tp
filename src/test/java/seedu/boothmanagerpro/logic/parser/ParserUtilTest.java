package seedu.boothmanagerpro.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.boothmanagerpro.testutil.Assert.assertThrows;
import static seedu.boothmanagerpro.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.boothmanagerpro.logic.parser.exceptions.ParseException;
import seedu.boothmanagerpro.model.person.Address;
import seedu.boothmanagerpro.model.person.Email;
import seedu.boothmanagerpro.model.person.Name;
import seedu.boothmanagerpro.model.person.Phone;
import seedu.boothmanagerpro.model.tag.Tag;

public class ParserUtilTest {
    private static final String INVALID_NAME = "R!chel";
    private static final String INVALID_PHONE = "+651234";
    private static final String INVALID_ADDRESS = " ";
    private static final String INVALID_EMAIL = "example.com";
    private static final String INVALID_TAG = "friend/invalid";

    private static final String VALID_NAME = "Rachel Walker";
    private static final String VALID_PHONE = "1234567";
    private static final String VALID_ADDRESS = "123 Main Street #0505";
    private static final String VALID_EMAIL = "rachel@example.com";
    private static final String VALID_TAG_1 = "friend";
    private static final String VALID_TAG_2 = "neighbour";

    private static final String WHITESPACE = " \t\r\n";

    @Test
    public void parseIndex_zeroAndLeadingZeros_handlesBoundaries() throws Exception {
        assertThrows(ParseException.class, ParserUtil.MESSAGE_INVALID_INDEX, () -> ParserUtil.parseIndex("000"));
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("0001"));
        assertThrows(ParseException.class, ParserUtil.MESSAGE_INVALID_INDEX, () -> ParserUtil.parseIndex("abc"));
    }

    @Test
    public void rejectMultipleIndices_rejectsMultipleNumbersAndAcceptsSingleTarget() throws Exception {
        for (String target : new String[]{"1 2", "  1   2  ", "1 -2", "+1 2 3"}) {
            assertThrows(ParseException.class, ParserUtil.MESSAGE_MULTIPLE_INDICES, () ->
                    ParserUtil.rejectMultipleIndices(target));
        }
        ParserUtil.rejectMultipleIndices("1");
        ParserUtil.rejectMultipleIndices("Alicia Tan");
    }

    @Test
    public void rejectNamePrefix_rejectsPrefixAndAcceptsPlainTarget() throws Exception {
        assertThrows(ParseException.class,
                String.format(ParserUtil.MESSAGE_NAME_PREFIX_NOT_ALLOWED, "delete", "delete NAME"), ()
                -> ParserUtil.rejectNamePrefix(" n/Alicia Tan", "delete", "delete NAME"));
        ParserUtil.rejectNamePrefix(" Alicia Tan", "delete", "delete NAME");
    }

    @Test
    public void parseIndex_invalidInput_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseIndex("10 a"));
    }

    @Test
    public void parseIndex_nonPositiveInput_throwsParseException() {
        for (String index : new String[]{"0", "-1", "+0", "-0", "+", Long.toString(Integer.MIN_VALUE)}) {
            assertThrows(ParseException.class, ParserUtil.MESSAGE_INVALID_INDEX, () -> ParserUtil.parseIndex(index));
        }
    }

    @Test
    public void parseIndex_leadingPlusPositiveInput_throwsUnsignedIndexParseException() {
        for (String index : new String[]{"+1", "+001", "+" + Integer.MAX_VALUE}) {
            assertThrows(ParseException.class, ParserUtil.MESSAGE_UNSIGNED_INDEX, () -> ParserUtil.parseIndex(index));
        }
    }

    @Test
    public void parseIndex_tooLargeInput_throwsParseException() {
        for (String index : new String[]{
            Long.toString(Integer.MAX_VALUE + 1L), "99999999999999",
            "+" + (Integer.MAX_VALUE + 1L), "+99999999999999"
        }) {
            assertThrows(ParseException.class, ParserUtil.MESSAGE_INDEX_TOO_LARGE, () -> ParserUtil.parseIndex(index));
        }
    }

    @Test
    public void isIndexLike() {
        for (String target : new String[]{"1", " 0 ", "-1", "+1", "99999999999999"}) {
            assertTrue(ParserUtil.isIndexLike(target));
        }
        for (String target : new String[]{"", "Alicia Tan", "1a", "1 2", "- 1"}) {
            assertFalse(ParserUtil.isIndexLike(target));
        }
    }

    @Test
    public void parseIndex_validInput_success() throws Exception {
        // No whitespaces
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("1"));

        // Leading and trailing whitespaces
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("  1  "));
    }

    @Test
    public void parseName_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseName((String) null));
    }

    @Test
    public void parseName_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseName(INVALID_NAME));
    }

    @Test
    public void parseName_validValueWithoutWhitespace_returnsName() throws Exception {
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(VALID_NAME));
    }

    @Test
    public void parseName_validValueWithWhitespace_returnsTrimmedName() throws Exception {
        String nameWithWhitespace = WHITESPACE + VALID_NAME + WHITESPACE;
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(nameWithWhitespace));
    }

    @Test
    public void parsePhone_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parsePhone((String) null));
    }

    @Test
    public void parsePhone_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parsePhone(INVALID_PHONE));
        assertThrows(ParseException.class,
                ParserUtil.MESSAGE_PHONE_CHARACTERS, ()
                -> ParserUtil.parsePhone("1234 5678 (HP)"));
        assertThrows(ParseException.class, ParserUtil.MESSAGE_PHONE_DIGITS, ()
                -> ParserUtil.parsePhone("123"));
    }

    @Test
    public void parsePhone_validValueWithoutWhitespace_returnsPhone() throws Exception {
        Phone expectedPhone = new Phone(VALID_PHONE);
        assertEquals(expectedPhone, ParserUtil.parsePhone(VALID_PHONE));
    }

    @Test
    public void parsePhone_validValueWithWhitespace_returnsTrimmedPhone() throws Exception {
        String phoneWithWhitespace = WHITESPACE + VALID_PHONE + WHITESPACE;
        Phone expectedPhone = new Phone(VALID_PHONE);
        assertEquals(expectedPhone, ParserUtil.parsePhone(phoneWithWhitespace));
    }

    @Test
    public void parseAddress_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseAddress((String) null));
    }

    @Test
    public void parseAddress_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseAddress(INVALID_ADDRESS));
    }

    @Test
    public void parseAddress_validValueWithoutWhitespace_returnsAddress() throws Exception {
        Address expectedAddress = new Address(VALID_ADDRESS);
        assertEquals(expectedAddress, ParserUtil.parseAddress(VALID_ADDRESS));
    }

    @Test
    public void parseAddress_validValueWithWhitespace_returnsTrimmedAddress() throws Exception {
        String addressWithWhitespace = WHITESPACE + VALID_ADDRESS + WHITESPACE;
        Address expectedAddress = new Address(VALID_ADDRESS);
        assertEquals(expectedAddress, ParserUtil.parseAddress(addressWithWhitespace));
    }

    @Test
    public void parseEmail_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseEmail((String) null));
    }

    @Test
    public void parseEmail_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseEmail(INVALID_EMAIL));
    }

    @Test
    public void parseEmail_validValueWithoutWhitespace_returnsEmail() throws Exception {
        Email expectedEmail = new Email(VALID_EMAIL);
        assertEquals(expectedEmail, ParserUtil.parseEmail(VALID_EMAIL));
    }

    @Test
    public void parseEmail_validValueWithWhitespace_returnsTrimmedEmail() throws Exception {
        String emailWithWhitespace = WHITESPACE + VALID_EMAIL + WHITESPACE;
        Email expectedEmail = new Email(VALID_EMAIL);
        assertEquals(expectedEmail, ParserUtil.parseEmail(emailWithWhitespace));
    }

    @Test
    public void parseTag_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseTag(null));
    }

    @Test
    public void parseTag_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseTag(INVALID_TAG));
    }

    @Test
    public void parseTag_validValueWithoutWhitespace_returnsTag() throws Exception {
        Tag expectedTag = new Tag(VALID_TAG_1);
        assertEquals(expectedTag, ParserUtil.parseTag(VALID_TAG_1));
    }

    @Test
    public void parseTag_validValueWithWhitespace_returnsTrimmedTag() throws Exception {
        String tagWithWhitespace = WHITESPACE + VALID_TAG_1 + WHITESPACE;
        Tag expectedTag = new Tag(VALID_TAG_1);
        assertEquals(expectedTag, ParserUtil.parseTag(tagWithWhitespace));
    }

    @Test
    public void parseTags_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseTags(null));
    }

    @Test
    public void parseTags_collectionWithInvalidTags_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseTags(List.of(VALID_TAG_1, INVALID_TAG)));
    }

    @Test
    public void parseTags_emptyCollection_returnsEmptySet() throws Exception {
        assertTrue(ParserUtil.parseTags(List.of()).isEmpty());
    }

    @Test
    public void parseTags_collectionWithValidTags_returnsTagSet() throws Exception {
        Set<Tag> actualTagSet = ParserUtil.parseTags(List.of(VALID_TAG_1, VALID_TAG_2));
        Set<Tag> expectedTagSet = Set.of(new Tag(VALID_TAG_1), new Tag(VALID_TAG_2));

        assertEquals(expectedTagSet, actualTagSet);
    }
}
