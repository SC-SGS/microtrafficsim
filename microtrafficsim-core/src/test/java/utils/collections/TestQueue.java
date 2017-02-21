package utils.collections;

/**
 * @author Dominic Parga Cacheiro
 */
public interface TestQueue extends TestCollection {

    void testOfferObj();

    void testRemove();

    void testPoll();

    void testElement();

    void testPeek();
}