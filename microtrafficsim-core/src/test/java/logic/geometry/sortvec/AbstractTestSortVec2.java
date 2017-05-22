package logic.geometry.sortvec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertFalse;

/**
 * @author Dominic Parga Cacheiro
 */
public abstract class AbstractTestSortVec2 {
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
        assertFalse("Vectors are still not shuffled after " + shuffleRepetationCount + " times", notShuffled);

        return mixed;
    }
}
