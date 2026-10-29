package seedu.boothmanagerpro.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.boothmanagerpro.logic.Messages.MESSAGE_PERSONS_LISTED_OVERVIEW;
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

import seedu.boothmanagerpro.logic.Messages;
import seedu.boothmanagerpro.logic.commands.FindCommand;
import seedu.boothmanagerpro.logic.commands.ListCommand;
import seedu.boothmanagerpro.logic.parser.exceptions.ParseException;
import seedu.boothmanagerpro.model.Model;
import seedu.boothmanagerpro.model.ModelManager;
import seedu.boothmanagerpro.model.UserPrefs;
import seedu.boothmanagerpro.model.person.Company;
import seedu.boothmanagerpro.model.person.ContactMatchesFieldsPredicate;
import seedu.boothmanagerpro.model.person.ContactMethod;
import seedu.boothmanagerpro.model.person.EnquiryStatus;
import seedu.boothmanagerpro.model.person.Name;
import seedu.boothmanagerpro.model.person.Person;
import seedu.boothmanagerpro.model.tag.Tag;
import seedu.boothmanagerpro.testutil.PersonBuilder;

/** Exercises field parsing, matching, and model interaction together. */
public class FindFieldsTest {
    private final FindCommandParser parser = new FindCommandParser();

    @Test
    public void parse_companyNameWithSlash_accepted() {
        FindCommand expected = new FindCommand(new ContactMatchesFieldsPredicate(Map.of(
                "c/", Set.of("acme design/ build"))));
        assertParseSuccess(parser, "c/Acme Design/ Build", expected);
    }

    @Test
    public void parse_repeatedValues_normalisesAndDeduplicates() {
        FindCommand expected = new FindCommand(new ContactMatchesFieldsPredicate(Map.of(
                "n/", Set.of("alice pauline"), "t/", Set.of("friends", "vip"))));
        assertParseSuccess(parser, " n/Alice Pauline n/ALICE PAULINE t/Friends t/vip ", expected);
        assertParseSuccess(parser, "\tn/Alice Pauline\tt/Friends\nt/vip", expected);
    }

    @Test
    public void parse_invalidFields_rejectsInput() {
        for (String input : List.of("x/foo", "x/ ", "a/123", "o/name", "n/Alice Pauline x/foo")) {
            assertParseFailure(parser, input, FindCommandParser.MESSAGE_UNSUPPORTED_FIELD);
        }
        for (String input : List.of("c/", "n/", "e/ ", "p/ ", "m/", "s/ ", "t/", "n/Alice Pauline t/")) {
            assertParseFailure(parser, input, FindCommandParser.MESSAGE_EMPTY_VALUE);
        }
        assertParseFailure(parser, "Alice n/Alice Pauline", FindCommandParser.MESSAGE_UNPREFIXED_VALUE);
    }

