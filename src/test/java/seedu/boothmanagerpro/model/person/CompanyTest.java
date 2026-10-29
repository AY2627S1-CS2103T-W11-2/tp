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
    public void isValidCompany() {
        assertThrows(NullPointerException.class, () -> Company.isValidCompany(null));

        // invalid companies
        assertFalse(Company.isValidCompany(""));
        assertFalse(Company.isValidCompany("   "));
        assertFalse(Company.isValidCompany("A".repeat(101)));

        // valid companies (boundaries and punctuation)
        assertTrue(Company.isValidCompany("A"));
        assertTrue(Company.isValidCompany("A".repeat(100)));
        assertTrue(Company.isValidCompany("Acme Pte. Ltd."));
        assertTrue(Company.isValidCompany("  TechNova & Partners (SG)  "));
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

