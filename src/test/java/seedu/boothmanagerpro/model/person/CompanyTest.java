package seedu.boothmanagerpro.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.boothmanagerpro.testutil.Assert.assertThrows;

import java.text.Normalizer;

import org.junit.jupiter.api.Test;

public class CompanyTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Company(null));
    }

    @Test
    public void constructor_invalidCompany_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Company(""));
        assertThrows(IllegalArgumentException.class, () -> new Company("   "));
        assertThrows(IllegalArgumentException.class, () -> new Company("\t\n  "));
        assertThrows(IllegalArgumentException.class, () -> new Company("A".repeat(101)));
    }

    @Test
    public void constructor_trimsAndNormalizesNfc() {
        String decomposed = Normalizer.normalize("  Café Co.  ", Normalizer.Form.NFD);
        Company company = new Company(decomposed);
        assertEquals("Café Co.", company.value);
        assertEquals(new Company("Café Co."), company);
    }

    @Test
    public void constructor_repeatedWhitespaceAndNewlines_collapsesToSingleSpace() {
        Company doubleSpace = new Company("TechNova  Pte Ltd");
        assertEquals("TechNova Pte Ltd", doubleSpace.value);
        assertEquals(new Company("TechNova Pte Ltd"), doubleSpace);

        Company tabsAndNewlines = new Company("TechNova\t\tPte\n\nLtd");
        assertEquals("TechNova Pte Ltd", tabsAndNewlines.value);
        assertEquals(new Company("TechNova Pte Ltd"), tabsAndNewlines);

        Company mixedWhitespace = new Company("  TechNova\n  Pte Ltd  ");
        assertEquals("TechNova Pte Ltd", mixedWhitespace.value);
        assertEquals(new Company("TechNova Pte Ltd"), mixedWhitespace);

        Company unicodeWhitespace = new Company("\u2003TechNova\u2003\u2003Pte\u2003Ltd\u2003");
        assertEquals("TechNova Pte Ltd", unicodeWhitespace.value);
        assertEquals(new Company("TechNova Pte Ltd"), unicodeWhitespace);
    }

    @Test
    public void isValidCompany() {
        assertThrows(NullPointerException.class, () -> Company.isValidCompany(null));

        // invalid companies
        assertFalse(Company.isValidCompany(""));
        assertFalse(Company.isValidCompany("   "));
        assertFalse(Company.isValidCompany("\t\r\n"));
        assertFalse(Company.isValidCompany("A".repeat(101)));
        assertFalse(Company.isValidCompany("A".repeat(50) + "   " + "B".repeat(51))); // 102 code points after collapse

        // valid companies (boundaries and punctuation)
        assertTrue(Company.isValidCompany("A"));
        assertTrue(Company.isValidCompany("A".repeat(100)));
        assertTrue(Company.isValidCompany("Acme Pte. Ltd."));
        assertTrue(Company.isValidCompany("  TechNova & Partners (SG)  "));
        assertTrue(Company.isValidCompany("TechNova  Pte Ltd"));
        assertTrue(Company.isValidCompany("TechNova\n  Pte Ltd"));
        assertTrue(Company.isValidCompany("TechNova\tPte Ltd"));
        assertTrue(Company.isValidCompany("A".repeat(50) + "    " + "B".repeat(49))); // 100 code points after collapse
    }

    @Test
    public void equalsAndHashCode() {
        Company company = new Company("TechNova Pte Ltd");

        // same values -> returns true
        assertTrue(company.equals(new Company("TechNova Pte Ltd")));
        assertEquals(company.hashCode(), new Company("TechNova Pte Ltd").hashCode());

        // same object -> returns true
        assertTrue(company.equals(company));

        // null -> returns false
        assertFalse(company.equals(null));

        // different types -> returns false
        assertFalse(company.equals(5.0f));

        // different values or casing -> returns false
        assertFalse(company.equals(new Company("technova pte ltd")));
        assertFalse(company.equals(new Company("Other Corp")));
    }

    @Test
    public void toStringMethod() {
        Company company = new Company("Quantum Dynamics");
        assertEquals("Quantum Dynamics", company.toString());
    }
}

