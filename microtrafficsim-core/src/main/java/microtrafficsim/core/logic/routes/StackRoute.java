package microtrafficsim.core.logic.routes;

import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.shortestpath.ShortestPathEdge;
import microtrafficsim.utils.hashing.FNVHashBuilder;
import microtrafficsim.utils.strings.builder.LevelStringBuilder;

import java.util.Stack;

/**
 * @author Dominic Parga Cacheiro
 */
public class StackRoute extends Stack<ShortestPathEdge<Node>> implements Route {
    private int spawnDelay;


    public StackRoute() {
        this(0);
    }

    public StackRoute(int spawnDelay) {
        this.spawnDelay = spawnDelay;
    }


    @Override
    public synchronized int hashCode() {
        FNVHashBuilder hashBuilder = new FNVHashBuilder();
        forEach(hashBuilder::add);
        return hashBuilder.getHash();
    }

    @Override
    public synchronized String toString() {
        LevelStringBuilder strBuilder = new LevelStringBuilder();
        strBuilder.appendln("<route>");
        strBuilder.incLevel();

        if (isEmpty())
            strBuilder.appendln("Route is empty.");
        else {
            strBuilder.appendln("hash  = " + hashCode());
            strBuilder.appendln("start = " + getOrigin().toString());
            strBuilder.appendln("end   = " + getDestination().toString());
            strBuilder.appendln("size  = " + size());
        }

        strBuilder.decLevel();
        strBuilder.appendln("</route>");
        return strBuilder.toString();
    }

    @Override
    public synchronized boolean equals(Object o) {
        return hashCode() == o.hashCode()
                && super.equals(o);
    }


    @Override
    public synchronized StackRoute clone() {
        StackRoute copy = new StackRoute(spawnDelay);
        forEach(copy::push);
        return copy;
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
        if (isEmpty())
            return null;
        return peek().getOrigin();
    }

    @Override
    public Node getDestination() {
        if (isEmpty())
            return null;
        return get(0).getDestination();
    }
}
