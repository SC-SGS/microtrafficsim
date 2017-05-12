package microtrafficsim.math.random.distributions.impl;

import microtrafficsim.math.random.distributions.WheelOfFortune;
import microtrafficsim.utils.collections.PrioritySkipListSet;
import microtrafficsim.utils.collections.SkipList;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Basic implementation using a {@link PrioritySkipListSet} comparing by the objects' hashcodes. For the random number
 * generator used for {@link #nextObject()}, {@link Random} is used.
 *
 * @author Dominic Parga Cacheiro
 */
public class BasicWheelOfFortune<T> implements WheelOfFortune<T> {

    private Random random;
    private SkipList<T> elements;
    private Map<T, Integer> fields;
    private int n;

    public BasicWheelOfFortune(long seed) {
        this(new Random(seed));
    }

    public BasicWheelOfFortune(Random random) {
        this.random = random;
        elements    = new PrioritySkipListSet<>(Comparator.comparingLong(Object::hashCode));
        fields      = new HashMap<>();
        n           = 0;
    }

    /**
     * The runtime of this implementation is determined by
     * {@link HashMap#containsKey(Object)} and {@link HashMap#put(Object, Object)}.
     */
    public void add(T t, int weight) {
        if (!fields.containsKey(t) && weight > 0) {
            elements.add(t);
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
            else {
                elements.remove(t);
                elements.add(t);
                n += weight - fields.put(t, weight);
            }
        }
    }

    @Override
    public void incWeight(T t) {
        Integer weight = fields.get(t);
        if (weight != null) {
            elements.remove(t);
            elements.add(t);
            fields.put(t, weight + 1);
        } else {
            elements.add(t);
            fields.put(t, 1);
        }
        n++;
    }

    @Override
    public void decWeight(T t) {
        Integer weight = fields.get(t);
        if (weight != null) {
            if (--weight > 0) {
                elements.remove(t);
                elements.add(t);
                fields.put(t, weight);
                n--;
            } else remove(t);
        }
    }

    /**
     * The runtime of this implementation is determined by {@link HashMap#remove(Object)}.
     */
    @Override
    public void remove(T t) {
        elements.remove(t);
        n -= fields.remove(t);
    }

    @Override
    public void clear() {
        elements.clear();
        fields.clear();
        n = 0;
    }

    @Override
    public int size() {
        return fields.size();
    }

    /**
     * The runtime of this method is O(n) where n is the number of elements in this wheel. Unweighted, the runtime is
     * in O(log(n)) due to {@link SkipList}.
     */
    @Override
    public T nextObject(boolean weightedUniformly) {
        if (weightedUniformly)
            return elements.get(random.nextInt(size()));
        else {
            if (n <= 0) return null;

            int i = random.nextInt(n);
            Iterator<T> objects = elements.iterator();
            T lastObj = null;

            while (i >= 0) {
                lastObj = objects.next();
                i -= fields.get(lastObj);
            }

            return lastObj;
        }
    }

    @Override
    public Iterator<T> iterator() {
        return elements.iterator();
    }

    @Override
    public long getSeed() {
        return random.getSeed();
    }

    @Override
    public void setSeed(long seed) {
        random.setSeed(seed);
    }

    /**
     * Resets the used random variable but does not clear this wheel.
     */
    @Override
    public void reset() {
        random.reset();
    }
}