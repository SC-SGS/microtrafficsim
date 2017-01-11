package microtrafficsim.interesting.emotions;


/**
 * Serves methods to handle angry objects like angry vehicles standing in a jam.
 *
 * @author Dominic Parga Cacheiro
 */
public interface Hulk {

    /**
     * This method increases the current anger of the object.
     */
    void becomeMoreAngry();

    /**
     * This method decreases the current anger of the object.
     */
    void calmDown();

    /**
     * @return The current anger of this object.
     */
    int getAnger();

    /**
     * @return The total anger of this object if you would have never called {@code calmDown()}. So this is equal to
     * {@code getAnger()}, if you really have never called {@code calmDown()}.
     *
     * @see #calmDown()
     * @see #getAnger()
     */
    int getTotalAnger();

    /**
     * @return The maximum dose of anger an object can possess.
     */
    int getMaxAnger();
}