package utils.collections;

import microtrafficsim.utils.collections.PrioritySkipList;
import microtrafficsim.utils.collections.SkipList;

/**
 * Tests implementations of {@link SkipList}
 *
 * @author Dominic Parga Cacheiro
 */
public class TestPrioritySkipList extends AbstractTestQueueSet {

    public TestPrioritySkipList() {
        super(PrioritySkipList::new);
    }
}