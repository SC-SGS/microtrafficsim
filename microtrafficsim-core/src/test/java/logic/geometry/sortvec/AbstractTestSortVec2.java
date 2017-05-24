package logic.geometry.sortvec;

import microtrafficsim.utils.logging.EasyMarkableLogger;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Dominic Parga Cacheiro
 */
public abstract class AbstractTestSortVec2 {
    public static final Logger logger = new EasyMarkableLogger(AbstractTestSortVec2.class);


    // if shuffling is unsuccessful after this, an AssertionError is thrown
    private static final int shuffleRepetationCount = 10;


    public static <T> ArrayList<T> shuffleCorrectly(List<T> sorted) {
        ArrayList<T> mixed = new ArrayList<>(sorted);

        /* check if shuffled */
        boolean notShuffled = true;
        for (int i = 0; i < shuffleRepetationCount; i++) {
            Collections.shuffle(mixed);

            Iterator<T> sortedIter = sorted.iterator();
            for (T t : mixed)
                notShuffled &= t.equals(sortedIter.next());
            if (!notShuffled)
                break;
        }
        if (notShuffled) {
            logger.warn("Vectors are still not shuffled after " + shuffleRepetationCount + " times. Reverse order is " +
                    "taken instead.");
            Collections.reverse(mixed);
        }

        return mixed;
    }
}
