package seedu.boothmanagerpro.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.boothmanagerpro.commons.util.CollectionUtil.requireAllNonNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.boothmanagerpro.commons.util.StringUtil;
import seedu.boothmanagerpro.model.person.exceptions.DuplicatePersonException;
import seedu.boothmanagerpro.model.person.exceptions.PersonNotFoundException;

/**
 * A list of persons that enforces uniqueness between its elements and does not allow nulls.
 * A person is considered unique by comparing using {@code Person#isSamePerson(Person)}. As such, adding and updating of
 * persons uses Person#isSamePerson(Person) for equality so as to ensure that the person being added or updated is
 * unique in terms of identity in the UniquePersonList. However, the removal of a person uses Person#equals(Object) so
 * as to ensure that the person with exactly the same fields will be removed.
 *
 * Supports a minimal set of list operations.
 *
 * @see Person#isSamePerson(Person)
 */
public class UniquePersonList implements Iterable<Person> {

    private final ObservableList<Person> internalList = FXCollections.observableArrayList();
    private final ObservableList<Person> internalUnmodifiableList =
            FXCollections.unmodifiableObservableList(internalList);

    /**
     * Returns true if the list contains an equivalent person as the given argument.
     */
    public boolean contains(Person toCheck) {
        requireNonNull(toCheck);
        return internalList.stream().anyMatch(toCheck::isSamePerson);
    }

    /**
     * Adds a person to the list.
     * The person must not already exist in the list.
     */
    public void add(Person toAdd) {
        requireNonNull(toAdd);
        if (contains(toAdd)) {
            throw new DuplicatePersonException();
        }
        internalList.add(toAdd);
    }

    /**
     * Replaces the person {@code target} in the list with {@code editedPerson}.
     * {@code target} must exist in the list.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the list.
     */
    public void setPerson(Person target, Person editedPerson) {
        requireAllNonNull(target, editedPerson);

        int index = internalList.indexOf(target);
        if (index == -1) {
            throw new PersonNotFoundException();
        }

        // Duplicate matching is not transitive; compare against every record except the target itself.
        if (internalList.stream().anyMatch(person -> !person.equals(target) && person.isSamePerson(editedPerson))) {
            throw new DuplicatePersonException();
        }

        internalList.set(index, editedPerson);
    }

    /**
     * Removes the equivalent person from the list.
     * The person must exist in the list.
     */
    public void remove(Person toRemove) {
        requireNonNull(toRemove);
        if (!internalList.remove(toRemove)) {
            throw new PersonNotFoundException();
        }
    }

    public void setPersons(UniquePersonList replacement) {
        requireNonNull(replacement);
        internalList.setAll(replacement.internalList);
    }

    /**
     * Replaces the contents of this list with {@code persons}.
     * {@code persons} must not contain duplicate persons.
     */
    public void setPersons(List<Person> persons) {
        requireAllNonNull(persons);
        if (!personsAreUnique(persons)) {
            throw new DuplicatePersonException();
        }

        internalList.setAll(persons);
    }

    /**
     * Returns the backing list as an unmodifiable {@code ObservableList}.
     */
    public ObservableList<Person> asUnmodifiableObservableList() {
        return internalUnmodifiableList;
    }

    @Override
    public Iterator<Person> iterator() {
        return internalList.iterator();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof UniquePersonList otherUniquePersonList)) {
            return false;
        }

        return internalList.equals(otherUniquePersonList.internalList);
    }

    @Override
    public int hashCode() {
        return internalList.hashCode();
    }

    @Override
    public String toString() {
        return internalList.toString();
    }

    /**
     * Checks both identity rules in one pass. Separate sets retain the OR-based duplicate rule:
     * an email conflict or a name/company conflict is enough to reject the replacement.
     */
    private boolean personsAreUnique(List<Person> persons) {
        Set<Email> emails = new HashSet<>();
        Set<NameCompanyIdentity> exhibitors = new HashSet<>();
        for (Person person : persons) {
            if (!emails.add(person.getEmail())) {
                return false;
            }
            if (person.getCompany().isPresent()
                    && !exhibitors.add(NameCompanyIdentity.from(person))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Matches the case-insensitive name/company comparison in Person.isSamePerson.
     * Legacy contacts without a company are checked by email only. Keeping the fields separate
     * avoids treating different name/company pairs as the same concatenated string.
     */
    private record NameCompanyIdentity(String name, String company) {
        private static NameCompanyIdentity from(Person person) {
            return new NameCompanyIdentity(StringUtil.foldCase(person.getName().fullName),
                    StringUtil.foldCase(person.getCompany().orElseThrow().value));
        }
    }
}
