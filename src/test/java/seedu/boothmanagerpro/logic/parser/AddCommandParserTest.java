package seedu.boothmanagerpro.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.boothmanagerpro.logic.parser.CommandParserTestUtil.assertParseFailure;

import org.junit.jupiter.api.Test;

import seedu.boothmanagerpro.model.ModelManager;
import seedu.boothmanagerpro.model.person.Company;
import seedu.boothmanagerpro.model.person.ContactMethod;
import seedu.boothmanagerpro.model.person.Email;
import seedu.boothmanagerpro.model.person.Name;
import seedu.boothmanagerpro.model.person.Person;
import seedu.boothmanagerpro.model.person.Phone;
import seedu.boothmanagerpro.model.tag.Tag;

public class AddCommandParserTest {
    private final AddCommandParser parser = new AddCommandParser();
    private final String valid = " n/Alicia Tan c/TechNova Pte Ltd e/alicia@technova.com p/91234567";

    @Test
    public void parse_example_normalisesAndFormatsContact() throws Exception {
        ModelManager model = new ModelManager();
        String result = parser.parse(valid.replace("alicia@technova.com", "ALICIA@TechNova.COM")
                .replace("91234567", "+65 9123-4567")
                + " m/EMAIL t/technology t/high-priority t/technology").execute(model).getFeedbackToUser();
        assertEquals("New exhibitor contact added:\nAlicia Tan at TechNova Pte Ltd\n"
                + "Email: alicia@technova.com\nPhone: +6591234567\nContact method: email\n"
                + "Tags: technology, high-priority", result);
        Person person = model.getFilteredPersonList().getFirst();
        assertEquals("TechNova Pte Ltd", person.getCompany().orElseThrow().value);
        assertEquals(ContactMethod.EMAIL, person.getContactMethod().orElseThrow());
        assertEquals(2, person.getTags().size());
    }

    @Test
    public void parse_optionalFieldsAbsentAndAnyOrder_success() throws Exception {
        ModelManager model = new ModelManager();
        parser.parse("p/91234567\te/alicia@technova.com c/ TechNova Pte Ltd   n/ Alicia Tan ").execute(model);
        Person person = model.getFilteredPersonList().getFirst();
        assertEquals("Alicia Tan", person.getName().fullName);
        assertEquals("TechNova Pte Ltd", person.getCompany().orElseThrow().value);
        assertTrue(person.getContactMethod().isEmpty());
        assertTrue(person.getTags().isEmpty());
    }

    @Test
    public void parse_requiredFieldsMissing_failure() {
        assertParseFailure(parser, "", AddCommandParser.MESSAGE_MISSING_FIELD);
        for (String field : new String[]{" n/Alicia Tan", " c/TechNova Pte Ltd", " e/alicia@technova.com",
            " p/91234567"}) {
            assertParseFailure(parser, valid.replace(field, ""), AddCommandParser.MESSAGE_MISSING_FIELD);
        }
    }

    @Test
    public void parse_repeatedFields_failure() {
        for (String field : new String[]{" n/Other", " c/Other", " e/other@example.com", " p/98765432"}) {
            assertParseFailure(parser, valid + field, AddCommandParser.MESSAGE_REPEATED_FIELD);
        }
        assertParseFailure(parser, valid + " m/email m/phone", AddCommandParser.MESSAGE_REPEATED_FIELD);
    }

    @Test
    public void parse_unknownPrefixOrPreamble_failure() {
        for (String field : new String[]{" a/Old address", " x/value", " company/Other", " m/phone x/invalid"}) {
            assertParseFailure(parser, valid + field, AddCommandParser.MESSAGE_UNKNOWN_FIELD);
        }
        assertParseFailure(parser, "unprefixed" + valid, AddCommandParser.MESSAGE_UNKNOWN_FIELD);
        assertParseFailure(parser, "Alicia Tan", AddCommandParser.MESSAGE_UNKNOWN_FIELD);
    }

    @Test
    public void parse_invalidFields_failure() {
        for (String name : new String[]{"", "123", "A1", "---", "A".repeat(81)}) {
            assertParseFailure(parser, valid.replace("Alicia Tan", name), Name.MESSAGE_CONSTRAINTS);
        }
        for (String company : new String[]{"", "A".repeat(101)}) {
            assertParseFailure(parser, valid.replace("TechNova Pte Ltd", company), Company.MESSAGE_CONSTRAINTS);
        }
        for (String phone : new String[]{"123456", "1".repeat(16), "1234567+", "abc1234567"}) {
            assertParseFailure(parser, valid.replace("91234567", phone), Phone.MESSAGE_CONSTRAINTS);
        }
        assertParseFailure(parser, valid.replace("alicia@technova.com", "a@@example.com"), Email.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, valid + " m/", ContactMethod.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, valid + " m/fax", ContactMethod.MESSAGE_CONSTRAINTS);
        for (String tag : new String[]{"", "a/b", "a".repeat(31)}) {
            assertParseFailure(parser, valid + " t/" + tag, Tag.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_boundaryValues_success() throws Exception {
        for (String method : new String[]{"email", "PHONE", "Other"}) {
            ModelManager model = new ModelManager();
            parser.parse(" n/" + "A".repeat(80) + " c/" + "C".repeat(100)
                    + " e/a@example.com p/+123456789012345 m/" + method + " t/" + "t".repeat(30)).execute(model);
            assertEquals(1, model.getFilteredPersonList().size());
        }
        parser.parse(valid.replace("Alicia Tan", "Anne-Marie O'Neil.").replace("91234567", "1234567"));
    }
}
