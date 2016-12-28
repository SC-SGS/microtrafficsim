package microtrafficsim.math.random.distributions;

import java.util.Iterator;

/**
 * <p>
 * This interface defines the functionality of a wheel of fortune. You associate objects with an int-value. This value
 * stands for an object's weight and is used for its probability calculation. The probability of getting
 * an object is equal to its weight divided by the sum of all weights.
 *
 * <p>
 * For example: Let this wheel contain two objects, {@code obj1} with int-value 2 and {@code obj2} with int-value 4.<br>
 * Thus the probability of <br>
 * &bull getting {@code obj1} is {@code 2/(2+4) = 1/3} and <br>
 * &bull getting {@code obj2} is {@code 4/(2+4) = 2/3}. <br>
 * As you see, the sum of all probabilities is 1.
 *
 * @author Dominic Parga Cacheiro
 */
public interface WheelOfFortune {

    /**
     * Adds the given object with the given weight to this wheel. If the object is already added, nothing happens. To
     * change the object's weight, call {@link #update(Object, int)}.
     *
     * @param obj This object is added to this wheel if the wheel doesn't contain it.
     * @param weight The weight of this object used for its probability calculation. For more information, see class
     *               documentation. If it is 0, nothing happens. If it is smaller than 0, an exception should be thrown.
     */
    void add(Object obj, int weight);

    /**
     * Updates the given object's weight to the given one. If the object is not in this wheel, nothing happens.
     *
     * @param obj This object is added to this wheel if the wheel doesn't contain it.
     * @param weight The weight of this object used for its probability calculation. For more information, see class
     *               documentation. If it is 0, nothing happens. If it is smaller than 0, an exception should be thrown.
     */
    void update(Object obj, int weight);

    /**
     * Removes the given object from this wheel.
     *
     * @param obj Object that should be removed.
     */
    void remove(Object obj);

    /**
     * @return One object in this wheel. How the probability for an object is calculated is described in the class
     * documentation.
     */
    Object nextObject();
}
