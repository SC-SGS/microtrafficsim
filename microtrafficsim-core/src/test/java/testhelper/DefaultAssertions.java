package testhelper;

import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.routes.Route;
import microtrafficsim.core.logic.streets.DirectedEdge;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Dominic Parga Cacheiro
 */
public abstract class DefaultAssertions {

    public static void assertRoutes(Route expected, Route actual) {
        assertEquals("Routes' sizes are not equal.", expected.size(), actual.size());
        assertEquals("Route origins are not equal.", expected.getOrigin(), actual.getOrigin());
        assertEquals("Route destinations are not equal.", expected.getDestination(), actual.getDestination());

        Iterator<DirectedEdge> expectedIter = expected.iterator();
        Iterator<DirectedEdge> actualIter = actual.iterator();
        while (expectedIter.hasNext()) {
            assertTrue("Actual iterator is empty before expected one does.", actualIter.hasNext());
            assertEdges(expectedIter.next(), actualIter.next());
        }

        assertEquals("Edge iterators are not equal.", expectedIter.hasNext(), actualIter.hasNext());
    }

    public static void assertEdges(DirectedEdge expected, DirectedEdge actual) {
        assertEquals("Edge ids are not equal.", expected.getId(), actual.getId());
        assertEquals("Edges' keys are not equal.", expected.key(), actual.key());
        assertNodes(expected.getOrigin(), actual.getOrigin());
        assertNodes(expected.getDestination(), actual.getDestination());
    }

    public static void assertNodes(Node expected, Node actual) {
        assertEquals("Nodes' keys are not equal.", expected.key(), actual.key());
    }
}
