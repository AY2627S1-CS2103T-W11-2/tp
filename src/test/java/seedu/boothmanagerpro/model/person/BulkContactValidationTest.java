package seedu.boothmanagerpro.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import javafx.collections.ListChangeListener;
import seedu.boothmanagerpro.model.AddressBook;
import seedu.boothmanagerpro.model.person.exceptions.DuplicatePersonException;
import seedu.boothmanagerpro.testutil.PersonBuilder;

/** Checks bulk identity validation against the public contact identity rule and model guarantees. */
public class BulkContactValidationTest {
    @Test
    public void replacement_identityMatrix_agreesWithPairwiseRule() {
        List<Person> contacts = List.of(
                contact("Alex Tan", "Acme", "alex@example.com"),
                contact("alex tan", "ACME", "other@example.com"),
                contact("Another Person", "Another Company", "ALEX@example.com"),
                contact("Alex Tan", "Other Company", "different@example.com"),
                contact("Another Person", "Acme", "third@example.com"),
                contact("Iris Tan", "Istanbul", "iris@example.com"),
                contact("\u0131RIS TAN", "\u0130STANBUL", "turkish@example.com"),
                contact("Jos\u00e9 Tan", "Caf\u00e9", "accent@example.com"),
                contact("Jose\u0301 Tan", "Cafe\u0301", "decomposed@example.com"),
                contact("\u03a3 Tan", "\u039f\u03a3", "sigma@example.com"),
                contact("\u03c2 Tan", "\u03bf\u03c2", "finalsigma@example.com"),
                contact("\u00df Tan", "Acme", "sharp@example.com"),
                contact("SS Tan", "Acme", "double@example.com"),
                contact("\uD801\uDC00 Tan", "Acme", "upper@example.com"),
                contact("\uD801\uDC28 Tan", "Acme", "lower@example.com"),
                legacy("Alex Tan", "legacy@example.com"));
        for (Person first : contacts) {
            for (Person second : contacts) {
                assertReplacementMatchesIdentityRule(List.of(first, second));
            }
        }
    }

    @Test
    public void replacement_nonTransitiveIdentity_rejectsConflictsInEveryOrder() {
        Person first = contact("Alex Tan", "Acme", "first@example.com");
        Person bridge = contact("Alex Tan", "Acme", "shared@example.com");
        Person last = contact("Ben Tan", "Beta", "shared@example.com");
        assertTrue(first.isSamePerson(bridge));
        assertTrue(bridge.isSamePerson(last));
        assertFalse(first.isSamePerson(last));
        assertReplacementMatchesIdentityRule(List.of(first, last));
        for (List<Person> order : List.of(List.of(first, bridge, last), List.of(first, last, bridge),
                List.of(bridge, first, last), List.of(bridge, last, first),
                List.of(last, first, bridge), List.of(last, bridge, first))) {
            assertReplacementMatchesIdentityRule(order);
        }
    }

    @Test
    public void replacement_legacyContacts_onlyEmailCreatesAConflict() {
        Person first = legacy("Alex Tan", "one@example.com");
        Person sameLegacyName = legacy("Alex Tan", "two@example.com");
        Person exhibitor = contact("Alex Tan", "Acme", "three@example.com");
        UniquePersonList list = new UniquePersonList();
        list.setPersons(List.of(first, sameLegacyName, exhibitor));
        assertEquals(List.of(first, sameLegacyName, exhibitor), list.asUnmodifiableObservableList());
        Person duplicateEmail = contact("Different Person", "Different Company", "ONE@example.com");
        assertThrows(DuplicatePersonException.class, () ->
                list.setPersons(List.of(first, sameLegacyName, duplicateEmail)));
        assertEquals(List.of(first, sameLegacyName, exhibitor), list.asUnmodifiableObservableList());
    }

