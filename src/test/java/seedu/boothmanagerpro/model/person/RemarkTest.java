package seedu.boothmanagerpro.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.boothmanagerpro.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class RemarkTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Remark(null));
    }

    @Test
    public void isEmpty() {
        assertTrue(new Remark("").isEmpty());
        assertFalse(new Remark("Likes to swim.").isEmpty());
    }

    @Test
    public void equals() {
        Remark remark = new Remark("Hello");

        assertTrue(remark.equals(remark));
        assertTrue(remark.equals(new Remark("Hello")));
        assertEquals(remark.hashCode(), new Remark("Hello").hashCode());

        assertFalse(remark.equals(null));
        assertFalse(remark.equals(5.0f));
        assertNotEquals(remark, new Remark("Bye"));
    }

    @Test
    public void toString_returnsValue() {
        assertEquals("Hello", new Remark("Hello").toString());
    }
}
