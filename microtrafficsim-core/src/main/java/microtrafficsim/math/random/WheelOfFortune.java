package microtrafficsim.math.random;

import java.util.*;

/**
 * TODO (also test)
 *
 * @author Dominic Parga Cacheiro
 */
public class WheelOfFortune {

    private Random random;
    private HashMap<Object, Integer> fields;
    private ArrayList<Object> orderedObjects;
    private int n;

    public WheelOfFortune(long seed) {
        random = new Random(seed);
        fields = new HashMap<>();
        orderedObjects = new ArrayList<>();
        n = 0;
    }

    /**
     * TODO<br>
     * <br>
     * but can be called multiple times
     *
     * @param obj
     * @param size
     */
    public void addField(Object obj, int size) {
        if (!fields.containsKey(obj) && size > 0) {
            fields.put(obj, size);
            orderedObjects.add(obj);
            n += size;
        }
    }

    public void updateFieldSize(Object obj, int size) {
        if (size <= 0) {
            fields.remove(obj);
        } else {
            Integer oldSize = fields.put(obj, size);
            if (oldSize != null)
                n += size - oldSize;
            else
                n += size;
        }
    }

    /**
     * TODO Laufzeit könnte man auf Kosten von Speicher verbessern
     *
     * @param obj
     */
    public void remove(Object obj) {
        fields.remove(obj);
        orderedObjects.remove(obj);
    }

    public Object nextObject() {
        if (n <= 0)
            return null;

        int i = random.nextInt(n);
        Iterator<Object> objects = orderedObjects.iterator();
        Object lastObj = null;
        if (objects.hasNext()) {
            do {
                lastObj = objects.next();
                i -= fields.get(lastObj);

            } while (i >= 0 && objects.hasNext());
        }

        return lastObj;
    }
}
