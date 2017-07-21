package microtrafficsim.core.logic.routes;

import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streets.DirectedEdge;

import java.util.Iterator;

/**
 * @author Dominic Parga Cacheiro
 */
public class MetaRoute implements Route {
    private final Node origin;
    private final Node destination;
    private int spawnDelay;
    private boolean isMonitored;


    public MetaRoute(Node origin, Node destination) {
        this(origin, destination, 0);
    }

    public MetaRoute(Node origin, Node destination, int spawndelay) {
        this.origin = origin;
        this.destination = destination;
        this.spawnDelay = spawndelay;
        this.isMonitored = false;
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
        MetaRoute route = new MetaRoute(origin, destination, spawnDelay);
        route.isMonitored = isMonitored;
        return route;
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
    public boolean isMonitored() {
        return isMonitored;
    }

    @Override
    public void setMonitored(boolean isMonitored) {
        this.isMonitored = isMonitored;
    }

    @Override
    public int getSpawnDelay() {
        return spawnDelay;
    }

    @Override
    public void setSpawnDelay(int spawnDelay) {
        this.spawnDelay = spawnDelay;
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
    public Iterator<DirectedEdge> iterator() {
        return new Iterator<DirectedEdge>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public DirectedEdge next() {
                return null;
            }
        };
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public DirectedEdge peek() {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public DirectedEdge pop() {
        throw new UnsupportedOperationException();
    }
}
