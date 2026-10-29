package seedu.boothmanagerpro.model.util;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.boothmanagerpro.model.AddressBook;
import seedu.boothmanagerpro.model.ReadOnlyAddressBook;
import seedu.boothmanagerpro.model.person.Address;
import seedu.boothmanagerpro.model.person.Company;
import seedu.boothmanagerpro.model.person.ContactMethod;
import seedu.boothmanagerpro.model.person.Email;
import seedu.boothmanagerpro.model.person.EnquiryStatus;
import seedu.boothmanagerpro.model.person.FollowUpDate;
import seedu.boothmanagerpro.model.person.Name;
import seedu.boothmanagerpro.model.person.Person;
import seedu.boothmanagerpro.model.person.Phone;
import seedu.boothmanagerpro.model.tag.Tag;

/**
 * Contains utility methods for populating {@code AddressBook} with sample data.
 */
public class SampleDataUtil {
    public static Person[] getSamplePersons() {
        return new Person[] {
            new Person(new Name("Alex Yeoh"), new Phone("87438807"), new Email("alexyeoh@example.com"),
                new Address("Blk 30 Geylang Street 29, #06-40"),
                getTagSet("lead"),
                Optional.of(new Company("TechNova Solutions")),
                Optional.of(ContactMethod.EMAIL),
                EnquiryStatus.NEEDS_FOLLOW_UP,
                Optional.of(new FollowUpDate("20261115"))),
            new Person(new Name("Bernice Yu"), new Phone("99272758"), new Email("berniceyu@example.com"),
                new Address("Blk 30 Lorong 3 Serangoon Gardens, #07-18"),
                getTagSet("sponsor", "vip"),
                Optional.of(new Company("Solaris Technologies")),
                Optional.of(ContactMethod.PHONE),
                EnquiryStatus.IN_PROGRESS,
                Optional.of(new FollowUpDate("20261120"))),
            new Person(new Name("Charlotte Oliveiro"), new Phone("93210283"), new Email("charlotte@example.com"),
                new Address("Blk 11 Ang Mo Kio Street 74, #11-04"),
                getTagSet("partner"),
                Optional.of(new Company("BioMed Global")),
                Optional.empty(),
                EnquiryStatus.COMPLETED,
                Optional.empty()),
            new Person(new Name("David Li"), new Phone("91031282"), new Email("lidavid@example.com"),
                new Address("Blk 436 Serangoon Gardens Street 26, #16-43"),
                getTagSet("vip"),
                Optional.of(new Company("Quantum Dynamics")),
                Optional.of(ContactMethod.OTHER),
                EnquiryStatus.NEEDS_FOLLOW_UP,
                Optional.of(new FollowUpDate("20261201"))),
            new Person(new Name("Irfan Ibrahim"), new Phone("92492021"), new Email("irfan@example.com"),
                new Address("Blk 47 Tampines Street 20, #17-35"),
                getTagSet("lead"),
                Optional.of(new Company("OmniCorp Labs")),
                Optional.of(ContactMethod.EMAIL),
                EnquiryStatus.IN_PROGRESS,
                Optional.of(new FollowUpDate("20261118"))),
            new Person(new Name("Roy Balakrishnan"), new Phone("92624417"), new Email("royb@example.com"),
                new Address("Blk 45 Aljunied Street 85, #11-31"),
                getTagSet("exhibitor"),
                Optional.of(new Company("Apex Innovations")),
                Optional.of(ContactMethod.PHONE),
                EnquiryStatus.CANCELLED,
                Optional.empty())
        };
    }

    public static ReadOnlyAddressBook getSampleAddressBook() {
        AddressBook sampleAb = new AddressBook();
        for (Person samplePerson : getSamplePersons()) {
            sampleAb.addPerson(samplePerson);
        }
        return sampleAb;
    }

    /**
     * Returns a tag set containing the list of strings given.
     */
    public static Set<Tag> getTagSet(String... strings) {
        return Arrays.stream(strings)
                .map(Tag::new)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

}
