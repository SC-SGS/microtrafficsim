package microtrafficsim.math.random.distributions.impl;

import microtrafficsim.math.random.distributions.WheelOfFortune;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Basic implementation using a {@link HashMap}. For the random number generator used for {@link #nextObject()},
 * {@link ResettableRandom} is used.
 *
 * @author Dominic Parga Cacheiro
 */
public class BasicWheelOfFortune implements WheelOfFortune {

    private ResettableRandom random;
    private HashMap<Object, Integer> fields;
    private int               n;

    public BasicWheelOfFortune(long seed) {
        this(new ResettableRandom(seed));
    }

    public BasicWheelOfFortune(ResettableRandom random) {
        this.random = random;
        fields      = new HashMap<>();
        n           = 0;
    }

    /**
     * The runtime of this implementation is determined by
     * {@link HashMap#containsKey(Object)} and {@link HashMap#put(Object, Object)}.
     */
    public void add(Object obj, int weight) {
        if (!fields.containsKey(obj) && weight > 0) {
            fields.put(obj, weight);
            n += weight;
        }
    }

    /**
     * The runtime of this implementation is determined by
     * {@link HashMap#containsKey(Object)} and {@link HashMap#put(Object, Object)}.
     */
    public void update(Object obj, int weight) {

        if (weight < 0)
            throw new IllegalArgumentException("The weight should be updated to < 0, which is forbidden.");

        if (weight > 0 && fields.containsKey(obj))
            n += weight - fields.put(obj, weight);
    }

    /**
     * The runtime of this implementation is determined by {@link HashMap#remove(Object)}.
     */
    public void remove(Object obj) {
        fields.remove(obj);
    }

    /**
     * The runtime of this method is O(n) where n is the number of elements in this wheel.
     */
    public Object nextObject() {

        if (n <= 0) return null;

        int              i       = random.nextInt(n);
        Iterator<Object> objects = fields.keySet().iterator();
        Object           lastObj = null;

        while (i >= 0) {
            lastObj = objects.next();
            i -= fields.get(lastObj);
        }

        return lastObj;
    }

    /*
    |================|
    | (i) Resettable |
    |================|
    */
    @Override
    public void reset() {
        random.reset();
        fields.clear();
        n = 0;
    }
}
