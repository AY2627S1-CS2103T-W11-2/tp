package seedu.boothmanagerpro.storage;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.boothmanagerpro.commons.exceptions.IllegalValueException;
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
 * JSON representation of {@link Person}, storing exhibitor fields as nullable strings rather than Optional objects.
 * Missing company and contact method fields remain absent when loading legacy records; present values are validated.
 */
class JsonAdaptedPerson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Person's %s field is missing!";
    public static final String DEFAULT_ADDRESS = "Not provided";

    private final String name;
    private final String phone;
    private final String email;
    private final String address;
    private final String company;
    private final String contactMethod;
    private final String enquiryStatus;
    private final String followUpDate;
    private final List<JsonAdaptedTag> tags = new ArrayList<>();

    /**
     * Creates a stored contact, accepting absent exhibitor fields in older data files.
     */
    public JsonAdaptedPerson(String name, String phone, String email, String address, List<JsonAdaptedTag> tags) {
        this(name, phone, email, address, tags, null, null);
    }

    /** Keeps existing callers compatible while defaulting new metadata during conversion. */
    public JsonAdaptedPerson(String name, String phone, String email, String address, List<JsonAdaptedTag> tags,
            String company, String contactMethod) {
        this(name, phone, email, address, tags, company, contactMethod, null, null);
    }

    /**
     * Captures raw JSON fields; validation is deferred to {@link #toModelType()}.
     * Null company/method values represent absent fields. Blank or unsupported values are not silently discarded.
     */
    @JsonCreator
    public JsonAdaptedPerson(@JsonProperty("name") String name, @JsonProperty("phone") String phone,
            @JsonProperty("email") String email, @JsonProperty("address") String address,
            @JsonProperty("tags") List<JsonAdaptedTag> tags, @JsonProperty("company") String company,
            @JsonProperty("contactMethod") String contactMethod,
            @JsonProperty("enquiryStatus") String enquiryStatus, @JsonProperty("followUpDate") String followUpDate) {
        this.enquiryStatus = enquiryStatus;
        this.followUpDate = followUpDate;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.company = company;
        this.contactMethod = contactMethod;
        if (tags != null) {
            this.tags.addAll(tags);
        }
    }

    /**
     * Converts a given {@code Person} into this class for Jackson use.
     */
    public JsonAdaptedPerson(Person source) {
        enquiryStatus = source.getEnquiryStatus().toString();
        followUpDate = source.getFollowUpDate().map(FollowUpDate::toIsoDate).orElse(null);
        name = source.getName().fullName;
        phone = source.getPhone().value;
        email = source.getEmail().value;
        address = source.getAddress().value;
        company = source.getCompany().map(value -> value.value).orElse(null);
        contactMethod = source.getContactMethod().map(Object::toString).orElse(null);
        tags.addAll(source.getTags().stream()
                .map(JsonAdaptedTag::new)
                .collect(Collectors.toList()));
    }

    /**
     * Validates all supplied values and reconstructs an immutable contact using current model constraints.
     * Tag order is retained, duplicates collapse, and legacy contacts may have no company or method.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted person.
     */
    public Person toModelType() throws IllegalValueException {
        return new Person(getModelName(), getModelPhone(), getModelEmail(), getModelAddress(),
                getModelTags(), getModelCompany(), getModelContactMethod(),
                getModelEnquiryStatus(), getModelFollowUpDate());
    }

    private Name getModelName() throws IllegalValueException {
        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName()));
        }
        if (!Name.isValidName(name)) {
            throw new IllegalValueException(Name.MESSAGE_CONSTRAINTS);
        }
        return new Name(name);
    }

    private Phone getModelPhone() throws IllegalValueException {
        if (phone == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName()));
        }
        if (!Phone.isValidPhone(phone)) {
            throw new IllegalValueException(Phone.MESSAGE_CONSTRAINTS);
        }
        return new Phone(phone);
    }

    private Email getModelEmail() throws IllegalValueException {
        if (email == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Email.class.getSimpleName()));
        }
        if (!Email.isValidEmail(email)) {
            throw new IllegalValueException(Email.MESSAGE_CONSTRAINTS);
        }
        return new Email(email);
    }

    /**
     * Converts the hidden legacy address, which users cannot see or edit in the app. Like other fields, surrounding
     * whitespace is removed; line breaks and repeated spaces become single spaces, and a blank value uses the default.
     */
    private Address getModelAddress() throws IllegalValueException {
        String normalised = address == null ? "" : address.strip().replaceAll("\\s+", " ");
        if (normalised.isEmpty()) {
            return new Address(DEFAULT_ADDRESS);
        }
        if (!Address.isValidAddress(normalised)) {
            throw new IllegalValueException(Address.MESSAGE_CONSTRAINTS);
        }
        return new Address(normalised);
    }

    private Set<Tag> getModelTags() throws IllegalValueException {
        final List<Tag> personTags = new ArrayList<>();
        for (JsonAdaptedTag tag : tags) {
            if (tag == null) {
                throw new IllegalValueException("Tags list must not contain null entries.");
            }
            personTags.add(tag.toModelType());
        }
        return new LinkedHashSet<>(personTags);
    }

    private Optional<Company> getModelCompany() throws IllegalValueException {
        if (company == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(new Company(company));
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(e.getMessage());
        }
    }

    private Optional<ContactMethod> getModelContactMethod() throws IllegalValueException {
        if (contactMethod == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(ContactMethod.fromString(contactMethod));
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(e.getMessage());
        }
    }

    private EnquiryStatus getModelEnquiryStatus() throws IllegalValueException {
        if (enquiryStatus == null) {
            return EnquiryStatus.NEEDS_FOLLOW_UP;
        }
        try {
            return EnquiryStatus.fromString(enquiryStatus);
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(e.getMessage());
        }
    }

    private Optional<FollowUpDate> getModelFollowUpDate() throws IllegalValueException {
        if (followUpDate == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(new FollowUpDate(followUpDate));
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(e.getMessage());
        }
    }

}
