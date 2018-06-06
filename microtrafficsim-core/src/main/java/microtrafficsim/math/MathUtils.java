package microtrafficsim.math;


import java.util.Iterator;

/**
 * Math utilities.
 */
public class MathUtils {
    private MathUtils() {}

    /**
     * Clamps the given value using the specified maximum and minimum.
     *
     * @param val the value to be clamped.
     * @param min the minimum to which the given value should be clamped.
     * @param max the maximum to which the given value should be clamped
     * @return the clamped value, i.e. {@code val} if {@code val} is between {@code min} and {@code max}, {@code max }
     * if {@code val} is greater than {@code max} or {@code min} if {@code val} is smaller than {@code min}.
     */
    public static double clamp(double val, double min, double max) {
        return val < min ? min : val > max ? max : val;
    }

    /**
     * Clamps the given value using the specified maximum and minimum.
     *
     * @param val the value to be clamped.
     * @param min the minimum to which the given value should be clamped.
     * @param max the maximum to which the given value should be clamped
     * @return the clamped value, i.e. {@code val} if {@code val} is between {@code min} and {@code max}, {@code max }
     * if {@code val} is greater than {@code max} or {@code min} if {@code val} is smaller than {@code min}.
     */
    public static int clamp(int val, int min, int max) {
        return val < min ? min : val > max ? max : val;
    }

    public static int max(int... ints) {
        int i = 1;
        while (i < ints.length)
            ints[0] = Math.max(ints[0], ints[i++]);
        return ints[0];
    }

    public static int min(int... ints) {
        int i = 1;
        while (i < ints.length)
            ints[0] = Math.min(ints[0], ints[i++]);
        return ints[0];
    }

    /**
     * Implementation detail: The runtime complexity is in O(log2(n)). <br>
     * Negative numbers has the same number of digits as the corresponding positive ones.
     *
     * @param n The given integer
     * @return The number of digits of the given integer n in O(log2(n))
     */
    public static int getDigitCountDecimal(int n) {

        /* overflow if n is negative */
        if (n == Integer.MIN_VALUE)
            return 10;


        /* make positive */
        if (n < 0)
            n *= -1;


        if (n < 100000)
            /* 1 <= digit count <= 5 */
            if (n < 100)
                /* 1 <= digit count <= 2 */
                return (n < 10) ? 1 : 2;
            else
                /* 3 <= digit count <= 5 */
                if (n < 1000)
                    return 3;
                else
                    /* 4 <= digit count <= 5 */
                    return (n < 10000) ? 4 : 5;
        else
            /* 6 <= digit count <= 10 */
            if (n < 10000000)
                /* 6 <= digit count <= 7 */
                return (n < 1000000) ? 6 : 7;
            else
                /* 8 <= digit count <= 10 */
                if (n < 100000000)
                    return 8;
                else
                    /* 9 <= digit count <= 10 */
                    return (n < 1000000000) ? 9 : 10;
    }

    /**
     * Returns an iterator providing a number sequence. This sequence starts with <i>{@code from}</i> (given as
     * parameter) and ends with <i>{@code to}</i> (given as parameter). In between, new values are calculated as
     * sigmoid function {@code e}<sup>{@code x}</sup>{@code / (1 + e} <sup>{@code x}</sup>{@code )}.
     *
     * @param from  First number in the sequence
     * @param to    Last number in the sequence
     * @param steps The length of the sequence (exclusive <i>{@code from}</i> and <i>{@code to}</i>)
     *
     * @throws IllegalArgumentException if <i>{@code from}</i> {@code >=} <i>{@code to}</i>
     * @throws IllegalArgumentException if <i>{@code from}</i> {@code <} <i>{@code 0}</i>
     * @throws IllegalArgumentException if <i>{@code steps}</i> {@code <} <i>{@code 0}</i>
     *
     * @return an iterator providing a number sequence calculated as sigmoid function.
     * {@link Iterator#hasNext() hasNext()} returns false, if the sequence has finished, but
     * {@link Iterator#next() next()} will return <i>{@code to}</i>
     */
    public static Iterator<Integer> createSigmoidSequence(int from, int to, int steps) {

        if (from >= to)
            throw new IllegalArgumentException("Exception: from >= to");
        if (from < 0)
            throw new IllegalArgumentException("Exception: from < 0");
        if (steps < 0)
            throw new IllegalArgumentException("Exception: steps < 0");

        return new Iterator<Integer>() {

            // e^t(x) / (1 + e^t(x)) in [0, xmax] for t(x) = 8 * x / xmax - 4

            private int step    = 0;
            private int delta   = to - from;
            private int maxStep = steps + 1;

            @Override
            public boolean hasNext() {
                return step <= maxStep;
            }

            @Override
            public Integer next() {

                if (step > maxStep)
                    return to;

                int next;
                if (step == maxStep)
                    next = to;
                else if (step == 0)
                    next = from;
                else {
                    double x = step / (double) maxStep;
                    double tmp = Math.exp(8 * x - 4);
                    next = (int) (delta * (tmp / (1 + tmp)) + from);
                }

                step++;
                return next;
            }


            // result for (from, to, steps) = (0, 20, 18)

            // |20|  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  | x|
            // |19|  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  | x| x|  |
            // |18|  |  |  |  |  |  |  |  |  |  |  |  |  |  |  | x| x|  |  |  |
            // |17|  |  |  |  |  |  |  |  |  |  |  |  |  |  | x|  |  |  |  |  |
            // |16|  |  |  |  |  |  |  |  |  |  |  |  |  | x|  |  |  |  |  |  |
            // |15|  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |
            // |14|  |  |  |  |  |  |  |  |  |  |  |  | x|  |  |  |  |  |  |  |
            // |13|  |  |  |  |  |  |  |  |  |  |  | x|  |  |  |  |  |  |  |  |
            // |12|  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |
            // |11|  |  |  |  |  |  |  |  |  |  | x|  |  |  |  |  |  |  |  |  |
            // |10|  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |
            // | 9|  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |
            // | 8|  |  |  |  |  |  |  |  |  | x|  |  |  |  |  |  |  |  |  |  |
            // | 7|  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |
            // | 6|  |  |  |  |  |  |  |  | x|  |  |  |  |  |  |  |  |  |  |  |
            // | 5|  |  |  |  |  |  |  | x|  |  |  |  |  |  |  |  |  |  |  |  |
            // | 4|  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |
            // | 3|  |  |  |  |  |  | x|  |  |  |  |  |  |  |  |  |  |  |  |  |
            // | 2|  |  |  |  |  | x|  |  |  |  |  |  |  |  |  |  |  |  |  |  |
            // | 1|  |  |  | x| x|  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |
            // | 0| x| x| x|  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |
            //    | 0| 1| 2| 3| 4| 5| 6| 7| 8| 9|10|11|12|13|14|15|16|17|18|19|
        };
    }
}