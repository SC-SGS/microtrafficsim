package microtrafficsim.core.logic.streets.information;

/**
 * @author Dominic Parga Cacheiro
 */
public enum Orientation  {
    FORWARD, BACKWARD;

    @Override
    public String toString() {
        return this == FORWARD ? "forward" : "backward";
    }
}
