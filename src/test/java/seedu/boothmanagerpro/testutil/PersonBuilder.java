package seedu.boothmanagerpro.testutil;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import seedu.boothmanagerpro.model.person.Address;
import seedu.boothmanagerpro.model.person.Company;
import seedu.boothmanagerpro.model.person.ContactMethod;
import seedu.boothmanagerpro.model.person.Email;
import seedu.boothmanagerpro.model.person.Name;
import seedu.boothmanagerpro.model.person.Person;
import seedu.boothmanagerpro.model.person.Phone;
import seedu.boothmanagerpro.model.tag.Tag;
import seedu.boothmanagerpro.model.util.SampleDataUtil;

/**
 * A utility class to help with building Person objects.
 */
public class PersonBuilder {

    public static final String DEFAULT_NAME = "Amy Bee";
    public static final String DEFAULT_PHONE = "85355255";
    public static final String DEFAULT_EMAIL = "amy@gmail.com";
    public static final String DEFAULT_ADDRESS = "123, Jurong West Ave 6, #08-111";

    private String company = "";
    private String preferredContactMethod = "";
    private Name name;
    private Phone phone;
    private Email email;
    private Address address;
    private Set<Tag> tags;
    private Optional<Company> company = Optional.empty();
    private Optional<ContactMethod> contactMethod = Optional.empty();

    /**
     * Creates a {@code PersonBuilder} with the default details.
     */
    public PersonBuilder() {
        name = new Name(DEFAULT_NAME);
        phone = new Phone(DEFAULT_PHONE);
        email = new Email(DEFAULT_EMAIL);
        address = new Address(DEFAULT_ADDRESS);
        tags = new HashSet<>();
    }

    /**
     * Initializes the PersonBuilder with the data of {@code personToCopy}.
     */
    public PersonBuilder(Person personToCopy) {
        company = personToCopy.getCompany();
        preferredContactMethod = personToCopy.getPreferredContactMethod();
        name = personToCopy.getName();
        phone = personToCopy.getPhone();
        email = personToCopy.getEmail();
        address = personToCopy.getAddress();
        tags = new HashSet<>(personToCopy.getTags());
        company = personToCopy.getCompany();
        contactMethod = personToCopy.getContactMethod();
    }

    /**
     * Sets the {@code Name} of the {@code Person} that we are building.
     */
    public PersonBuilder withName(String name) {
        this.name = new Name(name);
        return this;
    }

    /**
     * Parses the {@code tags} into a {@code Set<Tag>} and sets it to the {@code Person} that we are building.
     */
    public PersonBuilder withTags(String ... tags) {
        this.tags = SampleDataUtil.getTagSet(tags);
        return this;
    }

    /**
     * Sets the {@code Address} of the {@code Person} that we are building.
     */
    public PersonBuilder withAddress(String address) {
        this.address = new Address(address);
        return this;
    }

    /**
     * Sets the {@code Phone} of the {@code Person} that we are building.
     */
    public PersonBuilder withPhone(String phone) {
        this.phone = new Phone(phone);
        return this;
    }

    /**
     * Sets the {@code Email} of the {@code Person} that we are building.
     */
    public PersonBuilder withEmail(String email) {
        this.email = new Email(email);
        return this;
    }

    /**
     * Sets the company of the contact being built.
     */
    public PersonBuilder withCompany(String company) {
        this.company = company;
        return this;
    }

    /**
     * Sets the optional preferred contact method of the contact being built.
     */
    public PersonBuilder withContactMethod(String method) {
        preferredContactMethod = method;
        return this;
    }

    public Person build() {
        return new Person(name, phone, email, address, tags, company, contactMethod);
    }

    /** Sets the exhibitor organisation. */
    public PersonBuilder withCompany(String company) {
        this.company = Optional.of(new Company(company));
        return this;
    }

    /** Sets the representative's preferred contact method. */
    public PersonBuilder withContactMethod(String method) {
        contactMethod = Optional.of(ContactMethod.fromString(method));
        return this;
    }

}
