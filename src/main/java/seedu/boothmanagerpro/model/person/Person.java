package seedu.boothmanagerpro.model.person;

import static seedu.boothmanagerpro.commons.util.AppUtil.checkArgument;
import static seedu.boothmanagerpro.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import seedu.boothmanagerpro.commons.util.ToStringBuilder;
import seedu.boothmanagerpro.model.tag.Tag;

/**
 * An immutable contact with validated values and defensively copied tags.
 * Company may be absent in legacy records; contact method may be omitted by the user.
 * Duplicate matching uses {@link #isSamePerson(Person)}, while {@link #equals(Object)} compares all stored values.
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
    private final Set<Tag> tags = new LinkedHashSet<>();
    private final Optional<Company> company;
    private final Optional<ContactMethod> contactMethod;

    /**
     * Creates a legacy contact without company or preferred contact method.
     * All arguments must be non-null. New add commands use the constructor with exhibitor fields.
     */
    public Person(Name name, Phone phone, Email email, Address address, Set<Tag> tags) {
        this(name, phone, email, address, tags, Optional.empty(), Optional.empty());
    }

    /**
     * Creates a contact from validated values. All arguments, including optional containers, must be non-null.
     * Tags are copied in iteration order. Empty company is reserved for compatibility with legacy records;
     * {@code AddCommandParser} requires a company for new additions.
     */
    public Person(Name name, Phone phone, Email email, Address address, Set<Tag> tags,
            Optional<Company> company, Optional<ContactMethod> contactMethod) {
        requireAllNonNull(name, phone, email, address, tags, company, contactMethod);
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.tags.addAll(tags);
        this.company = company;
        this.contactMethod = contactMethod;
    }

    /** Returns the exhibitor organisation, or empty for a legacy contact without company information. */
    public Optional<Company> getCompany() {
        return company;
    }

    /** Returns the preferred method; empty means unspecified, which is distinct from {@code OTHER}. */
    public Optional<ContactMethod> getContactMethod() {
        return contactMethod;
    }

    /**
     * Creates a new exhibitor with a required company and no supplied postal address.
     * Keeps the legacy address default in the model so parsers do not depend on UI compatibility details.
     * All arguments must be non-null; an empty method means no preference was supplied.
     */
    public static Person createExhibitor(Name name, Company company, Email email, Phone phone,
            Optional<ContactMethod> contactMethod, Set<Tag> tags) {
        return new Person(name, phone, email, new Address("Not provided"), tags,
                Optional.of(company), contactMethod);
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
     * Matches normalised email, or case-insensitive name and company when both companies are present.
     * Legacy contacts without a company only match by email. Null never matches.
     * This OR-based duplicate rule is not transitive: updates must check every other stored contact.
     */
    public boolean isSamePerson(Person otherPerson) {
        if (otherPerson == this) {
            return true;
        }

        return otherPerson != null
                && (email.equals(otherPerson.email)
                || company.isPresent() && otherPerson.company.isPresent()
                && name.fullName.equalsIgnoreCase(otherPerson.name.fullName)
                && company.get().value.equalsIgnoreCase(otherPerson.company.get().value));
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
                && company.equals(otherPerson.company)
                && contactMethod.equals(otherPerson.contactMethod)
                && tags.equals(otherPerson.tags);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, phone, email, address, tags, company, contactMethod);
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
