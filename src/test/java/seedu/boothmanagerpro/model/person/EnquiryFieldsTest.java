package seedu.boothmanagerpro.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.boothmanagerpro.testutil.PersonBuilder;

/** Boundary and identity checks for the shared enquiry value objects. */
public class EnquiryFieldsTest {
    @Test
    public void status_acceptsLabelsAndRejectsUnknownValues() {
        for (EnquiryStatus status : EnquiryStatus.values()) {
            assertEquals(status, EnquiryStatus.fromString(" " + status.toString().toUpperCase(java.util.Locale.ROOT)
                    + " "));
        }
        for (String invalid : new String[]{"", "new", "in_progress", "done"}) {
            assertThrows(IllegalArgumentException.class, () -> EnquiryStatus.fromString(invalid));
        }
        assertThrows(NullPointerException.class, () -> EnquiryStatus.fromString(null));
    }

    @Test
    public void date_validatesCalendarAndExactFormat() {
        assertEquals(LocalDate.of(2024, 2, 29), new FollowUpDate(" 20240229 ").value);
        assertEquals("00010101", new FollowUpDate("00010101").toString());
        assertEquals("99991231", new FollowUpDate("99991231").toString());
        for (String invalid : new String[]{"", "none", "20230229", "20260431", "20261301", "20260100",
            "2026-10-25", "2026102", "202610250", "20261025T1200", "00000101", "2026 1025"}) {
            assertThrows(IllegalArgumentException.class, () -> new FollowUpDate(invalid));
        }
        assertThrows(NullPointerException.class, () -> new FollowUpDate(null));
    }

    @Test
    public void date_hasValueEquality() {
        FollowUpDate date = new FollowUpDate("20261025");
        assertEquals(date, date);
        assertEquals(date, new FollowUpDate("20261025"));
        assertEquals(date.hashCode(), new FollowUpDate("20261025").hashCode());
        assertFalse(date.equals(new FollowUpDate("20261026")));
        assertFalse(date.equals(null));
        assertFalse(date.equals("20261025"));
    }

    @Test
    public void metadata_affectsEqualityButNotDuplicateIdentity() {
        Person original = new PersonBuilder().build();
        assertEquals(EnquiryStatus.NEEDS_FOLLOW_UP, original.getEnquiryStatus());
        assertTrue(original.getFollowUpDate().isEmpty());
        Person status = new PersonBuilder(original).withEnquiryStatus("Completed").build();
        Person dated = new PersonBuilder(original).withFollowUpDate("20261025").build();
        assertFalse(original.equals(status));
        assertFalse(original.equals(dated));
        assertTrue(original.isSamePerson(status));
        assertTrue(original.isSamePerson(dated));
        assertEquals(dated, new PersonBuilder(dated).build());
        assertEquals(dated.hashCode(), new PersonBuilder(dated).build().hashCode());
    }
}
