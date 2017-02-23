package utils.collections.impl;

import microtrafficsim.utils.collections.skiplist.PrioritySkipList;
import microtrafficsim.utils.collections.skiplist.SkipList;

/**
 * Tests implementations of {@link SkipList}
 *
 * @author Dominic Parga Cacheiro
 */
public class TestPrioritySkipList extends AbstractTestSkipList {

    public TestPrioritySkipList() {
        super(PrioritySkipList::new);
    }
}