    @Test
    public void replacement_rejectedInput_leavesContentsAndListenersUntouched() {
        UniquePersonList list = new UniquePersonList();
        Person original = contact("Original Person", "Original Company", "original@example.com");
        list.add(original);
        AtomicInteger notifications = new AtomicInteger();
        list.asUnmodifiableObservableList().addListener((ListChangeListener<Person>) change -> {
            while (change.next()) {
                notifications.incrementAndGet();
            }
        });
        Person replacement = contact("New Person", "New Company", "new@example.com");
        Person duplicate = contact("NEW PERSON", "NEW COMPANY", "different@example.com");
        assertThrows(DuplicatePersonException.class, () ->
                list.setPersons(List.of(replacement, duplicate)));
        List<Person> containsNull = new ArrayList<>(List.of(replacement));
        containsNull.add(null);
        assertThrows(NullPointerException.class, () -> list.setPersons(containsNull));
        assertEquals(List.of(original), list.asUnmodifiableObservableList());
        assertEquals(0, notifications.get());
    }

    @Test
    public void replacement_emptyAndSingleton_preserveOrderAndRejectNullEntries() {
        UniquePersonList list = new UniquePersonList();
        Person first = contact("First Person", "First Company", "first@example.com");
        list.setPersons(List.of(first));
        assertEquals(List.of(first), list.asUnmodifiableObservableList());
        assertThrows(NullPointerException.class, () -> list.setPersons(Collections.singletonList(null)));
        assertEquals(List.of(first), list.asUnmodifiableObservableList());
        list.setPersons(List.of());
        assertTrue(list.asUnmodifiableObservableList().isEmpty());
    }

    @Test
    public void replacement_shuffledBatches_matchExistingIdentityRule() {
        List<Person> candidates = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            candidates.add(contact("Person " + i, "Company " + i, "person" + i + "@example.com"));
        }
        candidates.add(contact("Person 2", "Company 2", "different@example.com"));
        candidates.add(contact("Unrelated Person", "Unrelated Company", "person9@example.com"));
        candidates.add(legacy("Person 2", "legacy@example.com"));
        Random random = new Random(2103);
        for (int round = 0; round < 50; round++) {
            Collections.shuffle(candidates, random);
            int size = round % candidates.size();
            assertReplacementMatchesIdentityRule(new ArrayList<>(candidates.subList(0, size)));
        }
    }

    @Test
    public void snapshots_thousandContacts_preserveOrderAndRemainIndependent() {
        List<Person> people = IntStream.range(0, 1000)
                .mapToObj(i -> contact("Exhibitor " + i, "Company " + i, "person" + i + "@example.com"))
                .toList();
        AddressBook original = new AddressBook();
        original.setPersons(people);
        AddressBook snapshot = new AddressBook(original);
        assertEquals(people, snapshot.getPersonList());
        original.removePerson(people.getFirst());
        assertEquals(1000, snapshot.getPersonList().size());
        assertEquals(people.getFirst(), snapshot.getPersonList().getFirst());
        snapshot.removePerson(people.getLast());
        assertEquals(people.getLast(), original.getPersonList().getLast());
        assertEquals(999, original.getPersonList().size());
    }

    @Test
    public void replacement_copiesCallerListAndRetainsExistingObservableView() {
        UniquePersonList list = new UniquePersonList();
        var view = list.asUnmodifiableObservableList();
        Person first = contact("First Person", "Acme", "first@example.com");
        Person second = contact("Second Person", "Acme", "second@example.com");
        List<Person> input = new ArrayList<>(List.of(second, first));
        list.setPersons(input);
        input.clear();
        assertEquals(List.of(second, first), view);
        list.setPersons(List.of(first));
        assertEquals(List.of(first), view);
    }

    private static void assertReplacementMatchesIdentityRule(List<Person> input) {
        boolean hasDuplicate = false;
        for (int first = 0; first < input.size(); first++) {
            for (int second = first + 1; second < input.size(); second++) {
                hasDuplicate |= input.get(first).isSamePerson(input.get(second));
            }
        }
        UniquePersonList list = new UniquePersonList();
        if (hasDuplicate) {
            assertThrows(DuplicatePersonException.class, () -> list.setPersons(input));
            assertTrue(list.asUnmodifiableObservableList().isEmpty());
        } else {
            list.setPersons(input);
            assertEquals(input, list.asUnmodifiableObservableList());
        }
    }

    private static Person contact(String name, String company, String email) {
        return new PersonBuilder().withName(name).withCompany(company).withEmail(email).build();
    }

    private static Person legacy(String name, String email) {
        return new PersonBuilder().withName(name).withEmail(email).build();
    }
}
