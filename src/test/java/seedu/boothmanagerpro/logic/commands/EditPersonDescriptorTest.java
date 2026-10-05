package seedu.boothmanagerpro.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.boothmanagerpro.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.boothmanagerpro.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.boothmanagerpro.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.boothmanagerpro.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.boothmanagerpro.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.boothmanagerpro.logic.commands.CommandTestUtil.createAmyDescriptor;
import static seedu.boothmanagerpro.logic.commands.CommandTestUtil.createBobDescriptor;

import org.junit.jupiter.api.Test;

import seedu.boothmanagerpro.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.boothmanagerpro.testutil.EditPersonDescriptorBuilder;

public class EditPersonDescriptorTest {

    @Test
    public void equals() {
        EditPersonDescriptor amyDescriptor = createAmyDescriptor();

        // same values -> returns true
        EditPersonDescriptor descriptorWithSameValues = new EditPersonDescriptor(amyDescriptor);
        assertTrue(amyDescriptor.equals(descriptorWithSameValues));

        // same object -> returns true
        assertTrue(amyDescriptor.equals(amyDescriptor));

        // null -> returns false
        assertFalse(amyDescriptor.equals(null));

        // different types -> returns false
        assertFalse(amyDescriptor.equals(5));

        // different values -> returns false
        assertFalse(amyDescriptor.equals(createBobDescriptor()));

        // different name -> returns false
        EditPersonDescriptor editedAmy = new EditPersonDescriptorBuilder(amyDescriptor)
                .withName(VALID_NAME_BOB).build();
        assertFalse(amyDescriptor.equals(editedAmy));

        // different phone -> returns false
        editedAmy = new EditPersonDescriptorBuilder(amyDescriptor).withPhone(VALID_PHONE_BOB).build();
        assertFalse(amyDescriptor.equals(editedAmy));

        // different email -> returns false
        editedAmy = new EditPersonDescriptorBuilder(amyDescriptor).withEmail(VALID_EMAIL_BOB).build();
        assertFalse(amyDescriptor.equals(editedAmy));

        // different address -> returns false
        editedAmy = new EditPersonDescriptorBuilder(amyDescriptor).withAddress(VALID_ADDRESS_BOB).build();
        assertFalse(amyDescriptor.equals(editedAmy));

        // different tags -> returns false
        editedAmy = new EditPersonDescriptorBuilder(amyDescriptor).withTags(VALID_TAG_HUSBAND).build();
        assertFalse(amyDescriptor.equals(editedAmy));
    }

    @Test
    public void toStringMethod() {
        EditPersonDescriptor editPersonDescriptor = new EditPersonDescriptor();
        String expected = EditPersonDescriptor.class.getCanonicalName() + "{name="
                + editPersonDescriptor.getName().orElse(null) + ", phone="
                + editPersonDescriptor.getPhone().orElse(null) + ", email="
                + editPersonDescriptor.getEmail().orElse(null) + ", address="
                + editPersonDescriptor.getAddress().orElse(null) + ", tags="
                + editPersonDescriptor.getTags().orElse(null) + "}";
        assertEquals(expected, editPersonDescriptor.toString());
    }
}
