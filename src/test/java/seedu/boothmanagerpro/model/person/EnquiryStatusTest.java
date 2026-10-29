package seedu.boothmanagerpro.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.boothmanagerpro.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class EnquiryStatusTest {

    @Test
    public void fromString_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> EnquiryStatus.fromString(null));
    }

    @Test
    public void fromString_invalidValue_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                EnquiryStatus.MESSAGE_CONSTRAINTS, () -> EnquiryStatus.fromString(""));
        assertThrows(IllegalArgumentException.class,
                EnquiryStatus.MESSAGE_CONSTRAINTS, () -> EnquiryStatus.fromString("   "));
        assertThrows(IllegalArgumentException.class,
                EnquiryStatus.MESSAGE_CONSTRAINTS, () -> EnquiryStatus.fromString("Pending"));
        assertThrows(IllegalArgumentException.class,
                EnquiryStatus.MESSAGE_CONSTRAINTS, () -> EnquiryStatus.fromString("NEEDS_FOLLOW_UP"));
    }

    @Test
    public void fromString_validValuesCaseInsensitiveAndTrimmed_success() {
        assertEquals(EnquiryStatus.NEEDS_FOLLOW_UP, EnquiryStatus.fromString("needs follow-up"));
        assertEquals(EnquiryStatus.NEEDS_FOLLOW_UP, EnquiryStatus.fromString("  Needs Follow-Up  "));
        assertEquals(EnquiryStatus.IN_PROGRESS, EnquiryStatus.fromString("in progress"));
        assertEquals(EnquiryStatus.COMPLETED, EnquiryStatus.fromString("COMPLETED"));
        assertEquals(EnquiryStatus.CANCELLED, EnquiryStatus.fromString("  cAnCeLlEd "));
    }

    @Test
    public void toStringMethod() {
        assertEquals("Needs follow-up", EnquiryStatus.NEEDS_FOLLOW_UP.toString());
        assertEquals("In progress", EnquiryStatus.IN_PROGRESS.toString());
        assertEquals("Completed", EnquiryStatus.COMPLETED.toString());
        assertEquals("Cancelled", EnquiryStatus.CANCELLED.toString());
    }
}

