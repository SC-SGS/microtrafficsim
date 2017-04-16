package microtrafficsim.math.random.distributions.impl;

import microtrafficsim.math.random.distributions.WheelOfFortune;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Basic implementation using a {@link HashMap}. For the random number generator used for {@link #nextObject()},
 * {@link Random} is used.
 *
 * @author Dominic Parga Cacheiro
 */
public class BasicWheelOfFortune<T> implements WheelOfFortune<T> {

    private Random random;
    private HashMap<T, Integer> fields;
    private int               n;

    public BasicWheelOfFortune(long seed) {
        this(new Random(seed));
    }

    public BasicWheelOfFortune(Random random) {
        this.random = random;
        fields      = new HashMap<>();
        n           = 0;
    }

    /**
     * The runtime of this implementation is determined by
     * {@link HashMap#containsKey(Object)} and {@link HashMap#put(Object, Object)}.
     */
    public void add(T t, int weight) {
        if (!fields.containsKey(t) && weight > 0) {
            fields.put(t, weight);
            n += weight;
        }
    }

    /**
     * The runtime of this implementation is determined by
     * {@link HashMap#containsKey(Object)} and {@link HashMap#put(Object, Object)}.
     */
    public void update(T t, int weight) {

        if (weight < 0)
            throw new IllegalArgumentException("The weight should be updated to < 0, which is forbidden.");

        if (fields.containsKey(t)) {
            if (weight == 0)
                remove(t);
            else
                n += weight - fields.put(t, weight);
        }
    }

    @Override
    public void incWeight(T t) {
        Integer weight = fields.get(t);
        if (weight != null)
            fields.put(t, weight + 1);
        else
            fields.put(t, 1);
        n++;
    }

    @Override
    public void decWeight(T t) {
        Integer weight = fields.get(t);
        if (weight != null) {
            if (--weight > 0)
                fields.put(t, weight);
            else
                fields.remove(t);

            n--;
        }
    }

    /**
     * The runtime of this implementation is determined by {@link HashMap#remove(Object)}.
     */
    public void remove(T t) {
        n -= fields.remove(t);
    }

    /**
     * The runtime of this method is O(n) where n is the number of elements in this wheel.
     */
    public T nextObject() {

        if (n <= 0) return null;

        int         i       = random.nextInt(n);
        Iterator<T> objects = fields.keySet().iterator();
        T           lastObj = null;

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
