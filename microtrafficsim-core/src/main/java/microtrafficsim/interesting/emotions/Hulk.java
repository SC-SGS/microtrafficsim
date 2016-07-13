package microtrafficsim.interesting.emotions;


/**
 * This interface serves methods to handle angry objects like angry vehicles sitting in a jam.
 *
 * @author Dominic Parga Cacheiro
 */
public interface Hulk {

    /**
     * This method should increase the current anger of the object.
     */
    void becomeMoreAngry();

    /**
     * This method should decrease the current anger of the object.
     */
    void calmDown();

    /**
     * @return The current anger of this object.
     */
    int getAnger();

    /**
     * @return The total anger of this object if you would not {@link #calmDown()}.
     */
    int getTotalAnger();

    /**
     * @return The maximum dose of anger an object can possess.
     */
    int getMaxAnger();
}