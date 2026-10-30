package seedu.boothmanagerpro.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ArgumentTokenizerTest {

    private final Prefix unknownPrefix = new Prefix("--u");
    private final Prefix pSlash = new Prefix("p/");
    private final Prefix dashT = new Prefix("-t");
    private final Prefix hatQ = new Prefix("^Q");

    @Test
    public void tokenize_unicodeFieldSeparators_keepsFieldsSeparate() {
        Prefix company = new Prefix("c/");
        Prefix email = new Prefix("e/");
        for (String space : new String[]{"\u2003", "\u3000"}) {
            String input = "c/Acme" + space + "e/alice@example.com";
            ArgumentMultimap result = ArgumentTokenizer.tokenize(input, company, email);
            assertEquals("Acme", result.getValue(company).orElseThrow());
            assertEquals("alice@example.com", result.getValue(email).orElseThrow());
            assertEquals(2, ArgumentTokenizer.findSingleLetterPrefixes(input).size());
        }
    }

    @Test
    public void tokenize_unicodePatronymicSpacing_doesNotCreateStatusField() {
        String input = "n/Raj\u2003s/o\u2003Kumar e/raj@example.com";
        assertEquals(java.util.List.of("n/", "e/"), ArgumentTokenizer.findSingleLetterPrefixes(input).stream()
                .map(java.util.regex.MatchResult::group).toList());
    }

    @Test
    public void tokenize_emptyArgsString_noValues() {
        String argsString = "  ";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash);

        assertPreambleEmpty(argMultimap);
        assertArgumentAbsent(argMultimap, pSlash);
    }

    private void assertPreamblePresent(ArgumentMultimap argMultimap, String expectedPreamble) {
        assertEquals(expectedPreamble, argMultimap.getPreamble());
    }

    private void assertPreambleEmpty(ArgumentMultimap argMultimap) {
        assertTrue(argMultimap.getPreamble().isEmpty());
    }

    /**
     * Asserts all the arguments in {@code argMultimap} with {@code prefix} match the {@code expectedValues}
     * and only the last value is returned upon calling {@code ArgumentMultimap#getValue(Prefix)}.
     */
    private void assertArgumentPresent(ArgumentMultimap argMultimap, Prefix prefix, String... expectedValues) {

        // Verify the last value is returned
        assertEquals(expectedValues[expectedValues.length - 1], argMultimap.getValue(prefix).get());

        // Verify the number of values returned is as expected
        assertEquals(expectedValues.length, argMultimap.getAllValues(prefix).size());

        // Verify all values returned are as expected and in order
        for (int i = 0; i < expectedValues.length; i++) {
            assertEquals(expectedValues[i], argMultimap.getAllValues(prefix).get(i));
        }
    }

    private void assertArgumentAbsent(ArgumentMultimap argMultimap, Prefix prefix) {
        assertFalse(argMultimap.getValue(prefix).isPresent());
    }

    @Test
    public void tokenize_noPrefixes_allTakenAsPreamble() {
        String argsString = "  some random string /t tag with leading and trailing spaces ";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString);

        // Same string expected as preamble, but leading/trailing spaces should be trimmed
        assertPreamblePresent(argMultimap, argsString.trim());

    }

    @Test
    public void tokenize_oneArgument() {
        // Preamble present
        String argsString = "  Some preamble string p/ Argument value ";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash);
        assertPreamblePresent(argMultimap, "Some preamble string");
        assertArgumentPresent(argMultimap, pSlash, "Argument value");

        // No preamble
        argsString = " p/   Argument value ";
        argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash);
        assertPreambleEmpty(argMultimap);
        assertArgumentPresent(argMultimap, pSlash, "Argument value");

    }

    @Test
    public void tokenize_prefixAtStartAndAfterWhitespace_recognizesFields() {
        ArgumentMultimap result = ArgumentTokenizer.tokenize("p/first\t-t second\np/third", pSlash, dashT);
        assertPreambleEmpty(result);
        assertArgumentPresent(result, pSlash, "first", "third");
        assertArgumentPresent(result, dashT, "second");
    }

    @Test
    public void tokenize_multipleArguments() {
        // Only two arguments are present
        String argsString = "SomePreambleString -t dashT-Value p/pSlash value";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertPreamblePresent(argMultimap, "SomePreambleString");
        assertArgumentPresent(argMultimap, pSlash, "pSlash value");
        assertArgumentPresent(argMultimap, dashT, "dashT-Value");
        assertArgumentAbsent(argMultimap, hatQ);

        // All three arguments are present
        argsString = "Different Preamble String ^Q111 -t dashT-Value p/pSlash value";
        argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertPreamblePresent(argMultimap, "Different Preamble String");
        assertArgumentPresent(argMultimap, pSlash, "pSlash value");
        assertArgumentPresent(argMultimap, dashT, "dashT-Value");
        assertArgumentPresent(argMultimap, hatQ, "111");

        /* Also covers: Reusing of the tokenizer multiple times */

        // Reuse tokenizer on an empty string to ensure ArgumentMultimap is correctly reset
        // (i.e. no stale values from the previous tokenizing remain)
        argsString = "";
        argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertPreambleEmpty(argMultimap);
        assertArgumentAbsent(argMultimap, pSlash);

        /* Also covers: testing for prefixes not specified as a prefix */

        // Prefixes not previously given to the tokenizer should not return any values
        argsString = unknownPrefix + "some value";
        argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertArgumentAbsent(argMultimap, unknownPrefix);
        assertPreamblePresent(argMultimap, argsString); // Unknown prefix is taken as part of preamble
    }

    @Test
    public void tokenize_multipleArgumentsWithRepeats() {
        // Two arguments repeated, some have empty values
        String argsString = "SomePreambleString -t dashT-Value ^Q ^Q -t another dashT value p/ pSlash value -t";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertPreamblePresent(argMultimap, "SomePreambleString");
        assertArgumentPresent(argMultimap, pSlash, "pSlash value");
        assertArgumentPresent(argMultimap, dashT, "dashT-Value", "another dashT value", "");
        assertArgumentPresent(argMultimap, hatQ, "", "");
    }

    @Test
    public void tokenize_multipleArgumentsJoined() {
        String argsString = "SomePreambleStringp/ pSlash joined-tjoined -t not joined^Qjoined";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertPreamblePresent(argMultimap, "SomePreambleStringp/ pSlash joined-tjoined");
        assertArgumentAbsent(argMultimap, pSlash);
        assertArgumentPresent(argMultimap, dashT, "not joined^Qjoined");
        assertArgumentAbsent(argMultimap, hatQ);
    }

    @Test
    public void isPatronymicToken_variousContexts_detectsOnlyPatronymicBetweenNameWords() {
        assertTrue(ArgumentTokenizer.isPatronymicToken(" n/Raj s/o Kumar c/Acme", 7));
        assertTrue(ArgumentTokenizer.isPatronymicToken("Priya D/O Raman s/Completed", 6));
        assertFalse(ArgumentTokenizer.isPatronymicToken("s/o Kumar", 0));
        assertFalse(ArgumentTokenizer.isPatronymicToken("   s/o Kumar", 3));
        assertFalse(ArgumentTokenizer.isPatronymicToken(" n/ s/o Kumar", 4));
        assertFalse(ArgumentTokenizer.isPatronymicToken("1 s/o Kumar", 2));
        assertFalse(ArgumentTokenizer.isPatronymicToken("n/Raj s/o s/o Kumar", 10));
        assertFalse(ArgumentTokenizer.isPatronymicToken("n/Raj s/o", 6));
        assertFalse(ArgumentTokenizer.isPatronymicToken("n/Raj s/o   ", 6));
        assertFalse(ArgumentTokenizer.isPatronymicToken("n/Raj s/o c/Acme", 6));
        assertFalse(ArgumentTokenizer.isPatronymicToken("n/Raj s/o 123", 6));
        assertFalse(ArgumentTokenizer.isPatronymicToken("n/Raj s/other", 6));

        Prefix nSlash = CliSyntax.PREFIX_NAME;
        Prefix cSlash = CliSyntax.PREFIX_COMPANY;
        Prefix eSlash = CliSyntax.PREFIX_EMAIL;
        Prefix tSlash = CliSyntax.PREFIX_TAG;

        ArgumentMultimap tradingAs = ArgumentTokenizer.tokenize(
                " n/Alicia c/Acme Pte Ltd t/a Acme Events t/vip e/a@b.com",
                nSlash, cSlash, eSlash, tSlash);
        assertArgumentPresent(tradingAs, cSlash, "Acme Pte Ltd t/a Acme Events");
        assertArgumentPresent(tradingAs, tSlash, "vip");
        assertArgumentPresent(tradingAs, eSlash, "a@b.com");

        ArgumentMultimap careOf = ArgumentTokenizer.tokenize(
                "c/Holdings c/o Legal Dept e/legal@b.com", cSlash, eSlash);
        assertArgumentPresent(careOf, cSlash, "Holdings c/o Legal Dept");
        assertArgumentPresent(careOf, eSlash, "legal@b.com");

        ArgumentMultimap careOfAtStart = ArgumentTokenizer.tokenize("c/ c/o Legal Dept", cSlash);
        assertArgumentPresent(careOfAtStart, cSlash, "c/o Legal Dept");

        ArgumentMultimap initialisms = ArgumentTokenizer.tokenize(
                "c/ M/s. Lim & Co S/B d/b/a I/O Labs e/lim@b.com", cSlash, eSlash);
        assertArgumentPresent(initialisms, cSlash, "M/s. Lim & Co S/B d/b/a I/O Labs");
        assertArgumentPresent(initialisms, eSlash, "lim@b.com");

        // Unrequested single-letter prefixes are ignored by tokenize when not listed in prefixes
        ArgumentMultimap partialPrefixes = ArgumentTokenizer.tokenize("x/ignored p/91234567", pSlash);
        assertPreamblePresent(partialPrefixes, "x/ignored");
        assertArgumentPresent(partialPrefixes, pSlash, "91234567");

        // Non-abbreviation cases: blank company before t/a, trailing t/a, or t/a outside c/ remain prefixes
        assertFalse(ArgumentTokenizer.isCompanySlashAbbreviation("t/a Events", 0, "", 0));
        assertFalse(ArgumentTokenizer.isCompanySlashAbbreviation("c/ t/a Events", 3, "c/", 2));
        assertFalse(ArgumentTokenizer.isCompanySlashAbbreviation("c/Acme t/a", 7, "c/", 2));
        assertFalse(ArgumentTokenizer.isCompanySlashAbbreviation("c/Acme t/a t/b", 7, "c/", 2));
        assertFalse(ArgumentTokenizer.isCompanySlashAbbreviation("c/Acme t/a 123", 7, "c/", 2));
        assertFalse(ArgumentTokenizer.isCompanySlashAbbreviation("p/91234567 t/a vip", 11, "p/", 2));
    }

    @Test
    public void equalsMethod() {
        Prefix aaa = new Prefix("aaa");

        assertEquals(aaa, aaa);
        assertEquals(aaa, new Prefix("aaa"));

        assertNotEquals(aaa, "aaa");
        assertNotEquals(aaa, new Prefix("aab"));
    }

}
