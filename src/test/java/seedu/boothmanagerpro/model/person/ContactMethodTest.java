package seedu.boothmanagerpro.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.boothmanagerpro.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class ContactMethodTest {

    @Test
    public void fromString_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ContactMethod.fromString(null));
    }

    @Test
    public void fromString_invalidValue_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                ContactMethod.MESSAGE_CONSTRAINTS, () -> ContactMethod.fromString(""));
        assertThrows(IllegalArgumentException.class,
                ContactMethod.MESSAGE_CONSTRAINTS, () -> ContactMethod.fromString("   "));
        assertThrows(IllegalArgumentException.class,
                ContactMethod.MESSAGE_CONSTRAINTS, () -> ContactMethod.fromString("sms"));
        assertThrows(IllegalArgumentException.class,
                ContactMethod.MESSAGE_CONSTRAINTS, () -> ContactMethod.fromString("telegram"));
    }

    @Test
    public void fromString_validValuesCaseInsensitiveAndTrimmed_success() {
        assertEquals(ContactMethod.EMAIL, ContactMethod.fromString("email"));
        assertEquals(ContactMethod.EMAIL, ContactMethod.fromString("  EMAIL  "));
        assertEquals(ContactMethod.PHONE, ContactMethod.fromString("Phone"));
        assertEquals(ContactMethod.OTHER, ContactMethod.fromString("  oThEr "));
    }

    @Test
    public void toStringMethod() {
        assertEquals("email", ContactMethod.EMAIL.toString());
        assertEquals("phone", ContactMethod.PHONE.toString());
        assertEquals("other", ContactMethod.OTHER.toString());
    }
}

