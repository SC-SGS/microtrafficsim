package microtrafficsim.utils.strings;

import microtrafficsim.utils.strings.builder.BasicStringBuilder;
import microtrafficsim.utils.strings.builder.LevelStringBuilder;
import microtrafficsim.utils.strings.builder.StringBuilder;

/**
 * This class serves several methods for working with strings.
 *
 * @author Dominic Parga Cacheiro
 */
public class StringUtils {

    /**
     * <p>
     * Converts the given {@code number} to a {@code String} with an empty prefix to have the specified number of {@code
     * digits}.
     *
     * <p>
     * Example: <br>
     * {@code number = 4, digits = 2} returns {@code " 4"} <br>
     * {@code number = 2, digits = 3} returns {@code "  2"} <br>
     * {@code number = 42, digits = 3} returns {@code " 42"} <br>
     * {@code number = 42, digits = 1} returns {@code "2"} <br>
     *
     * @param number This number should be returned as {@code String}
     * @param digits The {@code String} will contain this number of chars.
     * @return A {@code String} having the specified number of {@code digits} and representing the given number.
     */
    public static String toString(int number, int digits) {

        LevelStringBuilder builder = new LevelStringBuilder();
        builder.setLevelSeparator(null);
        builder.setLevelSubString(" ");
        builder.setLevel(digits);
        builder.append(number);

        if (builder.length() > digits)
            builder.replace(0, builder.length() - digits, "");

        return builder.toString();
    }

    public static StringBuilder buildTimeString(String label, long time, String unit) {
        return buildTimeString(new BasicStringBuilder(), label, time, unit);
    }

    public static StringBuilder buildTimeString(StringBuilder builder, String label, long time, String unit) {
        // Long.MAX_VALUE < 10^20
        // => log10(Long.MAX_VALUE) / 3 < 7
        int[] bla = new int[] {-1, -1, -1, -1, -1, -1, -1};
        int c     = 0;
        while (time != 0) {
            bla[c++] = (int) (time % 1000);
            time     = time / 1000;
        }

        // build string
        c--;
        boolean       first   = true;
        builder.append(label);
        while (c >= 0) {
            int i = bla[c];
            if (i != -1) {
                if (first) {
                    first = false;
                    builder.append(i);
                } else {
                    if (i < 10)
                        builder.append("00").append(i);
                    else if (i < 100)
                        builder.append("0").append(i);
                    else
                        builder.append(i);
                }

                if (c != 0)
                    builder.append(" ");
                else
                    builder.append(unit);
            }
            c--;
        }
        return builder;
    }

    public static StringBuilder arrayToLines(String[] lines) {
        return arrayToLines(new BasicStringBuilder(), lines);
    }

    public static StringBuilder arrayToLines(StringBuilder builder, String[] lines) {
        for (String line : lines)
            builder.appendln(line);
        return builder;
    }
}
