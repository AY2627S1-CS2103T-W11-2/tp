package seedu.boothmanagerpro.model.person;

import static seedu.boothmanagerpro.commons.util.AppUtil.checkArgument;
import static seedu.boothmanagerpro.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import seedu.boothmanagerpro.commons.util.ToStringBuilder;
import seedu.boothmanagerpro.model.tag.Tag;

/**
 * Represents a Person in the address book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Person {

    public static final String MESSAGE_COMPANY_CONSTRAINTS = "Company must contain 1 to 100 characters.";
    public static final String MESSAGE_METHOD_CONSTRAINTS = "Contact method must be email, phone, or other.";

    // Identity fields
    private final Name name;
    private final Phone phone;
    private final Email email;

    // Data fields
    private final Address address;
    private final String company;
    private final String preferredContactMethod;
    private final Set<Tag> tags = new HashSet<>();

    /**
     * Creates a legacy contact with unspecified exhibitor fields. All supplied fields must be non-null.
     */
    public Person(Name name, Phone phone, Email email, Address address, Set<Tag> tags) {
        this(name, phone, email, address, tags, "", "");
    }

    /**
     * Creates a contact with exhibitor fields. Empty values represent fields absent from legacy records.
     * The add feature is responsible for requiring a company for new exhibitor contacts.
     */
    public Person(Name name, Phone phone, Email email, Address address, Set<Tag> tags,
            String company, String preferredContactMethod) {
        requireAllNonNull(name, phone, email, address, tags, company, preferredContactMethod);
        checkArgument(isValidCompany(company), MESSAGE_COMPANY_CONSTRAINTS);
        checkArgument(isValidContactMethod(preferredContactMethod), MESSAGE_METHOD_CONSTRAINTS);
        this.company = company.strip();
        this.preferredContactMethod = preferredContactMethod.strip().toLowerCase(Locale.ROOT);
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.tags.addAll(tags);
    }

    /**
     * Returns whether a company is valid, including an empty value for a legacy record.
     */
    public static boolean isValidCompany(String company) {
        return company.isEmpty() || (!company.isBlank() && company.strip().length() <= 100);
    }

    /**
     * Returns whether a preferred contact method is supported or omitted.
     */
    public static boolean isValidContactMethod(String method) {
        return method.isEmpty() || Set.of("email", "phone", "other").contains(method.strip().toLowerCase(Locale.ROOT));
    }

    public String getCompany() {
        return company;
    }

    public String getPreferredContactMethod() {
        return preferredContactMethod;
    }

    public Name getName() {
        return name;
    }

    public Phone getPhone() {
        return phone;
    }

    public Email getEmail() {
        return email;
    }

    public Address getAddress() {
        return address;
    }

    /**
     * Returns an immutable tag set, which throws {@code UnsupportedOperationException}
     * if modification is attempted.
     */
    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    /**
     * Returns true if both contacts share a normalized email or a name-and-company combination.
     * This defines a weaker notion of equality between two persons.
     */
    public boolean isSamePerson(Person otherPerson) {
        if (otherPerson == this) {
            return true;
        }

        return otherPerson != null
                && (email.value.equalsIgnoreCase(otherPerson.email.value)
                || (name.fullName.strip().equalsIgnoreCase(otherPerson.name.fullName.strip())
                && company.equalsIgnoreCase(otherPerson.company)));
    }

    /**
     * Returns true if both persons have the same identity and data fields.
     * This defines a stronger notion of equality between two persons.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Person otherPerson)) {
            return false;
        }

        return name.equals(otherPerson.name)
                && phone.equals(otherPerson.phone)
                && email.equals(otherPerson.email)
                && address.equals(otherPerson.address)
                && tags.equals(otherPerson.tags)
                && company.equals(otherPerson.company)
                && preferredContactMethod.equals(otherPerson.preferredContactMethod);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, phone, email, address, tags, company, preferredContactMethod);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("phone", phone)
                .add("email", email)
                .add("address", address)
                .add("tags", tags)
                .add("company", company)
                .add("preferredContactMethod", preferredContactMethod)
                .toString();
    }

}
