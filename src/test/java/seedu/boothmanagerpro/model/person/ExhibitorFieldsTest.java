package seedu.boothmanagerpro.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.boothmanagerpro.model.tag.Tag;
import seedu.boothmanagerpro.testutil.PersonBuilder;

public class ExhibitorFieldsTest {
    @Test
    public void company_trimsValidatesAndComparesValues() {
        Company company = new Company("  TechNova Pte Ltd  ");
        Company sameCompany = new Company("TechNova Pte Ltd");
        assertEquals("TechNova Pte Ltd", company.toString());
        assertEquals(sameCompany, company);
        assertEquals(sameCompany.hashCode(), company.hashCode());
        assertTrue(company.equals(company));
        assertNotEquals(new Company("Other"), company);
        assertFalse(company.equals("TechNova Pte Ltd"));
        assertFalse(company.equals(null));
        assertThrows(NullPointerException.class, () -> new Company(null));
        assertThrows(IllegalArgumentException.class, () -> new Company(" "));
        assertThrows(IllegalArgumentException.class, () -> new Company("a".repeat(101)));
        assertEquals(100, new Company("a".repeat(100)).value.length());
    }

    @Test
    public void contactMethod_normalisesAndRejectsUnsupportedValues() {
        assertEquals(ContactMethod.EMAIL, ContactMethod.fromString(" EMAIL "));
        assertEquals(ContactMethod.PHONE, ContactMethod.fromString("Phone"));
        assertEquals(ContactMethod.OTHER, ContactMethod.fromString("other"));
        assertEquals("phone", ContactMethod.PHONE.toString());
        assertThrows(IllegalArgumentException.class, () -> ContactMethod.fromString("fax"));
        assertThrows(IllegalArgumentException.class, () -> ContactMethod.fromString(""));
    }

    @Test
    public void namesPhonesEmailsAndTags_normaliseAtModelBoundary() {
        assertEquals("Anne-Marie O'Neil.", new Name("  Anne-Marie O'Neil. ").fullName);
        assertTrue(Name.isValidName("李明"));
        assertFalse(Name.isValidName("---"));
        assertFalse(Name.isValidName("John2"));
        assertEquals("alicia@example.com", new Email("ALICIA@EXAMPLE.COM").value);
        assertEquals(new Email("alicia@example.com"), new Email("ALICIA@EXAMPLE.COM"));
        assertEquals("+6591234567", new Phone(" +65 9123-4567 ").value);
        assertFalse(Phone.isValidPhone("++1234567"));
        assertFalse(Phone.isValidPhone("1234\t567"));
        assertEquals("high-priority", new Tag(" high-priority ").tagName);
        assertTrue(Tag.isValidTagName("priority client"));
        assertFalse(Tag.isValidTagName("priority/client"));
    }

    @Test
    public void person_newFieldsParticipateInEqualityButMethodDoesNotChangeIdentity() {
        Person person = new PersonBuilder().withCompany("TechNova").withContactMethod("email").build();
        Person copy = new PersonBuilder(person).build();
        assertEquals(person, copy);
        assertEquals(person.hashCode(), copy.hashCode());
        Person differentMethod = new PersonBuilder(person).withContactMethod("phone").build();
        assertNotEquals(person, differentMethod);
        assertTrue(person.isSamePerson(differentMethod));
        Person differentCompany = new PersonBuilder(person).withCompany("Other").build();
        assertNotEquals(person, differentCompany);
        assertTrue(person.isSamePerson(differentCompany)); // Email still matches.
        Person sameNameAndCompany = new PersonBuilder(person).withEmail("new@example.com").build();
        assertTrue(person.isSamePerson(sameNameAndCompany));
        Person differentContact = new PersonBuilder(differentCompany).withEmail("new@example.com").build();
        assertFalse(person.isSamePerson(differentContact));
    }

    @Test
    public void person_duplicateMatching_handlesLegacyRecordsAndDifferentRepresentatives() {
        Person legacy = new PersonBuilder().build();
        Person exhibitor = new PersonBuilder(legacy).withCompany("TechNova").build();
        assertTrue(legacy.isSamePerson(exhibitor));
        assertTrue(exhibitor.isSamePerson(legacy));

        Person otherEmail = new PersonBuilder(exhibitor).withEmail("other@example.com").build();
        assertFalse(legacy.isSamePerson(otherEmail));
        assertFalse(otherEmail.isSamePerson(legacy));

        Person colleague = new PersonBuilder(otherEmail).withName("Other Representative").build();
        assertFalse(exhibitor.isSamePerson(colleague));
        assertFalse(colleague.isSamePerson(exhibitor));
    }
}
