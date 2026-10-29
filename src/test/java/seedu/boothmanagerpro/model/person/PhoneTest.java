package seedu.boothmanagerpro.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.boothmanagerpro.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class PhoneTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Phone(null));
    }

    @Test
    public void constructor_invalidPhone_throwsIllegalArgumentException() {
        String invalidPhone = "";
        assertThrows(IllegalArgumentException.class, () -> new Phone(invalidPhone));
    }

    @Test
    public void isValidPhone() {
        // null phone number
        assertThrows(NullPointerException.class, () -> Phone.isValidPhone(null));

        // invalid phone numbers
        assertFalse(Phone.isValidPhone("")); // empty string
        assertFalse(Phone.isValidPhone(" ")); // spaces only
        assertFalse(Phone.isValidPhone("91")); // fewer than 7 digits
        assertFalse(Phone.isValidPhone("phone")); // non-numeric
        assertFalse(Phone.isValidPhone("9011p041")); // alphabets within digits
        assertTrue(Phone.isValidPhone("9312 1534")); // spaces within digits

        // length guardrails
        assertFalse(Phone.isValidPhone("1".repeat(16))); // more than 15 digits
        assertFalse(Phone.isValidPhone("1".repeat(1000))); // thousands of digits
        // Excessively padded input
        assertFalse(Phone.isValidPhone("+65" + " ".repeat(Phone.MAX_INPUT_LENGTH) + "91234567"));
        assertTrue(Phone.isValidPhone("+123456789012345")); // boundary value exactly 15 digits with plus

        // valid phone numbers
        assertFalse(Phone.isValidPhone("911")); // too short
        assertTrue(Phone.isValidPhone("93121534"));
        assertTrue(Phone.isValidPhone("124293842033123")); // long phone numbers
    }

    @Test
    public void constructor_formattedNumbersAndExtensions_preservesDisplay() {
        for (String phone : new String[]{"(65) 6123-4567", "+65 (6123) 4567 ext 88", "6123.4567 x88",
            "61234567 EXT. 123456", "9123 4567, 8765 4321", "91234567; +65 8765-4321",
            "91234567 / 87654321 ext 88", "+123456789012345 ext 123456"}) {
            assertTrue(Phone.isValidPhone(phone), phone);
            assertEquals(phone, new Phone("  " + phone + "  ").value);
        }
        assertEquals("+65 (6123) 4567", new Phone("+65  (6123)  4567").value);
        assertEquals("+6561234567", Phone.normalize("+65 (6123).4567"));
    }

    @Test
    public void isValidPhone_malformedExtensionsOrMultipleNumbers_rejectsInput() {
        for (String phone : new String[]{"612345 ext 88", "6123456789012345 x1", "61234567 ext",
            "61234567 ext 1234567", "61234567 ext abc", "61234567,", ",61234567", "61234567;;81234567",
            "61234567 / 123", "61234567 81234567", "6123\t4567", "61234567 (HP)"}) {
            assertFalse(Phone.isValidPhone(phone), phone);
        }
        String atLimit = "61234567" + " ".repeat(Phone.MAX_INPUT_LENGTH - 15) + "8123456";
        assertTrue(Phone.isValidPhone(atLimit));
        assertFalse(Phone.isValidPhone(atLimit.replace(" ", "  ")));
    }

    @Test
    public void equals() {
        Phone phone = new Phone("9999999");

        // same values -> returns true
        assertTrue(phone.equals(new Phone("9999999")));

        // same object -> returns true
        assertTrue(phone.equals(phone));

        // null -> returns false
        assertFalse(phone.equals(null));

        // different types -> returns false
        assertFalse(phone.equals(5.0f));

        // different values -> returns false
        assertFalse(phone.equals(new Phone("9959999")));
    }
}
