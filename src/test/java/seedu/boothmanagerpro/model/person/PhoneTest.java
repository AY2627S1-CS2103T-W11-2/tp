package seedu.boothmanagerpro.model.person;

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
        assertTrue(Phone.isValidPhone("9312 1534")); // single spaces between digit groups
        assertFalse(Phone.isValidPhone("9312  1534")); // consecutive spaces are not allowed
        assertFalse(Phone.isValidPhone("+65-91234567")); // punctuation other than a leading plus is not allowed
        assertFalse(Phone.isValidPhone("65+91234567")); // plus must be leading

        // length guardrails
        assertFalse(Phone.isValidPhone("1".repeat(16))); // more than 15 digits
        assertFalse(Phone.isValidPhone("1".repeat(1000))); // thousands of digits
        assertTrue(Phone.isValidPhone("+123456789012345")); // boundary value exactly 15 digits with plus
        assertTrue(Phone.isValidPhone("+1 2 3 4 5 6 7 8 9 0 1 2 3 4 5")); // 15 digits with single spaces

        // valid phone numbers
        assertFalse(Phone.isValidPhone("911")); // too short
        assertTrue(Phone.isValidPhone("93121534"));
        assertTrue(Phone.isValidPhone("124293842033123")); // long phone numbers
    }

    @Test
    public void isValidPhone_formattingExtensionsLabelsAndMultipleNumbers_rejectsInput() {
        for (String phone : new String[]{"(((((((1234567", "1)2)3)4)5)6)7", "1...2---3(((4)))567",
            "(65)61234567", "9123  4567", "9123-4567", "6123.4567", "61234567 ext 88",
            "91234567 (HP)", "91234567,87654321", "91234567/87654321"}) {
            assertFalse(Phone.isValidPhone(phone), phone);
            assertFalse(Phone.hasAllowedCharacters(phone), phone);
        }
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
