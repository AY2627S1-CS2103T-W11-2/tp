package seedu.boothmanagerpro.model.person;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.boothmanagerpro.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class AddressTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Address(null));
    }

    @Test
    public void constructor_invalidAddress_throwsIllegalArgumentException() {
        String invalidAddress = "";
        assertThrows(IllegalArgumentException.class, () -> new Address(invalidAddress));
    }

    @Test
    public void isValidAddress() {
        // null address
        assertThrows(NullPointerException.class, () -> Address.isValidAddress(null));

        // invalid addresses
        assertFalse(Address.isValidAddress("")); // empty string
        assertFalse(Address.isValidAddress(" ")); // spaces only

        // length guardrails
        assertFalse(Address.isValidAddress("A".repeat(201))); // exceeds max length of 200
        assertFalse(Address.isValidAddress("A".repeat(1000))); // thousands of characters long
        assertTrue(Address.isValidAddress("A".repeat(200))); // boundary value exactly 200

        // valid addresses
        assertTrue(Address.isValidAddress("Blk 456, Den Road, #01-355"));
        assertTrue(Address.isValidAddress("-")); // one character
        assertTrue(Address.isValidAddress("Leng Inc; 1234 Market St; San Francisco CA 2349879; USA")); // long address

        // different languages and special characters
        assertTrue(Address.isValidAddress("北京市海淀区中关村南大街5号")); // Chinese
        assertTrue(Address.isValidAddress("東京都千代田区千代田1-1")); // Japanese
        assertTrue(Address.isValidAddress("12 Rue de l'Élysée, 75008 Paris, France")); // French with accents and apostrophe
        assertTrue(Address.isValidAddress("Al-Madinah Al-Munawwarah Rd, حي الشرفية, Jeddah")); // Arabic mixed
        assertTrue(Address.isValidAddress("Level 12 / Suite 4B & 4C, 50-60 King's Cross Road")); // Slashes, ampersand, hyphen
    }

    @Test
    public void equals() {
        Address address = new Address("Valid Address");

        // same values -> returns true
        assertTrue(address.equals(new Address("Valid Address")));

        // same object -> returns true
        assertTrue(address.equals(address));

        // null -> returns false
        assertFalse(address.equals(null));

        // different types -> returns false
        assertFalse(address.equals(5.0f));

        // different values -> returns false
        assertFalse(address.equals(new Address("Other Valid Address")));
    }
}
