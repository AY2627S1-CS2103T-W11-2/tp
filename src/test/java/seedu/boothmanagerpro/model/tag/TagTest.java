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
    public void equality_caseInsensitiveAndInternalSpacesCollapsedWhileDisplayCasePreserved() {
        Tag tag = new Tag(" high-priority ");
        Tag duplicate = new Tag("high-priority");
        Tag capitalised = new Tag("High-priority");
        assertEquals("High-priority", capitalised.tagName);
        assertTrue(tag.equals(tag));
        assertTrue(tag.equals(duplicate));
        assertTrue(tag.equals(capitalised));
        assertEquals(tag.hashCode(), duplicate.hashCode());
        assertEquals(tag.hashCode(), capitalised.hashCode());
        assertFalse(tag.equals(null));
        assertFalse(tag.equals("high-priority"));
        assertFalse(tag.equals(new Tag("low-priority")));
        assertEquals(1, new LinkedHashSet<>(List.of(tag, duplicate, capitalised)).size());

        Tag multiSpaced = new Tag("  High   \t  Priority  ");
        assertEquals("High Priority", multiSpaced.tagName);
        assertEquals(new Tag("high priority"), multiSpaced);
        assertEquals(new Tag("high priority").hashCode(), multiSpaced.hashCode());
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
        assertFalse(Tag.isValidTagName("A".repeat(1000))); // thousands of characters long
        assertFalse(Tag.isValidTagName(" "));
        assertFalse(Tag.isValidTagName("industry/technology"));
        assertFalse(Tag.isValidTagName("two\nlines"));
        assertFalse(Tag.isValidTagName("two\rlines"));

        // different languages and special characters
        assertTrue(Tag.isValidTagName("赞助商")); // Chinese
        assertTrue(Tag.isValidTagName("出展者")); // Japanese
        assertTrue(Tag.isValidTagName("priorité-haute")); // French with diacritic
        assertTrue(Tag.isValidTagName("Hauptsponsor")); // German
        assertTrue(Tag.isValidTagName("vip_client#1")); // underscore and hash
        assertTrue(Tag.isValidTagName("测".repeat(30))); // 30 multi-byte code points
        assertFalse(Tag.isValidTagName("测".repeat(31))); // 31 multi-byte code points
        assertFalse(Tag.isValidTagName("\u2003")); // Unicode em space only
        assertTrue(Tag.isValidTagName("e\u0301".repeat(30))); // 30 NFC characters from decomposed input
    }

    @Test
    public void constructor_unicodeWhitespaceAndNfd_normalisesToTrimmedNfc() {
        Tag emSpaced = new Tag("\u2003vip\u2003");
        assertEquals("vip", emSpaced.tagName);
        assertEquals(new Tag("vip"), emSpaced);

        Tag nfcTag = new Tag("Caf\u00E9");
        Tag nfdTag = new Tag("Cafe\u0301");
        assertEquals("Caf\u00E9", nfdTag.tagName);
        assertEquals(nfcTag, nfdTag);
        assertEquals(nfcTag.hashCode(), nfdTag.hashCode());
    }

}
