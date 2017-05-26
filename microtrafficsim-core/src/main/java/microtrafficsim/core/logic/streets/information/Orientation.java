package microtrafficsim.core.logic.streets.information;

/**
 * @author Dominic Parga Cacheiro
 */
public final class Orientation implements Comparable<Orientation> {
    public static final Orientation FORWARD = new Orientation();
    public static final Orientation BACKWARD = new Orientation();

    private Orientation() {

    }

    @Override
    public String toString() {
        return this == FORWARD ? "forward" : "backward";
    }

    @Override
    public int compareTo(Orientation o) {
        int value = this == FORWARD ? 1 : 0;
        int otherValue = o == FORWARD ? 1 : 0;
        return value - otherValue;
    }
}
