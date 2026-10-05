package seedu.boothmanagerpro.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.boothmanagerpro.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.boothmanagerpro.testutil.PersonBuilder;

/**
 * Tests the data contract required to view contacts created by the planned add feature.
 */
public class ExhibitorFieldsTest {
    @Test
    public void constructor_normalizesCompanyAndMethod() {
        Person person = new PersonBuilder().withCompany("  TechNova Pte Ltd  ").withContactMethod(" EMAIL ").build();
        assertEquals("TechNova Pte Ltd", person.getCompany());
        assertEquals("email", person.getPreferredContactMethod());
        assertEquals(person, new PersonBuilder(person).build());
        assertEquals(person.hashCode(), new PersonBuilder(person).build().hashCode());
        assertNotEquals(person, new PersonBuilder(person).withCompany("Other").build());
        assertNotEquals(person, new PersonBuilder(person).withContactMethod("phone").build());
    }

    @Test
    public void constructor_invalidCompanyOrMethod_rejectsValue() {
        assertThrows(IllegalArgumentException.class, () -> new PersonBuilder().withCompany(" ").build());
        assertThrows(IllegalArgumentException.class, () -> new PersonBuilder().withCompany("A".repeat(101)).build());
        assertThrows(IllegalArgumentException.class, () -> new PersonBuilder().withContactMethod("fax").build());
        assertEquals("", new PersonBuilder().build().getPreferredContactMethod());
        assertEquals("other", new PersonBuilder().withContactMethod("OTHER").build().getPreferredContactMethod());
    }

    @Test
    public void isSamePerson_matchesEmailOrNameAndCompany() {
        Person first = new PersonBuilder().withName("Alicia Tan").withCompany("TechNova")
                .withEmail("alicia@example.com").build();
        Person sameNameOtherCompany = new PersonBuilder(first).withCompany("Other")
                .withEmail("different@example.com").build();
        assertFalse(first.isSamePerson(sameNameOtherCompany));
        assertTrue(first.isSamePerson(new PersonBuilder(first).withName("Other Name")
                .withCompany("Other").withEmail("ALICIA@EXAMPLE.COM").build()));
        assertTrue(first.isSamePerson(new PersonBuilder(first).withName("alicia tan ")
                .withCompany("technova").withEmail("different@example.com").build()));
    }
}
