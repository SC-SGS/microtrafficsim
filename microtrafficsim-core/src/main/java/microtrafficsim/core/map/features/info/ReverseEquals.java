package microtrafficsim.core.map.features.info;


/**
 * Interface to indicate that two Objects of a class may be reverse-equal and
 * provide means to test this.
 *
 * @author Maximilian Luz
 */
public interface ReverseEquals {

    /**
     * Tests if this equals {@code obj} when one of them is reversed.
     * This method should be equal to {@linkplain Object#equals(Object)} except that one
     * object is reversed.
     *
     * @param obj the object to compare this for.
     * @return {@code true} if this reverse-equals {@code obj}.
     */
    boolean reverseEquals(Object obj);
}
