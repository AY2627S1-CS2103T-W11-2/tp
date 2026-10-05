package seedu.boothmanagerpro.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.boothmanagerpro.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.boothmanagerpro.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.boothmanagerpro.testutil.TypicalPersons.ALICE;
import static seedu.boothmanagerpro.testutil.TypicalPersons.BENSON;
import static seedu.boothmanagerpro.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.boothmanagerpro.logic.commands.FindCommand;
import seedu.boothmanagerpro.logic.parser.exceptions.ParseException;
import seedu.boothmanagerpro.model.Model;
import seedu.boothmanagerpro.model.ModelManager;
import seedu.boothmanagerpro.model.UserPrefs;
import seedu.boothmanagerpro.model.person.ContactMatchesFieldsPredicate;

/** Exercises field parsing, matching, and model interaction together. */
public class FindFieldsTest {
    private final FindCommandParser parser = new FindCommandParser();

    @Test
    public void parse_repeatedValues_normalisesAndDeduplicates() {
        FindCommand expected = new FindCommand(new ContactMatchesFieldsPredicate(Map.of(
                "n/", Set.of("alice pauline"), "t/", Set.of("friends", "vip"))));
        assertParseSuccess(parser, " n/Alice Pauline n/ALICE PAULINE t/Friends t/vip ", expected);
        assertParseSuccess(parser, "\tn/Alice Pauline\tt/Friends\nt/vip", expected);
    }

    @Test
    public void parse_invalidFields_rejectsInput() {
        for (String input : List.of("c/Acme", "s/new", "x/foo", "n/Alice Pauline x/foo")) {
            assertParseFailure(parser, input, FindCommandParser.MESSAGE_UNSUPPORTED_FIELD);
        }
        for (String input : List.of("n/", "e/ ", "p/ ", "t/", "n/Alice Pauline t/")) {
            assertParseFailure(parser, input, FindCommandParser.MESSAGE_EMPTY_VALUE);
        }
        assertParseFailure(parser, "Alice n/Alice Pauline", FindCommandParser.MESSAGE_UNPREFIXED_VALUE);
    }

    @Test
    public void parse_invalidValues_rejectsInput() throws Exception {
        for (String input : List.of("p/12", "p/abc", "e/not-an-email", "n/@", "t/two words")) {
            assertThrows(ParseException.class, () -> parser.parse(input));
        }
    }

    @Test
    public void execute_exactName_doesNotMatchPartialName() throws Exception {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        parser.parse("n/alice pauline").execute(model);
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        parser.parse("n/Alice").execute(model);
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_sameFieldOrDifferentFieldAnd_searchesWholeAddressBook() throws Exception {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        parser.parse("n/Alice Pauline n/Benson Meier t/FRIENDS").execute(model);
        assertEquals(List.of(ALICE, BENSON), model.getFilteredPersonList());
        parser.parse("n/Alice Pauline t/owesMoney").execute(model);
        assertTrue(model.getFilteredPersonList().isEmpty());
        parser.parse("e/JOHND@EXAMPLE.COM p/9876-5432").execute(model);
        assertEquals(List.of(BENSON), model.getFilteredPersonList());
        assertEquals(getTypicalAddressBook(), model.getAddressBook());
    }

    @Test
    public void execute_phoneWhitespaceAndTagAlternatives_match() throws Exception {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        parser.parse("p/9435 1253 t/vip t/friends").execute(model);
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        parser.parse("t/friend").execute(model);
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void predicate_copiesCriteria_andHasValueEquality() {
        Set<String> values = new HashSet<>(Set.of("alice pauline"));
        Map<String, Set<String>> criteria = new HashMap<>(Map.of("n/", values));
        ContactMatchesFieldsPredicate predicate = new ContactMatchesFieldsPredicate(criteria);
        values.clear();
        criteria.clear();
        ContactMatchesFieldsPredicate copy = new ContactMatchesFieldsPredicate(
                Map.of("n/", Set.of("alice pauline")));
        assertTrue(predicate.test(ALICE));
        assertFalse(predicate.test(BENSON));
        assertEquals(copy, predicate);
        assertEquals(copy.hashCode(), predicate.hashCode());
        assertFalse(predicate.equals(null));
        assertFalse(predicate.equals("other"));
    }
}
