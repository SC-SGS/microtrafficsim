package utils.collections;

import microtrafficsim.utils.collections.PrioritySkipListSet;

/**
 * Tests implementation of {@link PrioritySkipListSet}
 *
 * @author Dominic Parga Cacheiro
 */
public class TestPrioritySkipListSet extends AbstractTestQueueSet {
    public TestPrioritySkipListSet() {
        super(PrioritySkipListSet::new);
    }
}