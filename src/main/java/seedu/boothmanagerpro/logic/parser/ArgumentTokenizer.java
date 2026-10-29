package seedu.boothmanagerpro.logic.parser;

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
public class ArgumentTokenizer {
    private static final Pattern SINGLE_LETTER_PREFIX = Pattern.compile("(?<!\\S)([a-zA-Z]/)");

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
     * excluding legal-name patronymic markers ({@code s/o} and {@code d/o}) appearing between words.
     */
    static List<MatchResult> findSingleLetterPrefixes(String argsString) {
        return SINGLE_LETTER_PREFIX.matcher(argsString).results()
                .filter(match -> !isPatronymicToken(argsString, match.start()))
                .toList();
    }

    /**
     * Returns true if the token starting at {@code start} is a legal-name patronymic marker
     * ({@code s/o} or {@code d/o}, case-insensitive) appearing between two words containing letters,
     * rather than a command field prefix.
     */
    static boolean isPatronymicToken(String args, int start) {
        if (start + 3 >= args.length()) {
            return false;
        }
        char first = Character.toLowerCase(args.charAt(start));
        if ((first != 's' && first != 'd')
                || args.charAt(start + 1) != '/'
                || Character.toLowerCase(args.charAt(start + 2)) != 'o'
                || !Character.isWhitespace(args.charAt(start + 3))) {
            return false;
        }

        int prevEnd = start - 1;
        while (prevEnd >= 0 && Character.isWhitespace(args.charAt(prevEnd))) {
            prevEnd--;
        }
        if (prevEnd < 0) {
            return false;
        }
        int prevStart = prevEnd;
        while (prevStart > 0 && !Character.isWhitespace(args.charAt(prevStart - 1))) {
            prevStart--;
        }
        String prevToken = args.substring(prevStart, prevEnd + 1);
        if (prevToken.equalsIgnoreCase("s/o") || prevToken.equalsIgnoreCase("d/o")) {
            return false;
        }
        String prevWord = prevToken.length() >= 2
                && isAsciiLetter(prevToken.charAt(0)) && prevToken.charAt(1) == '/'
                ? prevToken.substring(2)
                : prevToken;
        if (prevWord.isEmpty() || prevWord.contains("/")
                || prevWord.codePoints().noneMatch(Character::isLetter)) {
            return false;
        }

        int nextStart = start + 3;
        while (nextStart < args.length() && Character.isWhitespace(args.charAt(nextStart))) {
            nextStart++;
        }
        if (nextStart >= args.length()) {
            return false;
        }
        int nextEnd = nextStart;
        while (nextEnd < args.length() && !Character.isWhitespace(args.charAt(nextEnd))) {
            nextEnd++;
        }
        String nextWord = args.substring(nextStart, nextEnd);
        return !nextWord.contains("/") && nextWord.codePoints().anyMatch(Character::isLetter);
    }

    private static boolean isAsciiLetter(char ch) {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z');
    }

    /**
     * Finds all zero-based prefix positions in the given arguments string.
     *
     * @param argsString Arguments string of the form: {@code preamble <prefix>value <prefix>value ...}
     * @param prefixes   Prefixes to find in the arguments string
     * @return           List of zero-based prefix positions in the given arguments string
     */
    private static List<PrefixPosition> findAllPrefixPositions(String argsString, Prefix... prefixes) {
        return Arrays.stream(prefixes)
                .flatMap(prefix -> findPrefixPositions(argsString, prefix).stream())
                .collect(Collectors.toList());
    }

    /**
     * @see #findAllPrefixPositions(String, Prefix...)
     */
    private static List<PrefixPosition> findPrefixPositions(String argsString, Prefix prefix) {
        return Pattern.compile("(?<!\\S)" + Pattern.quote(prefix.getPrefix())).matcher(argsString).results()
                .filter(match -> !isPatronymicToken(argsString, match.start()))
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
