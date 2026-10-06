package seedu.boothmanagerpro.model.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.boothmanagerpro.testutil.Assert.assertThrows;

import java.util.LinkedHashSet;
import java.util.List;

import org.junit.jupiter.api.Test;

public class TagTest {

    @Test
    public void equality_trimmedDuplicatesCollapseButCaseIsPreserved() {
        Tag tag = new Tag(" high-priority ");
        Tag duplicate = new Tag("high-priority");
        assertTrue(tag.equals(tag));
        assertTrue(tag.equals(duplicate));
        assertEquals(tag.hashCode(), duplicate.hashCode());
        assertFalse(tag.equals(null));
        assertFalse(tag.equals("high-priority"));
        assertFalse(tag.equals(new Tag("High-priority")));
        assertEquals(2, new LinkedHashSet<>(List.of(tag, duplicate, new Tag("High-priority"))).size());
    }

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Tag(null));
    }

    @Test
    public void constructor_invalidTagName_throwsIllegalArgumentException() {
        String invalidTagName = "";
        assertThrows(IllegalArgumentException.class, () -> new Tag(invalidTagName));
    }

    @Test
    public void isValidTagName() {
        // null tag name
        assertThrows(NullPointerException.class, () -> Tag.isValidTagName(null));
        assertTrue(Tag.isValidTagName("high-priority"));
        assertTrue(Tag.isValidTagName("A".repeat(30)));
        assertFalse(Tag.isValidTagName("A".repeat(31)));
        assertFalse(Tag.isValidTagName(" "));
        assertFalse(Tag.isValidTagName("industry/technology"));
        assertFalse(Tag.isValidTagName("two\nlines"));
    }

}
