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
    public void contactListRequest_isExplicitAndPartOfResultIdentity() {
        CommandResult result = CommandResult.forContactList("Listed contacts");
        CommandResult sameResult = CommandResult.forContactList("Listed contacts");
        CommandResult normalResult = new CommandResult("Listed contacts");
        assertTrue(result.isShowContactList());
        assertFalse(normalResult.isShowContactList());
        assertEquals(result, sameResult);
        assertEquals(result.hashCode(), sameResult.hashCode());
        assertNotEquals(result, normalResult);
        assertNotEquals(result.hashCode(), normalResult.hashCode());
        assertTrue(result.getPersonToView().isEmpty());
        assertTrue(result.getViewChoices().isEmpty());
        assertFalse(result.isShowHelp());
        assertFalse(result.isExit());
        assertThrows(NullPointerException.class, () -> CommandResult.forContactList(null));
    }

    @Test
    public void contactListRequest_canSelectContactWithoutMakingAddRevealList() {
        CommandResult viewResult = CommandResult.forContactList("Contact found", ALICE);
        CommandResult addResult = new CommandResult("Contact added", ALICE);
        assertTrue(viewResult.isShowContactList());
        assertEquals(ALICE, viewResult.getPersonToView().orElseThrow());
        assertEquals(viewResult, CommandResult.forContactList("Contact found", ALICE));
        assertFalse(addResult.isShowContactList());
        assertNotEquals(viewResult, new CommandResult("Contact found", ALICE));
        assertThrows(NullPointerException.class, () -> CommandResult.forContactList("Contact found", null));
    }

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
    public void equals() {
        CommandResult commandResult = new CommandResult("feedback");

        // same values -> returns true
        assertTrue(commandResult.equals(new CommandResult("feedback")));
        assertTrue(commandResult.equals(new CommandResult("feedback", false, false)));

        // same object -> returns true
        assertTrue(commandResult.equals(commandResult));

        // null -> returns false
        assertFalse(commandResult.equals(null));

        // different types -> returns false
        assertFalse(commandResult.equals(0.5f));

        // different feedbackToUser value -> returns false
        assertFalse(commandResult.equals(new CommandResult("different")));

        // different showHelp value -> returns false
        assertFalse(commandResult.equals(new CommandResult("feedback", true, false)));

        // different exit value -> returns false
        assertFalse(commandResult.equals(new CommandResult("feedback", false, true)));
    }

    @Test
    public void hashcode() {
        CommandResult commandResult = new CommandResult("feedback");

        // same values -> returns same hashcode
        assertEquals(commandResult.hashCode(), new CommandResult("feedback").hashCode());

        // different feedbackToUser value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("different").hashCode());

        // different showHelp value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("feedback", true, false).hashCode());

        // different exit value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("feedback", false, true).hashCode());
    }

    @Test
    public void toStringMethod() {
        CommandResult commandResult = new CommandResult("feedback");
        String expected = CommandResult.class.getCanonicalName() + "{feedbackToUser="
                + commandResult.getFeedbackToUser() + ", showHelp=" + commandResult.isShowHelp()
                + ", exit=" + commandResult.isExit() + ", showContactList=false, viewChoices=[], personToView=null}";
        assertEquals(expected, commandResult.toString());
    }
}
