package seedu.boothmanagerpro.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.boothmanagerpro.testutil.Assert.assertThrows;
import static seedu.boothmanagerpro.testutil.TypicalPersons.ALICE;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.boothmanagerpro.model.person.Person;

public class CommandResultTest {
    @Test
    public void viewChoices_areImmutableSnapshots() {
        List<Person> choices = new ArrayList<>(List.of(ALICE));
        CommandResult result = new CommandResult("Choose a contact", choices);
        choices.clear();
        assertEquals(List.of(ALICE), result.getViewChoices());
        assertThrows(UnsupportedOperationException.class, () -> result.getViewChoices().clear());
        assertEquals(result, new CommandResult("Choose a contact", List.of(ALICE)));
        assertEquals(result.hashCode(), new CommandResult("Choose a contact", List.of(ALICE)).hashCode());
        assertNotEquals(result, new CommandResult("Choose a contact"));
    }

    @Test
    public void contactsToShow_areImmutableSnapshotsComparedWithNumbering() {
        List<Person> contacts = new ArrayList<>(List.of(ALICE));
        CommandResult numbered = CommandResult.showingContacts("Contacts", contacts);
        contacts.clear();
        assertEquals(List.of(ALICE), numbered.getContactsToShow());
        assertTrue(numbered.isNumberedContacts());
        assertThrows(UnsupportedOperationException.class, () -> numbered.getContactsToShow().clear());

        CommandResult unnumbered = CommandResult.showingContact("Contacts", ALICE);
        assertEquals(List.of(ALICE), unnumbered.getContactsToShow());
        assertFalse(unnumbered.isNumberedContacts());

        // same contacts and numbering -> returns true
        assertEquals(numbered, CommandResult.showingContacts("Contacts", List.of(ALICE)));
        assertEquals(numbered.hashCode(), CommandResult.showingContacts("Contacts", List.of(ALICE)).hashCode());

        // same contacts, different numbering -> returns false
        assertNotEquals(numbered, unnumbered);

        // different contacts -> returns false
        assertNotEquals(numbered, CommandResult.showingContacts("Contacts", List.of()));
        assertNotEquals(numbered, new CommandResult("Contacts"));
    }

    @Test
    public void equals() {
        CommandResult commandResult = new CommandResult("feedback");

        // same values -> returns true
        assertTrue(commandResult.equals(new CommandResult("feedback")));
        assertTrue(commandResult.equals(new CommandResult("feedback", false)));

        // same object -> returns true
        assertTrue(commandResult.equals(commandResult));

        // null -> returns false
        assertFalse(commandResult.equals(null));

        // different types -> returns false
        assertFalse(commandResult.equals(0.5f));

        // different feedbackToUser value -> returns false
        assertFalse(commandResult.equals(new CommandResult("different")));

        // different exit value -> returns false
        assertFalse(commandResult.equals(new CommandResult("feedback", true)));
    }

    @Test
    public void hashcode() {
        CommandResult commandResult = new CommandResult("feedback");

        // same values -> returns same hashcode
        assertEquals(commandResult.hashCode(), new CommandResult("feedback").hashCode());

        // different feedbackToUser value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("different").hashCode());

        // different exit value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("feedback", true).hashCode());
    }

    @Test
    public void toStringMethod() {
        CommandResult commandResult = new CommandResult("feedback");
        String expected = CommandResult.class.getCanonicalName() + "{feedbackToUser="
                + commandResult.getFeedbackToUser()
                + ", exit=" + commandResult.isExit() + ", viewChoices=[], contactsToShow=[], numberedContacts=false}";
        assertEquals(expected, commandResult.toString());
    }
}
