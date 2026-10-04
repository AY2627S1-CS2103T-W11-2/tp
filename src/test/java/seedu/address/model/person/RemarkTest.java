package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class RemarkTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Remark(null));
    }

    @Test
    public void constructor_anyNonNullValue_accepted() {
        assertEquals("", new Remark("").value);
        assertEquals(" ", new Remark(" ").value);
        assertEquals("Likes to swim.", new Remark("Likes to swim.").value);
    }

    @Test
    public void equals() {
        Remark remark = new Remark("Some remark");

        // same values -> returns true
        assertTrue(remark.equals(new Remark("Some remark")));

        // same object -> returns true
        assertTrue(remark.equals(remark));

        // null -> returns false
        assertFalse(remark.equals(null));

        // different types -> returns false
        assertFalse(remark.equals("Some remark"));

        // different values -> returns false
        assertFalse(remark.equals(new Remark("Other remark")));
    }
}
