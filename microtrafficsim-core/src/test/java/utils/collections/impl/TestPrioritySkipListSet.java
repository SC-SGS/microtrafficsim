package utils.collections.impl;

import microtrafficsim.utils.collections.skiplist.PrioritySkipListSet;
import microtrafficsim.utils.collections.skiplist.SkipList;

/**
 * Tests implementations of {@link SkipList}
 *
 * @author Dominic Parga Cacheiro
 */
public class TestPrioritySkipListSet extends AbstractTestSkipList {

    public TestPrioritySkipListSet() {
        super(PrioritySkipListSet::new);
    }
}