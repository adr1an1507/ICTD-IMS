/*
 * ICTD IMS - FILE TEXT
 *
 * Small helper for saving text in the data files.
 * Each line of a data file holds several values separated by "|".
 * If a value itself contains "|" or a new line, it is "escaped" first
 * so it cannot break the file format.
 *
 *   |   becomes   \p
 *   new line becomes   \n
 *   \   becomes   \\
 */
package backend.storage;

public final class FileText {

    private FileText() {
        // Only static methods, so nobody needs to create an object
    }

    /** Makes one value safe to write in a data file. */
    public static String escape(String text) {
        StringBuilder result = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c == '\\') {
                result.append("\\\\");
            } else if (c == '|') {
                result.append("\\p");
            } else if (c == '\n') {
                result.append("\\n");
            } else if (c != '\r') {
                result.append(c);
            }
        }
        return result.toString();
    }

    /** Turns an escaped value back into the original text. */
    public static String unescape(String text) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\\' && i + 1 < text.length()) {
                char next = text.charAt(++i);
                if (next == 'p') {
                    result.append('|');
                } else if (next == 'n') {
                    result.append('\n');
                } else {
                    result.append(next);
                }
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    /** Joins values into one line of the data file. */
    public static String join(String... values) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                line.append('|');
            }
            line.append(escape(values[i]));
        }
        return line.toString();
    }

    /** Splits one line of the data file back into its values. */
    public static String[] split(String line) {
        String[] parts = line.split("\\|", -1); // -1 keeps empty values
        for (int i = 0; i < parts.length; i++) {
            parts[i] = unescape(parts[i]);
        }
        return parts;
    }
}