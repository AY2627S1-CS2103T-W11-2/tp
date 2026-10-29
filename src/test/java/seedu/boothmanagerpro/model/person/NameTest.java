package seedu.boothmanagerpro.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.boothmanagerpro.testutil.Assert.assertThrows;

import java.text.Normalizer;

import org.junit.jupiter.api.Test;

public class NameTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Name(null));
    }

    @Test
    public void constructor_invalidName_throwsIllegalArgumentException() {
        String invalidName = "";
        assertThrows(IllegalArgumentException.class, () -> new Name(invalidName));
    }

    @Test
    public void constructor_repeatedSpaces_collapsesToSingleSpace() {
        assertEquals("Chloe Ng", new Name("  Chloe   Ng  ").fullName);
        assertEquals(new Name("Chloe Ng"), new Name("Chloe   Ng"));
    }

    @Test
    public void isValidName() {
        // null name
        assertThrows(NullPointerException.class, () -> Name.isValidName(null));

        // invalid name
        assertFalse(Name.isValidName("")); // empty string
        assertFalse(Name.isValidName(" ")); // spaces only
        assertFalse(Name.isValidName("^")); // only non-alphanumeric characters
        assertFalse(Name.isValidName("peter*")); // contains non-alphanumeric characters

        // length guardrails
        assertFalse(Name.isValidName("A".repeat(81))); // exceeds 80 characters
        assertFalse(Name.isValidName("A".repeat(1000))); // thousands of characters
        assertTrue(Name.isValidName("A".repeat(80))); // boundary value exactly 80 characters

        // valid name
        assertTrue(Name.isValidName("peter jack")); // alphabets only
        assertFalse(Name.isValidName("12345")); // numbers only
        assertFalse(Name.isValidName("peter the 2nd")); // alphanumeric characters
        assertTrue(Name.isValidName("Capital Tan")); // with capital letters
        assertTrue(Name.isValidName("Anne-Marie O'Neil")); // realistic punctuation
        assertTrue(Name.isValidName("Dr. Élodie")); // full stops and accented letters
        assertTrue(Name.isValidName("David Roger Jackson Ray Jr")); // long names

        // different languages and legal patronymics
        assertTrue(Name.isValidName("陈大文")); // Chinese
        assertTrue(Name.isValidName("山田 太郎")); // Japanese
        assertTrue(Name.isValidName("Владимир")); // Cyrillic
        assertTrue(Name.isValidName("Renée O’Connor")); // French diacritic and curly apostrophe
        assertTrue(Name.isValidName("Raj s/o Kumar")); // patronymic s/o
        assertTrue(Name.isValidName("Raj S/O Kumar")); // uppercase S/O
        assertTrue(Name.isValidName("Priya d/o Raman")); // patronymic d/o
        assertTrue(Name.isValidName("Dr. Raj s/o M. Kumar")); // titles and initials with s/o

        // invalid slash and patronymic forms
        assertFalse(Name.isValidName("s/o")); // bare patronymic
        assertFalse(Name.isValidName("s/o Kumar")); // missing preceding name
        assertFalse(Name.isValidName("Raj s/o")); // missing following name
        assertFalse(Name.isValidName("Raj s/o s/o Kumar")); // consecutive patronymics
        assertFalse(Name.isValidName("--- s/o Kumar")); // no letters before s/o
        assertFalse(Name.isValidName("Raj s/o ---")); // no letters after s/o
        assertFalse(Name.isValidName("Raj/Kumar")); // arbitrary slash
        assertFalse(Name.isValidName("Raj x/o Kumar")); // unsupported slash marker
    }

    @Test
    public void equals() {
        Name name = new Name("Valid Name");

        // same values -> returns true
        assertTrue(name.equals(new Name("Valid Name")));

        // same object -> returns true
        assertTrue(name.equals(name));

        // null -> returns false
        assertFalse(name.equals(null));

        // different types -> returns false
        assertFalse(name.equals(5.0f));

        // different values -> returns false
        assertFalse(name.equals(new Name("Other Valid Name")));
    }

    // Unicode NFC normalisation

    @Test
    public void constructor_nfdInput_storedAsNfc() {
        // NFD: e + combining acute (U+0301)
        String nfd = "Jose\u0301 Tan";
        // NFC: é as precomposed U+00E9
        String nfc = "Jos\u00E9 Tan";
        Name fromNfd = new Name(nfd);
        Name fromNfc = new Name(nfc);
        // Both must store the same NFC bytes and compare as equal
        assertEquals(nfc, fromNfd.fullName);
        assertEquals(fromNfc, fromNfd);
    }

    @Test
    public void constructor_nfcAndNfdRepresentations_haveEqualHashCode() {
        Name nfcName = new Name("Jos\u00E9 Tan");
        Name nfdName = new Name("Jose\u0301 Tan");
        assertEquals(nfcName.hashCode(), nfdName.hashCode());
    }

    @Test
    public void normaliseWhitespace_returnsNfcForm() {
        String nfd = "Cafe\u0301 Owner";
        String result = Name.normaliseWhitespace(nfd);
        // After normalisation the result must already be in NFC form
        assertEquals(Normalizer.normalize(result, Normalizer.Form.NFC), result);
        assertEquals("Caf\u00E9 Owner", result);
    }
}
