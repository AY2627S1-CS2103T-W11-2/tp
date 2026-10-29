package seedu.boothmanagerpro.logic.parser;

import static seedu.boothmanagerpro.logic.parser.CommandParserTestUtil.assertInvalidIndexFailure;
import static seedu.boothmanagerpro.logic.parser.CommandParserTestUtil.assertNamePrefixFailure;
import static seedu.boothmanagerpro.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.boothmanagerpro.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.boothmanagerpro.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.boothmanagerpro.commons.core.index.Index;
import seedu.boothmanagerpro.logic.commands.TagCommand;
import seedu.boothmanagerpro.model.person.Name;
import seedu.boothmanagerpro.model.tag.Tag;

/** Tests the target, t/TAG field and error-message rules of {@code tag}. */
public class TagCommandParserTest {
    private static final String MISSING_TARGET = "Specify a contact name or displayed index.\n"
            + TagCommand.MESSAGE_USAGE;
    private static final String MISSING_TAG = "Supply at least one t/TAG field.\n" + TagCommand.MESSAGE_USAGE;

    private final TagCommandParser parser = new TagCommandParser();

    @Test
    public void parse_indexOrNameTarget_success() {
        assertParseSuccess(parser, "1 t/vip", new TagCommand(INDEX_FIRST_PERSON, tags("vip")));
        assertParseSuccess(parser, "  12   t/ vip  ", new TagCommand(Index.fromOneBased(12), tags("vip")));
        assertParseSuccess(parser, "Alicia Tan t/vip t/sponsor",
                new TagCommand(new Name("Alicia Tan"), tags("vip", "sponsor")));
        assertParseSuccess(parser, "Raj s/o Kumar t/vip", new TagCommand(new Name("Raj s/o Kumar"), tags("vip")));
    }

    @Test
    public void parse_repeatedTags_collapsedInInputOrder() {
        assertParseSuccess(parser, "1 t/b t/a t/b", new TagCommand(INDEX_FIRST_PERSON, tags("b", "a")));
    }

    @Test
    public void parse_missingTargetOrTag_failure() {
        for (String args : new String[]{"", "   ", "t/vip"}) {
            assertParseFailure(parser, args, MISSING_TARGET);
        }
        assertParseFailure(parser, "1", MISSING_TAG);
        assertParseFailure(parser, "Alicia Tan", MISSING_TAG);
    }

    @Test
    public void parse_emptyOrInvalidTag_failure() {
        assertParseFailure(parser, "1 t/", "Empty field: t/. Supply a value after this prefix.");
        assertParseFailure(parser, "1 t/vip t/  ", "Empty field: t/. Supply a value after this prefix.");
        assertParseFailure(parser, "1 t/" + "a".repeat(31), "Invalid value for t/: " + Tag.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_unknownPrefix_failure() {
        assertParseFailure(parser, "1 c/Acme t/vip", "Unknown field prefix: c/. Use t/.");
        assertParseFailure(parser, "1 t/vip n/Alicia", "Unknown field prefix: n/. Use t/.");
    }

    @Test
    public void parse_invalidIndex_failure() {
        assertInvalidIndexFailure(parser, " t/vip");
    }

    @Test
    public void parse_namePrefixBeforeTarget_failure() {
        for (String args : new String[]{"n/Alicia Tan t/vip", " n/1 t/vip", "n/"}) {
            assertNamePrefixFailure(parser, args, "tag", "tag NAME_OR_INDEX t/TAG [t/TAG]...");
        }
    }

    private static Set<Tag> tags(String... names) {
        Set<Tag> tags = new LinkedHashSet<>();
        Arrays.stream(names).map(Tag::new).forEach(tags::add);
        return tags;
    }
}
