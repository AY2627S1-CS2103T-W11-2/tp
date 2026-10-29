package seedu.boothmanagerpro.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.boothmanagerpro.testutil.Assert.assertThrows;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.boothmanagerpro.model.tag.Tag;
import seedu.boothmanagerpro.testutil.PersonBuilder;

public class ContactUpdateTest {

    @Test
    public void constructor_nullComponent_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ContactUpdate(
                null, Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));
        assertThrows(NullPointerException.class, () -> new ContactUpdate(
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), null));
    }

    @Test
    public void constructor_defensivelyCopiesTags() {
        Set<Tag> mutableTags = new HashSet<>();
        mutableTags.add(new Tag("vip"));
        ContactUpdate update = new ContactUpdate(
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(mutableTags));

        mutableTags.add(new Tag("extra"));
        assertEquals(Set.of(new Tag("vip")), update.tags().orElseThrow());
        assertThrows(UnsupportedOperationException.class, () -> update.tags().orElseThrow().add(new Tag("new")));
    }

    @Test
    public void applyTo_allFieldsEmpty_preservesOriginalPerson() {
        Person original = new PersonBuilder()
                .withName("Alicia Tan")
                .withCompany("TechNova")
                .withEmail("alicia@technova.com")
                .withPhone("91234567")
                .withAddress("Blk 10")
                .withContactMethod("email")
                .withEnquiryStatus("In progress")
                .withFollowUpDate("20261110")
                .withTags("vip")
                .build();

        ContactUpdate emptyUpdate = new ContactUpdate(
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

        assertEquals(original, emptyUpdate.applyTo(original));
    }

    @Test
    public void applyTo_suppliedFields_replacesSuppliedAndPreservesAddress() {
        Person original = new PersonBuilder()
                .withName("Alicia Tan")
                .withCompany("TechNova")
                .withEmail("alicia@technova.com")
                .withPhone("91234567")
                .withAddress("Blk 10")
                .withContactMethod("email")
                .withEnquiryStatus("Needs follow-up")
                .withFollowUpDate("20261110")
                .withTags("vip")
                .build();

        ContactUpdate update = new ContactUpdate(
                Optional.of(new Name("Alicia Lim")),
                Optional.of(new Company("NewCorp")),
                Optional.of(new Email("alicia@newcorp.com")),
                Optional.of(new Phone("81112222")),
                Optional.of(Optional.of(ContactMethod.PHONE)),
                Optional.of(EnquiryStatus.COMPLETED),
                Optional.of(Optional.empty()),
                Optional.of(Set.of()));

        Person result = update.applyTo(original);
        assertEquals(new Name("Alicia Lim"), result.getName());
        assertEquals(Optional.of(new Company("NewCorp")), result.getCompany());
        assertEquals(new Email("alicia@newcorp.com"), result.getEmail());
        assertEquals(new Phone("81112222"), result.getPhone());
        assertEquals(new Address("Blk 10"), result.getAddress());
        assertEquals(Optional.of(ContactMethod.PHONE), result.getContactMethod());
        assertEquals(EnquiryStatus.COMPLETED, result.getEnquiryStatus());
        assertTrue(result.getFollowUpDate().isEmpty());
        assertTrue(result.getTags().isEmpty());

        ContactUpdate clearMethod = new ContactUpdate(
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.of(Optional.empty()), Optional.empty(), Optional.empty(), Optional.empty());
        assertTrue(clearMethod.applyTo(result).getContactMethod().isEmpty());
    }
}
