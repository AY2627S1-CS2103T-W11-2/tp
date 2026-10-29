package seedu.boothmanagerpro.logic.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Tokenizes arguments string of the form: {@code preamble <prefix>value <prefix>value ...}<br>
 *     e.g. {@code some preamble text t/ 11.00 t/12.00 k/ m/ July}  where prefixes are {@code t/ k/ m/}.<br>
 * 1. An argument's value can be an empty string e.g. the value of {@code k/} in the above example.<br>
 * 2. Leading and trailing whitespaces of an argument value will be discarded.<br>
 * 3. An argument may be repeated and all its values will be accumulated e.g. the value of {@code t/}
 *    in the above example.<br>
 */
public final class ArgumentTokenizer {
    private static final String COMPANY_PREFIX = CliSyntax.PREFIX_COMPANY.getPrefix();
    private static final Pattern SINGLE_LETTER_PREFIX = Pattern.compile("(?<!\\S)([a-zA-Z]/)");
    private static final Pattern BEFORE_PATRONYMIC = Pattern.compile(
            "(?:^|.*\\s)(?!(?i:[sd]/o|a/[lp])\\s)(?:[a-zA-Z]/)?(?=[^\\s/]*\\p{L})[^\\s/]+\\s+",
            Pattern.DOTALL);
    private static final Pattern FROM_PATRONYMIC = Pattern.compile(
            "(?i:[sd]/o|a/[lp])\\s+(?=[^\\s/]*\\p{L})[^\\s/]+(?:\\s.*|$)",
            Pattern.DOTALL);
    private static final Pattern COMPANY_INITIALISM_OR_MESSRS = Pattern.compile(
            "(?:[A-Z]/[A-Z]|M/s)(?:\\s.*|[.,;:)].*|$)",
            Pattern.DOTALL);
    private static final String COMPANY_FOLLOWING_WORD =
            "(?:(?=[^\\s/]*\\p{L})[^\\s/]+|(?:[A-Z]/[A-Z]|M/s)(?=\\s|$|[.,;:)]))";
    private static final Pattern COMPANY_CARE_OF = Pattern.compile(
            "(?i:c/o)\\s+" + COMPANY_FOLLOWING_WORD + "(?:\\s.*|$)",
            Pattern.DOTALL);
    private static final Pattern COMPANY_INFIX_ABBREVIATION = Pattern.compile(
            "(?i:t/a|d/b/a)\\s+" + COMPANY_FOLLOWING_WORD + "(?:\\s.*|$)",
            Pattern.DOTALL);

    private ArgumentTokenizer() {}

    /**
     * Tokenizes an arguments string and returns an {@code ArgumentMultimap} object that maps prefixes to their
     * respective argument values. Only the given prefixes will be recognized, at the start of the string
     * or after whitespace.
     *
     * @param argsString Arguments string of the form: {@code preamble <prefix>value <prefix>value ...}
     * @param prefixes   Prefixes to tokenize the arguments string with
     * @return           ArgumentMultimap object that maps prefixes to their arguments
     */
    public static ArgumentMultimap tokenize(String argsString, Prefix... prefixes) {
        List<PrefixPosition> positions = findAllPrefixPositions(argsString, prefixes);
        return extractArguments(argsString, positions);
    }

    /**
     * Finds all whitespace-delimited single-letter prefix tokens (e.g. {@code n/}, {@code c/}),
     * excluding legal-name patronymic markers ({@code s/o}, {@code d/o}, {@code a/l}, {@code a/p}) between words
     * and common business slash abbreviations inside a {@code c/} company value.
     */
    public static List<MatchResult> findSingleLetterPrefixes(String argsString) {
        List<MatchResult> accepted = new ArrayList<>();
        String activePrefix = "";
        int activeValueStart = 0;
        for (MatchResult match : SINGLE_LETTER_PREFIX.matcher(argsString).results().toList()) {
            int start = match.start();
            if (isPatronymicToken(argsString, start)
                    || isCompanySlashAbbreviation(argsString, start, activePrefix, activeValueStart)) {
                continue;
            }
            accepted.add(match);
            activePrefix = match.group(1);
            activeValueStart = match.end();
        }
        return List.copyOf(accepted);
    }

    /**
     * Returns true if the token starting at {@code start} is a legal-name patronymic marker
     * ({@code s/o}, {@code d/o}, {@code a/l} or {@code a/p}, case-insensitive) between two words containing letters,
     * rather than a command field prefix.
     */
    static boolean isPatronymicToken(String args, int start) {
        return BEFORE_PATRONYMIC.matcher(args.substring(0, start)).matches()
                && FROM_PATRONYMIC.matcher(args.substring(start)).matches();
    }

