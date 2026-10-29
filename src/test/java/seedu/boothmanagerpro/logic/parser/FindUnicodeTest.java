package seedu.boothmanagerpro.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.boothmanagerpro.model.Model;
import seedu.boothmanagerpro.model.ModelManager;
import seedu.boothmanagerpro.model.person.Person;
import seedu.boothmanagerpro.testutil.PersonBuilder;

/** Checks exact-name searches against the Unicode case rules used by keyword search. */
public class FindUnicodeTest {
    @Test
    public void find_turkishName_caseInsensitiveQueriesShouldAgree() throws Exception {
        assertSearchesAgree("\u0130pek", "ipek");
        assertSearchesAgree("ipek", "\u0130pek");
        assertSearchesAgree("Ipek", "\u0131pek");
    }

    @Test
    public void find_greekName_caseInsensitiveQueriesShouldAgree() throws Exception {
        assertSearchesAgree("\u039f\u03a3", "\u03bf\u03c3");
        assertSearchesAgree("\u03bf\u03c3", "\u039f\u03a3");
        assertSearchesAgree("\u03bf\u03c2", "\u03bf\u03c3");
    }

    @Test
    public void find_supplementaryLetters_caseInsensitiveQueriesShouldAgree() throws Exception {
        assertSearchesAgree("\ud801\udc00", "\ud801\udc28");
        assertSearchesAgree("\ud801\udc28", "\ud801\udc00");
    }

    @Test
    public void find_unicodeName_matchesWholeWordsAndKeepsFieldCombinations() throws Exception {
        Person person = new PersonBuilder().withName("\u0130pek Tan").withTags("vip").build();
        Model model = new ModelManager();
        model.addPerson(person);
        FindCommandParser parser = new FindCommandParser();
        parser.parse("n/ipek").execute(model);
        assertEquals(List.of(person), List.copyOf(model.getFilteredPersonList()));
        parser.parse("n/ipe").execute(model);
        assertEquals(List.of(), List.copyOf(model.getFilteredPersonList()));
        parser.parse("n/Nobody n/ipek tan t/VIP").execute(model);
        assertEquals(List.of(person), List.copyOf(model.getFilteredPersonList()));
        parser.parse("n/ipek tan t/other").execute(model);
        assertEquals(List.of(), List.copyOf(model.getFilteredPersonList()));
    }

    private void assertSearchesAgree(String storedName, String query) throws Exception {
        Person person = new PersonBuilder().withName(storedName).build();
        Model model = new ModelManager();
        model.addPerson(person);
        FindCommandParser parser = new FindCommandParser();
        parser.parse(query).execute(model);
        assertEquals(List.of(person), List.copyOf(model.getFilteredPersonList()), "Legacy query matches");
        parser.parse("n/" + query).execute(model);
        assertEquals(List.of(person), List.copyOf(model.getFilteredPersonList()), "Exact name query matches");
    }
}
