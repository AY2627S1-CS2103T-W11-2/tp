package seedu.boothmanagerpro.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.boothmanagerpro.logic.parser.CommandParserTestUtil.assertParseFailure;

import org.junit.jupiter.api.Test;

import seedu.boothmanagerpro.model.ModelManager;
import seedu.boothmanagerpro.model.person.Company;
import seedu.boothmanagerpro.model.person.ContactMethod;
import seedu.boothmanagerpro.model.person.Email;
import seedu.boothmanagerpro.model.person.EnquiryStatus;
import seedu.boothmanagerpro.model.person.FollowUpDate;
import seedu.boothmanagerpro.model.person.Name;
import seedu.boothmanagerpro.model.person.Person;
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
        assertEquals("Added Alicia Tan at TechNova Pte Ltd.", result);
        Person person = model.getFilteredPersonList().getFirst();
        assertEquals("TechNova Pte Ltd", person.getCompany().orElseThrow().value);
        assertEquals(ContactMethod.EMAIL, person.getContactMethod().orElseThrow());
        assertEquals("alicia@technova.com", person.getEmail().value);
        assertEquals("+6591234567", person.getPhone().value);
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
    public void parse_requiredFieldsMissing_listsOnlyMissingPrefixes() {
        assertParseFailure(parser, "", String.format(AddCommandParser.MESSAGE_MISSING_FIELD, "n/, c/, e/, p/"));
        assertParseFailure(parser, " n/Alicia Tan p/91234567",
                String.format(AddCommandParser.MESSAGE_MISSING_FIELD, "c/, e/"));
        for (String field : new String[]{" n/Alicia Tan", " c/TechNova Pte Ltd", " e/alicia@technova.com",
            " p/91234567"}) {
            assertParseFailure(parser, valid.replace(field, ""),
                    String.format(AddCommandParser.MESSAGE_MISSING_FIELD, field.trim().substring(0, 2)));
        }
    }

    @Test
    public void parse_repeatedFields_listsAllRepeatedPrefixes() {
        for (String field : new String[]{" n/Other", " c/Other", " e/other@example.com", " p/98765432"}) {
            assertParseFailure(parser, valid + field,
                    String.format(AddCommandParser.MESSAGE_REPEATED_FIELD, field.trim().substring(0, 2)));
        }
        assertParseFailure(parser, valid + " m/email m/phone",
                String.format(AddCommandParser.MESSAGE_REPEATED_FIELD, "m/"));
        assertParseFailure(parser, "n/A n/B c/C c/D",
                String.format(AddCommandParser.MESSAGE_REPEATED_FIELD, "n/, c/"));
    }

    @Test
    public void parse_unknownPrefixOrPreamble_identifiesProblem() {
        for (String prefix : new String[]{"a/", "x/"}) {
            assertParseFailure(parser, valid + " " + prefix + "value",
                    String.format(AddCommandParser.MESSAGE_UNKNOWN_FIELD, prefix));
        }
        assertParseFailure(parser, "n/A n/B x/value",
                String.format(AddCommandParser.MESSAGE_UNKNOWN_FIELD, "x/"));
        assertParseFailure(parser, "unprefixed" + valid, AddCommandParser.MESSAGE_PREAMBLE);
        assertParseFailure(parser, "Alicia Tan", AddCommandParser.MESSAGE_PREAMBLE);
    }

    @Test
    public void parse_emptyValues_identifiesField() {
        for (String field : new String[]{"n/Alicia Tan", "c/TechNova Pte Ltd", "e/alicia@technova.com", "p/91234567"}) {
            String prefix = field.substring(0, 2);
            assertParseFailure(parser, valid.replace(field, prefix),
                    "Empty field: " + prefix + ". Supply a value after this prefix.");
        }
        for (String prefix : new String[]{"m/", "s/", "f/", "t/"}) {
            assertParseFailure(parser, valid + " " + prefix,
                    "Empty field: " + prefix + ". Supply a value after this prefix.");
        }
    }

    @Test
    public void parse_invalidFields_failure() {
        for (String name : new String[]{"123", "A1", "---", "A".repeat(81)}) {
            assertParseFailure(parser, valid.replace("Alicia Tan", name),
                    "Invalid value for n/: " + Name.MESSAGE_CONSTRAINTS);
        }
        for (String company : new String[]{"A".repeat(101)}) {
            assertParseFailure(parser, valid.replace("TechNova Pte Ltd", company),
                    "Invalid value for c/: " + Company.MESSAGE_CONSTRAINTS);
        }
        for (String phone : new String[]{"123456", "1".repeat(16)}) {
            assertParseFailure(parser, valid.replace("91234567", phone),
                    "Invalid value for p/: Phone number must contain 7 to 15 digits, optionally starting with +.");
        }
        for (String phone : new String[]{"1234567+", "abc1234567", "(65) 6123-4567"}) {
            assertParseFailure(parser, valid.replace("91234567", phone), "Invalid value for p/: "
                    + "Phone number can only contain digits, spaces, hyphens, and a + at the start.");
        }
        assertParseFailure(parser, valid.replace("alicia@technova.com", "a@@example.com"),
                    "Invalid value for e/: " + Email.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, valid.replace("alicia@technova.com", "a@" + "a".repeat(1000) + "!"),
                "Invalid value for e/: " + Email.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, valid + " m/fax",
                    "Invalid value for m/: " + ContactMethod.MESSAGE_CONSTRAINTS);
        for (String tag : new String[]{"a/b", "a".repeat(31)}) {
            assertParseFailure(parser, valid + " t/" + tag,
                    "Invalid value for t/: " + Tag.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_invalidEnquiryFields_identifiesField() {
        assertParseFailure(parser, valid + " s/done",
                "Invalid value for s/: " + EnquiryStatus.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, valid + " f/20230229",
                "Invalid value for f/: " + FollowUpDate.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, valid + " s/Completed s/In progress f/20261025 f/20261026",
                String.format(AddCommandParser.MESSAGE_REPEATED_FIELD, "s/, f/"));
    }

    @Test
    public void parse_companyNameWithSlash_acceptedAsValue() throws Exception {
        // Regression test: slashes inside field values must not be mistaken for field prefix delimiters.
        // e.g. "Acme Design/ Build" should be stored as-is, not trigger an unknown-prefix error.
        ModelManager model = new ModelManager();
        parser.parse(" n/Slash Person c/Acme Design/ Build e/slash@example.com p/81234567").execute(model);
        assertEquals("Acme Design/ Build",
                model.getFilteredPersonList().getFirst().getCompany().orElseThrow().value);
        // Slash at the start of the value segment after c/ is also accepted.
        ModelManager model2 = new ModelManager();
        parser.parse(" n/Another Person c/A/B Corp e/ab@example.com p/91234567").execute(model2);
        assertEquals("A/B Corp",
                model2.getFilteredPersonList().getFirst().getCompany().orElseThrow().value);
    }

    @Test
    public void parse_patronymicLegalName_acceptedWithoutStatusCollision() throws Exception {
        ModelManager model = new ModelManager();
        String feedback = parser.parse(" n/Raj s/o Kumar c/Acme e/raj@example.com p/91234567")
                .execute(model).getFeedbackToUser();
        assertEquals("Added Raj s/o Kumar at Acme.", feedback);
        assertEquals("Raj s/o Kumar", model.getFilteredPersonList().getFirst().getName().fullName);

        ModelManager model2 = new ModelManager();
        parser.parse(" n/Priya d/o Raman c/Acme e/priya@example.com p/91234568 s/In progress").execute(model2);
        Person priya = model2.getFilteredPersonList().getFirst();
        assertEquals("Priya d/o Raman", priya.getName().fullName);
        assertEquals(EnquiryStatus.IN_PROGRESS, priya.getEnquiryStatus());

        assertParseFailure(parser, valid + " s/o",
                "Invalid value for s/: " + EnquiryStatus.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " n/Alice s/o c/Acme e/alice@example.com p/91234567",
                "Invalid value for s/: " + EnquiryStatus.MESSAGE_CONSTRAINTS);
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
