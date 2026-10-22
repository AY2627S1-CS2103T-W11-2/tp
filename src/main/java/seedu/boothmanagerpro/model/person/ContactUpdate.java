package seedu.boothmanagerpro.model.person;

import static seedu.boothmanagerpro.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import seedu.boothmanagerpro.model.tag.Tag;

/**
 * Immutable, validated partial update. Empty outer optionals preserve existing values.
 * A present empty date clears the date; a present empty tag set clears tags.
 */
public record ContactUpdate(Optional<Name> name, Optional<Company> company, Optional<Email> email,
        Optional<Phone> phone, Optional<ContactMethod> method, Optional<EnquiryStatus> status,
        Optional<Optional<FollowUpDate>> date, Optional<Set<Tag>> tags) {

    /** Copies tags defensively so a parsed update cannot change before execution. */
    public ContactUpdate {
        requireAllNonNull(name, company, email, phone, method, status, date, tags);
        tags = tags.map(values -> Collections.unmodifiableSet(new LinkedHashSet<>(values)));
    }

    /** Replaces only supplied attributes, retaining the legacy address and all omitted fields. */
    public Person applyTo(Person original) {
        return new Person(name.orElse(original.getName()), phone.orElse(original.getPhone()),
                email.orElse(original.getEmail()), original.getAddress(), tags.orElse(original.getTags()),
                company.or(original::getCompany), method.or(original::getContactMethod),
                status.orElse(original.getEnquiryStatus()), date.orElse(original.getFollowUpDate()));
    }
}
