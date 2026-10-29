package seedu.boothmanagerpro.logic.parser;

import static seedu.boothmanagerpro.logic.parser.CommandParserTestUtil.assertInvalidIndexFailure;
import static seedu.boothmanagerpro.logic.parser.CommandParserTestUtil.assertNamePrefixFailure;
import static seedu.boothmanagerpro.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.boothmanagerpro.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.boothmanagerpro.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.boothmanagerpro.logic.commands.UpdateCommand;
import seedu.boothmanagerpro.model.person.ContactUpdate;
import seedu.boothmanagerpro.model.person.EnquiryStatus;
import seedu.boothmanagerpro.model.person.FollowUpDate;
import seedu.boothmanagerpro.model.person.Name;
import seedu.boothmanagerpro.model.person.Phone;
import seedu.boothmanagerpro.model.tag.Tag;

/** Tests the target, field and error-message rules of {@code update}. */
public class UpdateCommandParserTest {
    private static final String MISSING_TARGET = "Specify a contact name or displayed index.\n"
            + UpdateCommand.MESSAGE_USAGE;
    private static final String MISSING_FIELD = "Supply at least one field to update.\n" + UpdateCommand.MESSAGE_USAGE;
    private static final String TARGET_USAGE =
            "update NAME_OR_INDEX FIELD... (n/ only sets a new name, e.g. update Alicia Tan n/Alicia Lim)";

    private final UpdateCommandParser parser = new UpdateCommandParser();

    @Test
    public void parse_indexOrNameTarget_success() {
        ContactUpdate completed = changes(Optional.empty(), Optional.empty(), Optional.of(EnquiryStatus.COMPLETED),
                Optional.empty(), Optional.empty());
        assertParseSuccess(parser, "1 s/Completed", new UpdateCommand(INDEX_FIRST_PERSON, completed));
        assertParseSuccess(parser, "  1   s/ completed ", new UpdateCommand(INDEX_FIRST_PERSON, completed));
        assertParseSuccess(parser, "Alicia Tan s/Completed", new UpdateCommand(new Name("Alicia Tan"), completed));
    }

    @Test
    public void parse_newNameAfterTarget_success() {
        ContactUpdate rename = changes(Optional.of(new Name("Alicia Lim")), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty());
        assertParseSuccess(parser, "Alicia Tan n/Alicia Lim", new UpdateCommand(new Name("Alicia Tan"), rename));
    }

    @Test
    public void parse_phoneWithSeparators_success() {
        ContactUpdate phone = changes(Optional.empty(), Optional.of(new Phone("+6591234567")), Optional.empty(),
                Optional.empty(), Optional.empty());
        assertParseSuccess(parser, "1 p/+65 9123-4567", new UpdateCommand(INDEX_FIRST_PERSON, phone));
    }

    @Test
    public void parse_emptyDateOrTag_clearsField() {
        ContactUpdate clearDate = changes(Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.of(Optional.<FollowUpDate>empty()), Optional.empty());
        ContactUpdate clearTags = changes(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.of(Set.<Tag>of()));
        assertParseSuccess(parser, "1 f/", new UpdateCommand(INDEX_FIRST_PERSON, clearDate));
        assertParseSuccess(parser, "1 t/", new UpdateCommand(INDEX_FIRST_PERSON, clearTags));
    }

    @Test
    public void parse_missingTargetOrField_failure() {
        for (String args : new String[]{"", "   ", "s/Completed"}) {
            assertParseFailure(parser, args, MISSING_TARGET);
        }
        assertParseFailure(parser, "1", MISSING_FIELD);
        assertParseFailure(parser, "Alicia Tan", MISSING_FIELD);
    }

    @Test
    public void parse_repeatedField_failure() {
        assertParseFailure(parser, "1 s/Completed s/In progress",
                "Repeated fields: s/. Keep one value per field; only t/ may repeat.");
        assertParseFailure(parser, "1 p/91234567 e/a@b.com p/98765432",
                "Repeated fields: p/. Keep one value per field; only t/ may repeat.");
    }

    @Test
    public void parse_emptyField_failure() {
        for (String prefix : new String[]{"n/", "c/", "e/", "p/", "m/", "s/"}) {
            assertParseFailure(parser, "1 " + prefix + "  ",
                    "Empty field: " + prefix + ". Supply a value after this prefix.");
        }
        assertParseFailure(parser, "1 t/ t/vip", "Empty field: t/. Supply a value after this prefix.");
    }

    @Test
    public void parse_unknownPrefix_failure() {
        assertParseFailure(parser, "1 a/Somewhere",
                "Unknown field prefix: a/. Use n/, c/, e/, p/, m/, s/, f/, or t/.");
    }

    @Test
    public void parse_invalidPhone_failure() {
        assertParseFailure(parser, "1 p/(65) 6123-4567", "Invalid value for p/: "
                + "Phone number can only contain digits, spaces, hyphens, and a + at the start.");
        assertParseFailure(parser, "1 p/123",
                "Invalid value for p/: Phone number must contain 7 to 15 digits, optionally starting with +.");
    }

    @Test
    public void parse_invalidIndex_failure() {
        assertInvalidIndexFailure(parser, " s/Completed");
    }

    @Test
    public void parse_namePrefixBeforeTarget_failure() {
        for (String args : new String[]{"n/Alicia Tan s/Completed", " n/1 s/Completed", "n/"}) {
            assertNamePrefixFailure(parser, args, "update", TARGET_USAGE);
        }
    }

    /** Returns changes to the given fields only; company, email and contact method are left unchanged. */
    private static ContactUpdate changes(Optional<Name> name, Optional<Phone> phone, Optional<EnquiryStatus> status,
            Optional<Optional<FollowUpDate>> date, Optional<Set<Tag>> tags) {
        return new ContactUpdate(name, Optional.empty(), Optional.empty(), phone, Optional.empty(), status, date,
                tags);
    }
}
