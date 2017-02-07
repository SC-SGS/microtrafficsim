package microtrafficsim.utils.strings;

/**
 * This class serves several methods for working with strings.
 *
 * @author Dominic Parga Cacheiro
 */
public class StringUtils {

    public static StringBuilder buildTimeString(String label, long time, String unit) {
        return buildTimeString(new StringBuilder(), label, time, unit);
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
        return arrayToLines(new StringBuilder(), lines);
    }

    public static StringBuilder arrayToLines(StringBuilder builder, String[] lines) {
        for (String line : lines)
            builder.append(line).append("\n");
        return builder;
    }
}
