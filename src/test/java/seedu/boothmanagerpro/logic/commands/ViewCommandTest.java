package seedu.boothmanagerpro.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.boothmanagerpro.logic.commands.CommandTestUtil.assertCommandFailure;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.boothmanagerpro.model.AddressBook;
import seedu.boothmanagerpro.model.Model;
import seedu.boothmanagerpro.model.ModelManager;
import seedu.boothmanagerpro.model.UserPrefs;
import seedu.boothmanagerpro.model.person.Name;
import seedu.boothmanagerpro.model.person.NameContainsKeywordsPredicate;
import seedu.boothmanagerpro.model.person.Person;
import seedu.boothmanagerpro.testutil.PersonBuilder;

/**
 * Tests viewing full exhibitor records while updating the displayed matches without modifying saved data.
 */
public class ViewCommandTest {
    private final Person alicia = new PersonBuilder().withName("Alicia Tan").withCompany("TechNova Pte Ltd")
            .withEmail("alicia@technova.com").withPhone("91234567").withContactMethod("email")
            .withTags("technology", "high-priority").build();
    private final Model model = new ModelManager();

    @Test
    public void execute_exactNameIgnoringCaseAndOuterSpaces_returnsSpecifiedDetails() throws Exception {
        model.addPerson(alicia);
        model.updateFilteredPersonList(person -> false);
        Model expected = new ModelManager(model.getAddressBook(), new UserPrefs());
        expected.updateFilteredPersonList(person -> person.equals(alicia));
        CommandResult result = new ViewCommand(new Name("alicia tan ")).execute(model);
        assertEquals("Exhibitor contact found:\nAlicia Tan at TechNova Pte Ltd\nEmail: alicia@technova.com"
                + "\nPhone: 91234567\nContact method: email\nTags: high-priority, technology",
                result.getFeedbackToUser());
        assertTrue(result.getViewChoices().isEmpty());
        assertEquals(expected, model);
    }

    @Test
    public void execute_sameNameDifferentCompany_returnsNumberedChoices() throws Exception {
        Person second = new PersonBuilder(alicia).withCompany("TechIndustries Pte Ltd")
                .withEmail("alicia@techindustries.com").build();
        model.addPerson(alicia);
        model.addPerson(second);
        AddressBook original = new AddressBook(model.getAddressBook());
        CommandResult result = new ViewCommand(alicia.getName()).execute(model);
        assertEquals(List.of(alicia, second), result.getViewChoices());
        assertEquals("2 contacts matching \"Alicia Tan\" found:\n1. Alicia Tan at TechNova Pte Ltd\n"
                + "2. Alicia Tan at TechIndustries Pte Ltd\nPlease input the corresponding number (1 to 2) "
                + "for the contact you would like to view.", result.getFeedbackToUser());
        assertEquals(original, model.getAddressBook());
    }

    @Test
    public void execute_noMatch_throwsWithoutMutation() {
        assertCommandFailure(new ViewCommand(alicia.getName()), model,
                String.format(ViewCommand.MESSAGE_NO_MATCH, alicia.getName()));
        model.addPerson(alicia);
        assertCommandFailure(new ViewCommand(new Name("Nobody")), model,
                String.format(ViewCommand.MESSAGE_NO_MATCH, "Nobody"));
    }

    @Test
    public void execute_keywordMatchesExactAndLongerNames_likeFind() throws Exception {
        Person first = new PersonBuilder().withName("rhineson").withEmail("first@example.com").build();
        Person second = new PersonBuilder().withName("rhineson kok").withEmail("second@example.com").build();
        Person third = new PersonBuilder().withName("David Lee").withEmail("third@example.com").build();
        model.addPerson(first);
        model.addPerson(second);
        model.addPerson(third);
        new FindCommand(new NameContainsKeywordsPredicate(List.of("RHINESON"))).execute(model);
        List<Person> findMatches = List.copyOf(model.getFilteredPersonList());
        CommandResult result = new ViewCommand(new Name("RHINESON")).execute(model);
        assertEquals(List.of(first, second), result.getViewChoices());
        assertEquals(findMatches, model.getFilteredPersonList());

        CommandResult multipleKeywords = new ViewCommand(new Name("rhineson David")).execute(model);
        assertEquals(List.of(first, second, third), multipleKeywords.getViewChoices());
        assertEquals(3, model.getAddressBook().getPersonList().size());
        assertCommandFailure(new ViewCommand(new Name("rhin")), model,
                String.format(ViewCommand.MESSAGE_NO_MATCH, "rhin"));
    }

    @Test
    public void createDetailsResult_legacyAndMissingOptionalFields_displaysAbsenceClearly() {
        Person legacy = new PersonBuilder().withTags().build();
        String result = ViewCommand.createDetailsResult(legacy).getFeedbackToUser();
        assertTrue(result.contains("at Not specified"));
        assertTrue(result.contains("Contact method: Not specified"));
        assertTrue(result.endsWith("Tags: None"));
    }

    @Test
    public void equalsAndToString() {
        ViewCommand command = new ViewCommand(alicia.getName());
        assertEquals(command, command);
        assertEquals(command, new ViewCommand(alicia.getName()));
        assertNotEquals(command, new ViewCommand(new Name("Other")));
        assertNotEquals(command, null);
        assertNotEquals(command, 1);
        assertEquals(ViewCommand.class.getCanonicalName() + "{name=Alicia Tan}", command.toString());
    }
}
