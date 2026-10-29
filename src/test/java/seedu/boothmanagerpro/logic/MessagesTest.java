package seedu.boothmanagerpro.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.boothmanagerpro.testutil.TypicalPersons.ALICE;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.boothmanagerpro.logic.parser.Prefix;
import seedu.boothmanagerpro.model.person.Person;
import seedu.boothmanagerpro.testutil.PersonBuilder;

public class MessagesTest {

    @Test
    public void defaultConstructor() {
        assertNotNull(new Messages());
    }

    @Test
    public void getErrorMessageForDuplicatePrefixes_validPrefixes_success() {
        Prefix namePrefix = new Prefix("n/");
        Prefix phonePrefix = new Prefix("p/");

        // single prefix
        assertEquals(Messages.MESSAGE_DUPLICATE_FIELDS + "n/",
                Messages.getErrorMessageForDuplicatePrefixes(namePrefix));

        // multiple prefixes and deduplication check
        assertEquals(Messages.MESSAGE_DUPLICATE_FIELDS + "n/ p/",
                Messages.getErrorMessageForDuplicatePrefixes(namePrefix, phonePrefix, namePrefix));
    }

    @Test
    public void getErrorMessageForDuplicatePrefixes_noPrefixes_assertionError() {
        assertThrows(AssertionError.class, () -> Messages.getErrorMessageForDuplicatePrefixes());
    }

    @Test
    public void format_personWithAllFields_success() {
        String expected = ALICE.getName()
                + "; Company: " + Messages.formatOptional(ALICE.getCompany())
                + "; Contact method: " + Messages.formatOptional(ALICE.getContactMethod())
                + "; Phone: " + ALICE.getPhone()
                + "; Email: " + ALICE.getEmail()
                + "; Tags: " + Messages.formatTags(ALICE);
        assertEquals(expected, Messages.format(ALICE));
    }

    @Test
    public void format_personWithOptionalFieldsEmpty_success() {
        Person person = new PersonBuilder().build();
        String expected = person.getName()
                + "; Company: Not specified"
                + "; Contact method: Not specified"
                + "; Phone: " + person.getPhone()
                + "; Email: " + person.getEmail()
                + "; Tags: None";
        assertEquals(expected, Messages.format(person));
    }

    @Test
    public void formatContactDetails_success() {
        Person person = new PersonBuilder().withCompany("TechCorp").withContactMethod("email")
                .withFollowUpDate("20261025").withTags("lead").build();
        String expected = "Amy Bee at TechCorp\n"
                + "Email: amy@gmail.com\n"
                + "Phone: 85355255\n"
                + "Contact method: email\n"
                + "Enquiry status: Needs follow-up\n"
                + "Follow-up date: 25 Oct 2026\n"
                + "Tags: lead";
        assertEquals(expected, Messages.formatContactDetails(person));
    }

    @Test
    public void formatFollowUpDate_presentAndEmpty() {
        Person personWithDate = new PersonBuilder().withFollowUpDate("20261025").build();
        assertEquals("25 Oct 2026", Messages.formatFollowUpDate(personWithDate));

        Person personWithoutDate = new PersonBuilder().build();
        assertEquals("None", Messages.formatFollowUpDate(personWithoutDate));
    }

    @Test
    public void formatNumberedContacts_emptyAndNonEmptyList() {
        assertEquals("", Messages.formatNumberedContacts(List.of()));

        Person p1 = new PersonBuilder().withName("Person One").build();
        Person p2 = new PersonBuilder().withName("Person Two").build();
        String expected = "1. " + Messages.formatContactDetails(p1)
                + "\n\n2. " + Messages.formatContactDetails(p2);
        assertEquals(expected, Messages.formatNumberedContacts(List.of(p1, p2)));
    }

    @Test
    public void formatTags_emptyAndNonEmpty() {
        Person personWithoutTags = new PersonBuilder().build();
        assertEquals(Messages.EMPTY_TAGS, Messages.formatTags(personWithoutTags));

        Person personWithTags = new PersonBuilder().withTags("sponsor", "vip").build();
        assertEquals("sponsor, vip", Messages.formatTags(personWithTags));
    }

    @Test
    public void formatOptional_presentAndEmpty() {
        assertEquals("Not specified", Messages.formatOptional(Optional.empty()));
        assertEquals("hello", Messages.formatOptional(Optional.of("hello")));
    }
}
