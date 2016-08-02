package microtrafficsim.math;


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
}