    @Test
    public void parse_invalidValues_rejectsInput() {
        assertParseFailure(parser, "n/@", "Invalid value for n/: " + Name.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "c/" + "A".repeat(101), "Invalid value for c/: " + Company.MESSAGE_CONSTRAINTS);
        for (String badEmail : List.of("e/@", "e/bad email", "e/a@@b.com", "e/a!b", "e/" + "a".repeat(255))) {
            assertParseFailure(parser, badEmail,
                    "Invalid value for e/: " + FindCommandParser.MESSAGE_INVALID_EMAIL_SEARCH);
        }
        assertParseFailure(parser, "p/1234\t567",
                "Invalid value for p/: Phone number can only contain digits, spaces, hyphens, and a + at the start.");
        assertParseFailure(parser, "p/abc",
                "Invalid value for p/: Phone number can only contain digits, spaces, hyphens, and a + at the start.");
        assertParseFailure(parser, "p/(65) 6123-4567",
                "Invalid value for p/: Phone number can only contain digits, spaces, hyphens, and a + at the start.");
        for (String badPhone : List.of("p/+", "p/---", "p/1234567890123456", "p/" + "1  ".repeat(11))) {
            assertParseFailure(parser, badPhone,
                    "Invalid value for p/: " + FindCommandParser.MESSAGE_INVALID_PHONE_SEARCH);
        }
        assertParseFailure(parser, "m/fax", "Invalid value for m/: " + ContactMethod.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "s/new", "Invalid value for s/: " + EnquiryStatus.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "t/tag/with/slash", "Invalid value for t/: " + Tag.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void execute_name_matchesFullNameOrWholeWordButNotSubstring() throws Exception {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        parser.parse("n/alice pauline").execute(model);
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        parser.parse("n/Alice   Pauline").execute(model);
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        parser.parse("n/Alice").execute(model);
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
        parser.parse("n/Ali").execute(model);
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

    @Test
    public void predicate_equalityChecksSelfAndDifferentCriteria() {
        ContactMatchesFieldsPredicate predicate = new ContactMatchesFieldsPredicate(
                Map.of("n/", Set.of("alice pauline")));
        assertTrue(predicate.equals(predicate));
        assertFalse(predicate.equals(new ContactMatchesFieldsPredicate(
                Map.of("n/", Set.of("benson meier")))));
        assertEquals("{n/=[alice pauline]}", predicate.toString());
    }

    @Test
    public void predicate_unknownFieldDoesNotMatch() {
        ContactMatchesFieldsPredicate predicate = new ContactMatchesFieldsPredicate(
                Map.of("x/", Set.of("alice pauline")));
        assertFalse(predicate.test(ALICE));
    }

    @Test
    public void execute_partialCompanyEmailPhoneAndStatusMethodFilters_matchExpectedContacts() throws Exception {
        Person alicia = new PersonBuilder().withName("Alicia Tan").withCompany("TechNova Solutions")
                .withEmail("alicia@technova.com").withPhone("87438807")
                .withContactMethod("email").withEnquiryStatus("In progress").build();
        Person ben = new PersonBuilder().withName("Ben Lim").withCompany("TechNova Pte Ltd")
                .withEmail("ben@technova.com").withPhone("+6591234567")
                .withContactMethod("phone").withEnquiryStatus("Completed").build();
        Person chloe = new PersonBuilder().withName("Chloe Ng").withCompany("GreenWorks")
                .withEmail("chloe@greenworks.org").withPhone("98765432")
                .withEnquiryStatus("Needs follow-up").build();
        Model model = new ModelManager();
        model.addPerson(alicia);
        model.addPerson(ben);
        model.addPerson(chloe);

        // Partial company search
        parser.parse("c/TechNova").execute(model);
        assertEquals(List.of(alicia, ben), model.getFilteredPersonList());
        parser.parse("c/Solutions").execute(model);
        assertEquals(List.of(alicia), model.getFilteredPersonList());

        // Domain and partial email search
        parser.parse("e/@technova.com").execute(model);
        assertEquals(List.of(alicia, ben), model.getFilteredPersonList());
        parser.parse("e/alicia@").execute(model);
        assertEquals(List.of(alicia), model.getFilteredPersonList());
        parser.parse("e/greenworks").execute(model);
        assertEquals(List.of(chloe), model.getFilteredPersonList());

        // Partial phone search (including country code, spaces, and hyphens)
        parser.parse("p/8743").execute(model);
        assertEquals(List.of(alicia), model.getFilteredPersonList());
        parser.parse("p/+65 9123").execute(model);
        assertEquals(List.of(ben), model.getFilteredPersonList());
        parser.parse("p/9123-45").execute(model);
        assertEquals(List.of(ben), model.getFilteredPersonList());

        // Enquiry status (s/) and contact method (m/) filters with OR/AND and absent-method handling
        parser.parse("s/in progress").execute(model);
        assertEquals(List.of(alicia), model.getFilteredPersonList());
        parser.parse("s/In progress s/Completed").execute(model);
        assertEquals(List.of(alicia, ben), model.getFilteredPersonList());
        parser.parse("m/EMAIL").execute(model);
        assertEquals(List.of(alicia), model.getFilteredPersonList());
        parser.parse("m/email m/phone").execute(model);
        assertEquals(List.of(alicia, ben), model.getFilteredPersonList());
        parser.parse("c/TechNova s/Completed m/phone").execute(model);
        assertEquals(List.of(ben), model.getFilteredPersonList());
        parser.parse("s/Cancelled").execute(model);
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void parse_invalidInputPreservesExistingResultsAndContacts() throws Exception {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        parser.parse("n/Alice Pauline").execute(model);
        for (String input : List.of("n/", "e/bad email", "x/test", "s/new", "m/fax")) {
            assertThrows(ParseException.class, () -> parser.parse(input).execute(model));
            assertEquals(List.of(ALICE), model.getFilteredPersonList());
            assertEquals(getTypicalAddressBook(), model.getAddressBook());
        }
    }

    @Test
    public void execute_resultCountAndListReset() throws Exception {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        assertEquals(String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, 1) + "\n\n1. "
                        + Messages.formatContactDetails(ALICE),
                parser.parse("e/alice@example.com").execute(model).getFeedbackToUser());
        assertEquals(String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, 0),
                parser.parse("e/missing@example.com").execute(model).getFeedbackToUser());
        new ListCommand().execute(model);
        assertEquals(7, model.getFilteredPersonList().size());
    }
}