    /**
     * Returns true if the token starting at {@code start} is a common business slash abbreviation
     * inside an active {@code c/} company field (such as {@code S/B}, {@code P/L}, {@code I/O},
     * {@code A/B}, {@code M/s}, {@code c/o}, {@code t/a}, or {@code d/b/a}), rather than a field prefix.
     */
    static boolean isCompanySlashAbbreviation(String args, int start, String activePrefix, int activeValueStart) {
        if (!COMPANY_PREFIX.equals(activePrefix)) {
            return false;
        }
        String rest = args.substring(start);
        if (COMPANY_INITIALISM_OR_MESSRS.matcher(rest).matches()
                || COMPANY_CARE_OF.matcher(rest).matches()) {
            return true;
        }
        return !args.substring(activeValueStart, start).isBlank()
                && COMPANY_INFIX_ABBREVIATION.matcher(rest).matches();
    }

    /**
     * Finds all zero-based prefix positions in the given arguments string.
     *
     * @param argsString Arguments string of the form: {@code preamble <prefix>value <prefix>value ...}
     * @param prefixes   Prefixes to find in the arguments string
     * @return           List of zero-based prefix positions in the given arguments string
     */
    private static List<PrefixPosition> findAllPrefixPositions(String argsString, Prefix... prefixes) {
        List<Prefix> prefixList = Arrays.asList(prefixes);
        List<PrefixPosition> positions = new ArrayList<>();
        for (MatchResult match : findSingleLetterPrefixes(argsString)) {
            Prefix candidate = new Prefix(match.group(1));
            if (prefixList.contains(candidate)) {
                positions.add(new PrefixPosition(candidate, match.start()));
            }
        }
        for (Prefix prefix : prefixes) {
            if (!SINGLE_LETTER_PREFIX.matcher(prefix.getPrefix()).matches()) {
                positions.addAll(findPrefixPositions(argsString, prefix));
            }
        }
        return positions;
    }

    /**
     * @see #findAllPrefixPositions(String, Prefix...)
     */
    private static List<PrefixPosition> findPrefixPositions(String argsString, Prefix prefix) {
        return Pattern.compile("(?<!\\S)" + Pattern.quote(prefix.getPrefix())).matcher(argsString).results()
                .map(match -> new PrefixPosition(prefix, match.start()))
                .collect(Collectors.toList());
    }

    /**
     * Extracts prefixes and their argument values, and returns an {@code ArgumentMultimap} object that maps the
     * extracted prefixes to their respective arguments. Prefixes are extracted based on their zero-based positions in
     * {@code argsString}.
     *
     * @param argsString      Arguments string of the form: {@code preamble <prefix>value <prefix>value ...}
     * @param prefixPositions Zero-based positions of all prefixes in {@code argsString}
     * @return                ArgumentMultimap object that maps prefixes to their arguments
     */
    private static ArgumentMultimap extractArguments(String argsString, List<PrefixPosition> prefixPositions) {

        // Sort by start position
        prefixPositions.sort((prefix1, prefix2) -> prefix1.getStartPosition() - prefix2.getStartPosition());

        // Insert a PrefixPosition to represent the preamble
        PrefixPosition preambleMarker = new PrefixPosition(new Prefix(""), 0);
        prefixPositions.addFirst(preambleMarker);

        // Add a dummy PrefixPosition to represent the end of the string
        PrefixPosition endPositionMarker = new PrefixPosition(new Prefix(""), argsString.length());
        prefixPositions.add(endPositionMarker);

        // Map prefixes to their argument values (if any)
        ArgumentMultimap argMultimap = new ArgumentMultimap();
        for (int i = 0; i < prefixPositions.size() - 1; i++) {
            // Extract and store prefixes and their arguments
            Prefix argPrefix = prefixPositions.get(i).getPrefix();
            String argValue = extractArgumentValue(argsString, prefixPositions.get(i), prefixPositions.get(i + 1));
            argMultimap.put(argPrefix, argValue);
        }

        return argMultimap;
    }

    /**
     * Returns the trimmed value of the argument in the arguments string specified by {@code currentPrefixPosition}.
     * The end position of the value is determined by {@code nextPrefixPosition}.
     */
    private static String extractArgumentValue(String argsString,
                                        PrefixPosition currentPrefixPosition,
                                        PrefixPosition nextPrefixPosition) {
        Prefix prefix = currentPrefixPosition.getPrefix();

        int valueStartPos = currentPrefixPosition.getStartPosition() + prefix.getPrefix().length();
        String value = argsString.substring(valueStartPos, nextPrefixPosition.getStartPosition());

        return value.strip();
    }

    /**
     * Represents a prefix's position in an arguments string.
     */
    private static class PrefixPosition {
        private int startPosition;
        private final Prefix prefix;

        PrefixPosition(Prefix prefix, int startPosition) {
            this.prefix = prefix;
            this.startPosition = startPosition;
        }

        int getStartPosition() {
            return startPosition;
        }

        Prefix getPrefix() {
            return prefix;
        }
    }

}
