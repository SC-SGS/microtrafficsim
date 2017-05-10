package microtrafficsim.utils.collections;

/**
 * <p>
 * This data structure works like a {@code PriorityQueue}. The difference is: a {@code QueueSet} detects double
 * elements in O(logn) (due to set) and removes the old one in O(logn). All given runtime complexities are expected
 * (but "with high probability").
 *
 * <p>
 * Important note: Double elements are recognized by their priority determined by a comparator (or their natural
 * order). The advantage over a simple set is the access to a certain index in O(logn). You can give a comparator
 * comparing an object's hashcode to the skip list and have a random object of the list in O(logn).
 *
 * @author Dominic Parga Cacheiro
 */
public interface SkipList<E> extends QueueSet<E> {
    long getSeed();
}
