package microtrafficsim.math.random.distributions;

import microtrafficsim.math.random.Seeded;
import microtrafficsim.utils.Resettable;

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
public interface WheelOfFortune<T> extends Resettable, Seeded {

    /**
     * Adds the given object with the given weight to this wheel. If the object is already added, nothing happens. To
     * change the object's weight, call {@link #update(T, int)}.
     *
     * @param t This object is added to this wheel if the wheel doesn't contain it.
     * @param weight The weight of this object used for its probability calculation. For more information, see class
     *               documentation. If it is 0, nothing happens. If it is smaller than 0, an exception should be thrown.
     */
    void add(T t, int weight);

    /**
     * Updates the given object's weight to the given one. If the object is not in this wheel, nothing happens.
     *
     * @param t This object is added to this wheel if the wheel doesn't contain it.
     * @param weight The weight of this object used for its probability calculation. For more information, see class
     *               documentation. If it is 0, nothing happens. If it is smaller than 0, an exception should be thrown.
     */
    void update(T t, int weight);

    /**
     * Increments the weight for the given object. Does add the object if not contained.
     *
     * @param t The weight of this object should be increased by 1.
     */
    void incWeight(T t);

    /**
     * Decrements the weight for the given object (minimum is 0). Does nothing if the element is not contained.
     *
     * @param t The weight of this object should be decreased by 1.
     */
    void decWeight(T t);

    /**
     * Removes the given object from this wheel.
     *
     * @param t Object that should be removed.
     */
    void remove(T t);

    /**
     * Removes all elements from this wheel but does not reset it.
     */
    void clear();

    /**
     * @return Number of elements stored in this wheel
     */
    int size();

    default boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Calls {@link #nextObject(boolean) nextObject(true)}
     */
    default T nextObject() {
        return nextObject(false);
    }

    /**
     * @param weighted If false, all elements in this wheel are weighted equally for the use of this method
     *
     * @return One object in this wheel. How the probability for an object is calculated is described in the class
     * documentation.
     */
    T nextObject(boolean weightedUniformly);
}
