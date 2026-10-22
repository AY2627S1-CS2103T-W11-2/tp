package seedu.boothmanagerpro.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import seedu.boothmanagerpro.model.person.EnquiryStatus;

public class ContactCardTest {

    @Test
    public void statusStyleClass_eachStatus_hasOwnStyleClass() {
        assertEquals("status-needs-follow-up", ContactCard.statusStyleClass(EnquiryStatus.NEEDS_FOLLOW_UP));
        assertEquals("status-in-progress", ContactCard.statusStyleClass(EnquiryStatus.IN_PROGRESS));
        assertEquals("status-completed", ContactCard.statusStyleClass(EnquiryStatus.COMPLETED));
    }
}
