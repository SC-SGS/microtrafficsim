package microtrafficsim.core.logic.routes;

import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.shortestpath.ShortestPathEdge;
import microtrafficsim.utils.hashing.FNVHashBuilder;

import java.util.Iterator;

/**
 * @author Dominic Parga Cacheiro
 */
public class MetaRoute implements Route {
    private final Node origin;
    private final Node destination;

    public MetaRoute(Node origin, Node destination) {
        this.origin = origin;
        this.destination = destination;
    }


    @Override
    public int hashCode() {
        return new FNVHashBuilder()
                .add(origin)
                .add(destination)
                .getHash();
    }

    @Override
    public String toString() {
        String str = "";
        str += "Origin       = " + origin + "\n";
        str += "Destination  = " + destination;
        return str;
    }


    @Override
    public MetaRoute clone() {
        return new MetaRoute(origin, destination);
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public Node getOrigin() {
        return origin;
    }

    @Override
    public Node getDestination() {
        return destination;
    }

    @Override
    public Iterator<ShortestPathEdge<Node>> iterator() {
        return new Iterator<ShortestPathEdge<Node>>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public ShortestPathEdge<Node> next() {
                return null;
            }
        };
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public ShortestPathEdge<Node> peek() {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public ShortestPathEdge<Node> pop() {
        throw new UnsupportedOperationException();
    }
}
