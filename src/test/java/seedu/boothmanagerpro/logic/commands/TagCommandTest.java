package seedu.boothmanagerpro.logic.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.boothmanagerpro.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.boothmanagerpro.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.boothmanagerpro.testutil.Assert.assertThrows;
import static seedu.boothmanagerpro.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.boothmanagerpro.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.boothmanagerpro.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.boothmanagerpro.commons.core.index.Index;
import seedu.boothmanagerpro.logic.Messages;
import seedu.boothmanagerpro.model.Model;
import seedu.boothmanagerpro.model.ModelManager;
import seedu.boothmanagerpro.model.UserPrefs;
import seedu.boothmanagerpro.model.person.Name;
import seedu.boothmanagerpro.model.person.Person;
import seedu.boothmanagerpro.model.tag.Tag;
import seedu.boothmanagerpro.testutil.PersonBuilder;

public class TagCommandTest {

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new TagCommand((Index) null, Set.of(new Tag("vip"))));
        assertThrows(NullPointerException.class, () -> new TagCommand((Name) null, Set.of(new Tag("vip"))));
        assertThrows(NullPointerException.class, () -> new TagCommand(INDEX_FIRST_PERSON, null));
        assertThrows(NullPointerException.class, () -> new TagCommand(new Name("Alice Pauline"), null));
    }

    @Test
    public void execute_validIndex_togglesTagsSuccess() {
        // ALICE initially has tag "friends"; toggling "friends" and "vip" removes "friends" and adds "vip"
        Person target = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Set<Tag> toggleTags = new LinkedHashSet<>(List.of(new Tag("friends"), new Tag("vip")));
        TagCommand tagCommand = new TagCommand(INDEX_FIRST_PERSON, toggleTags);

        Person updatedPerson = new PersonBuilder(target).withTags("vip").build();
        String expectedMessage = String.format(
                "Updated tags for %s. Added: %s. Removed: %s. Current tags: %s.",
                AddCommand.formatIdentity(updatedPerson), "vip", "friends", "vip");

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(target, updatedPerson);

        assertCommandSuccess(tagCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_validNameCaseInsensitive_addsTagsSuccess() {
        Person target = model.getFilteredPersonList().get(2); // CARL has no tags initially
        TagCommand tagCommand = new TagCommand(new Name("carl kurz"), Set.of(new Tag("sponsor")));

        Person updatedPerson = new PersonBuilder(target).withTags("sponsor").build();
        String expectedMessage = String.format(
                "Updated tags for %s. Added: %s. Removed: %s. Current tags: %s.",
                AddCommand.formatIdentity(updatedPerson), "sponsor", Messages.EMPTY_TAGS, "sponsor");

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(target, updatedPerson);

        assertCommandSuccess(tagCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndex_throwsCommandException() {
        Index outOfBound = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        TagCommand tagCommand = new TagCommand(outOfBound, Set.of(new Tag("vip")));

        assertCommandFailure(tagCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_nameNotFound_throwsCommandException() {
        Name unknown = new Name("Nonexistent Person");
        TagCommand tagCommand = new TagCommand(unknown, Set.of(new Tag("vip")));

        assertCommandFailure(tagCommand, model,
                String.format(DeleteCommand.MESSAGE_PERSON_NOT_FOUND, unknown));
    }

    @Test
    public void execute_ambiguousName_throwsCommandException() {
        Person duplicateName = new PersonBuilder()
                .withName("Alice Pauline")
                .withCompany("Other Company")
                .withEmail("alice2@other.com")
                .withPhone("88889999")
                .build();
        model.addPerson(duplicateName);

        Name ambiguous = new Name("Alice Pauline");
        TagCommand tagCommand = new TagCommand(ambiguous, Set.of(new Tag("vip")));

        assertCommandFailure(tagCommand, model, String.format(TagCommand.MESSAGE_AMBIGUOUS, ambiguous));
    }

    @Test
    public void equals() {
        Set<Tag> vip = Set.of(new Tag("vip"));
        Set<Tag> sponsor = Set.of(new Tag("sponsor"));
        TagCommand byFirstIndex = new TagCommand(INDEX_FIRST_PERSON, vip);
        TagCommand bySecondIndex = new TagCommand(INDEX_SECOND_PERSON, vip);
        TagCommand byName = new TagCommand(new Name("Alice Pauline"), vip);

        assertTrue(byFirstIndex.equals(byFirstIndex));
        assertTrue(byFirstIndex.equals(new TagCommand(INDEX_FIRST_PERSON, vip)));
        assertTrue(byName.equals(new TagCommand(new Name("Alice Pauline"), vip)));

        assertFalse(byFirstIndex.equals(null));
        assertFalse(byFirstIndex.equals(1));
        assertFalse(byFirstIndex.equals(bySecondIndex));
        assertFalse(byFirstIndex.equals(byName));
        assertFalse(byFirstIndex.equals(new TagCommand(INDEX_FIRST_PERSON, sponsor)));
    }
}
