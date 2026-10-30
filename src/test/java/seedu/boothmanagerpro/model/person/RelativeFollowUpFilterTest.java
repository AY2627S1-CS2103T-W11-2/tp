package seedu.boothmanagerpro.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.boothmanagerpro.testutil.PersonBuilder;

public class RelativeFollowUpFilterTest {
    private static final Clock TODAY = Clock.fixed(Instant.parse("2026-10-29T16:30:00Z"),
            ZoneId.of("Asia/Singapore"));

    @Test
    public void relativeFilters_distinguishPastTodayAndFuture() {
        Person yesterday = new PersonBuilder().withFollowUpDate("20261029").build();
        Person today = new PersonBuilder().withFollowUpDate("20261030").build();
        Person tomorrow = new PersonBuilder().withFollowUpDate("20261031").build();
        Person undated = new PersonBuilder().build();
        for (String keyword : Set.of("today", "due", "overdue")) {
            ContactMatchesFieldsPredicate predicate = predicate(Set.of(keyword), TODAY);
            assertEquals(!keyword.equals("today"), predicate.test(yesterday));
            assertEquals(!keyword.equals("overdue"), predicate.test(today));
            assertFalse(predicate.test(tomorrow));
            assertFalse(predicate.test(undated));
        }
    }

    @Test
    public void alternativesAndOtherFields_keepOrAndSemantics() {
        ContactMatchesFieldsPredicate predicate = new ContactMatchesFieldsPredicate(
                Map.of("f/", Set.of("today", "20261031"), "t/", Set.of("vip")), TODAY);
        assertTrue(predicate.test(new PersonBuilder().withFollowUpDate("20261030").withTags("vip").build()));
        assertTrue(predicate.test(new PersonBuilder().withFollowUpDate("20261031").withTags("vip").build()));
        assertFalse(predicate.test(new PersonBuilder().withFollowUpDate("20261029").withTags("vip").build()));
        assertFalse(predicate.test(new PersonBuilder().withFollowUpDate("20261030").withTags("other").build()));
    }

    @Test
    public void referenceDate_usesClockTimeZone() {
        Person contact = new PersonBuilder().withFollowUpDate("20261030").build();
        assertTrue(predicate(Set.of("today"), TODAY).test(contact));
        assertFalse(predicate(Set.of("today"), TODAY.withZone(ZoneOffset.UTC)).test(contact));
    }

    @Test
    public void leapAndYearBoundaries_compareActualCalendarDates() {
        assertTrue(RelativeFollowUpFilter.OVERDUE.matches(new FollowUpDate("20240229"),
                LocalDate.of(2024, 3, 1)));
        assertTrue(RelativeFollowUpFilter.DUE.matches(new FollowUpDate("20261231"),
                LocalDate.of(2027, 1, 1)));
        assertFalse(RelativeFollowUpFilter.OVERDUE.matches(new FollowUpDate("00010101"),
                LocalDate.of(1, 1, 1)));
        assertTrue(RelativeFollowUpFilter.TODAY.matches(new FollowUpDate("99991231"),
                LocalDate.of(9999, 12, 31)));
    }

    @Test
    public void keywordParsing_doesNotAcceptMisspellingsOrDatesAsRelativeFilters() {
        assertEquals(RelativeFollowUpFilter.DUE, RelativeFollowUpFilter.fromKeyword(" DuE ").orElseThrow());
        for (String input : Set.of("", "tomorrow", "due soon", "20261030")) {
            assertTrue(RelativeFollowUpFilter.fromKeyword(input).isEmpty());
        }
        assertThrows(NullPointerException.class, () -> RelativeFollowUpFilter.fromKeyword(null));
        assertThrows(NullPointerException.class, () -> RelativeFollowUpFilter.TODAY.matches(null, LocalDate.now()));
        FollowUpDate validDate = new FollowUpDate("20261030");
        assertThrows(NullPointerException.class, () -> RelativeFollowUpFilter.TODAY.matches(validDate, null));
    }

    @Test
    public void equality_includesReferenceDate() {
        ContactMatchesFieldsPredicate predicate = predicate(Set.of("today"), TODAY);
        ContactMatchesFieldsPredicate same = predicate(Set.of("today"), TODAY);
        assertEquals(predicate, same);
        assertEquals(predicate.hashCode(), same.hashCode());
        assertNotEquals(predicate, predicate(Set.of("today"), Clock.offset(TODAY, java.time.Duration.ofDays(1))));
    }

    private ContactMatchesFieldsPredicate predicate(Set<String> values, Clock clock) {
        return new ContactMatchesFieldsPredicate(Map.of("f/", values), clock);
    }
}
