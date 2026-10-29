package seedu.boothmanagerpro.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.boothmanagerpro.testutil.Assert.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

public class FollowUpDateTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new FollowUpDate(null));
    }

    @Test
    public void constructor_invalidFormat_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new FollowUpDate(""));
        assertThrows(IllegalArgumentException.class, () -> new FollowUpDate("2026-10-25"));
        assertThrows(IllegalArgumentException.class, () -> new FollowUpDate("2026102"));
        assertThrows(IllegalArgumentException.class, () -> new FollowUpDate("202610251"));
        assertThrows(IllegalArgumentException.class, () -> new FollowUpDate("abcdefgh"));
    }

    @Test
    public void constructor_invalidCalendarDate_throwsIllegalArgumentException() {
        // Year 0000 is disallowed
        assertThrows(IllegalArgumentException.class, () -> new FollowUpDate("00000101"));
        // Invalid month / day
        assertThrows(IllegalArgumentException.class, () -> new FollowUpDate("20261301"));
        assertThrows(IllegalArgumentException.class, () -> new FollowUpDate("20260431"));
        assertThrows(IllegalArgumentException.class, () -> new FollowUpDate("20260230"));
        // Non-leap year Feb 29
        assertThrows(IllegalArgumentException.class, () -> new FollowUpDate("20250229"));
    }

    @Test
    public void constructor_validDatesIncludingLeapYearAndBoundaries_success() {
        FollowUpDate leapDay = new FollowUpDate(" 20240229 ");
        assertEquals(LocalDate.of(2024, 2, 29), leapDay.value);
        assertEquals("20240229", leapDay.toIsoDate());
        assertEquals("29 Feb 2024", leapDay.toString());

        FollowUpDate minYear = new FollowUpDate("00010101");
        assertEquals("00010101", minYear.toIsoDate());

        FollowUpDate maxYear = new FollowUpDate("99991231");
        assertEquals("99991231", maxYear.toIsoDate());
    }

    @Test
    public void equalsAndHashCode() {
        FollowUpDate date = new FollowUpDate("20261115");

        assertTrue(date.equals(date));
        assertTrue(date.equals(new FollowUpDate("20261115")));
        assertEquals(date.hashCode(), new FollowUpDate("20261115").hashCode());

        assertFalse(date.equals(null));
        assertFalse(date.equals("20261115"));
        assertFalse(date.equals(new FollowUpDate("20261116")));
    }
}

