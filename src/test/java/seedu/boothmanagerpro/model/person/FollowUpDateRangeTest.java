package seedu.boothmanagerpro.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.boothmanagerpro.testutil.PersonBuilder;

public class FollowUpDateRangeTest {
    @Test
    public void matches_includesBothEndpointsAndDatesBetweenThem() {
        FollowUpDateRange range = FollowUpDateRange.parse("20261101..20261107");
        for (String date : List.of("20261101", "20261104", "20261107")) {
            assertTrue(range.matches(new FollowUpDate(date)));
        }
        assertFalse(range.matches(new FollowUpDate("20261031")));
        assertFalse(range.matches(new FollowUpDate("20261108")));
    }

    @Test
    public void parse_singleDayLeapDayAndYearLimits_accepts() {
        for (String date : List.of("00010101", "20240229", "99991231")) {
            FollowUpDateRange range = FollowUpDateRange.parse(date + ".." + date);
            assertTrue(range.matches(new FollowUpDate(date)));
            assertEquals(range.start(), range.end());
        }
        assertTrue(FollowUpDateRange.parse("20261231..20270101").matches(new FollowUpDate("20270101")));
        assertEquals(FollowUpDateRange.parse("20261101..20261107"),
                FollowUpDateRange.parse(" 20261101 .. 20261107 "));
    }

    @Test
    public void parse_reversedIncompleteOrInvalidIntervals_rejects() {
        for (String value : List.of("20261107..20261101", "20261101", "..", "20261101..", "..20261107",
                "20261101...20261107", "20261101..20261107..20261108", "20230229..20230301",
                "00000101..20261107", "20261101..100000101", "today..20261107", "2026-11-01..20261107")) {
            IllegalArgumentException error =
                    assertThrows(IllegalArgumentException.class, () -> FollowUpDateRange.parse(value), value);
            assertEquals(FollowUpDateRange.MESSAGE_CONSTRAINTS, error.getMessage());
        }
        assertThrows(NullPointerException.class, () -> FollowUpDateRange.parse(null));
        assertThrows(NullPointerException.class, () -> new FollowUpDateRange(null, new FollowUpDate("20261107")));
        assertThrows(NullPointerException.class, () -> new FollowUpDateRange(new FollowUpDate("20261101"), null));
        assertThrows(NullPointerException.class, () -> FollowUpDateRange.parse("20261101..20261107").matches(null));
    }

    @Test
    public void predicate_copiesRangeCriteriaAndCombinesWithOtherFields() {
        Set<String> dates = new HashSet<>(Set.of("20261101..20261107"));
        Map<String, Set<String>> criteria = new HashMap<>(Map.of("f/", dates, "t/", Set.of("vip")));
        ContactMatchesFieldsPredicate predicate = new ContactMatchesFieldsPredicate(criteria);
        dates.clear();
        dates.add("20261201..20261207");
        criteria.clear();
        assertTrue(predicate.test(new PersonBuilder().withFollowUpDate("20261104").withTags("vip").build()));
        assertFalse(predicate.test(new PersonBuilder().withFollowUpDate("20261204").withTags("vip").build()));
        assertFalse(predicate.test(new PersonBuilder().withFollowUpDate("20261104").withTags("other").build()));
        assertFalse(predicate.test(new PersonBuilder().withTags("vip").build()));
    }
}
