package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.Remark;

public class RemarkCommandTest {
    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_validIndex_updatesRemarkAndPreservesOtherFields() throws Exception {
        Person original = model.getFilteredPersonList().get(0);
        Remark originalRemark = original.getRemark();

        CommandResult result = new RemarkCommand(INDEX_FIRST_PERSON, "Likes to swim.").execute(model);

        Person updated = model.getAddressBook().getPersonList().get(0);
        assertNotSame(original, updated);
        assertEquals(new Remark("Likes to swim."), updated.getRemark());
        assertEquals(originalRemark, original.getRemark());
        assertEquals(original.getName(), updated.getName());
        assertEquals(original.getPhone(), updated.getPhone());
        assertEquals(original.getEmail(), updated.getEmail());
        assertEquals(original.getAddress(), updated.getAddress());
        assertEquals(original.getTags(), updated.getTags());
        assertEquals(String.format(RemarkCommand.MESSAGE_ADD_REMARK_SUCCESS, Messages.format(updated)),
                result.getFeedbackToUser());
    }

    @Test
    public void execute_emptyRemark_clearsRemark() throws Exception {
        CommandResult result = new RemarkCommand(INDEX_FIRST_PERSON, "").execute(model);

        Person updated = model.getAddressBook().getPersonList().get(0);
        assertEquals(new Remark(""), updated.getRemark());
        assertEquals(String.format(RemarkCommand.MESSAGE_DELETE_REMARK_SUCCESS, Messages.format(updated)),
                result.getFeedbackToUser());
    }

    @Test
    public void execute_filteredList_updatesDisplayedPerson() throws Exception {
        Remark firstPersonRemark = model.getAddressBook().getPersonList().get(0).getRemark();
        showPersonAtIndex(model, INDEX_SECOND_PERSON);

        new RemarkCommand(INDEX_FIRST_PERSON, "Updated remark").execute(model);

        assertEquals(new Remark("Updated remark"), model.getAddressBook().getPersonList().get(1).getRemark());
        assertEquals(firstPersonRemark, model.getAddressBook().getPersonList().get(0).getRemark());
        assertEquals(model.getAddressBook().getPersonList().size(), model.getFilteredPersonList().size());
    }

    @Test
    public void execute_invalidIndex_throwsCommandException() {
        Index invalidIndex = Index.fromZeroBased(model.getFilteredPersonList().size());
        assertCommandFailure(new RemarkCommand(invalidIndex, "Remark"), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);

        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandFailure(new RemarkCommand(INDEX_SECOND_PERSON, "Remark"), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }
}
