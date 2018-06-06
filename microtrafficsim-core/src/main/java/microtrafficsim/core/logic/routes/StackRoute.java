package microtrafficsim.core.logic.routes;

import java.util.Stack;

import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.utils.strings.builder.LevelStringBuilder;

/**
 * @author Dominic Parga Cacheiro
 */
public class StackRoute extends Stack<DirectedEdge> implements Route {
    private int spawnDelay;
    private boolean isMonitored;


    public StackRoute() {
        this(0);
    }

    public StackRoute(int spawnDelay) {
        this.spawnDelay = spawnDelay;
        isMonitored = false;
    }


    @Override
    public synchronized String toString() {
        LevelStringBuilder strBuilder = new LevelStringBuilder()
                .setDefaultLevelSubString()
                .setDefaultLevelSeparator();
        strBuilder.appendln("<" + getClass().getSimpleName() + ">").incLevel();
        {
            if (isEmpty())
                strBuilder.appendln("Route is empty.");
            else {
                strBuilder.appendln("hash       = " + hashCode());
                strBuilder.appendln("spawndelay = " + spawnDelay);
                strBuilder.appendln("start      = " + getOrigin());
                strBuilder.appendln("end        = " + getDestination());
                strBuilder.appendln("size       = " + size());
            }
        }
        strBuilder.decLevel().append("</" + getClass().getSimpleName() + ">");
        return strBuilder.toString();
    }


    @Override
    public synchronized StackRoute clone() {
        StackRoute copy = new StackRoute(spawnDelay);
        copy.setMonitored(isMonitored);
        forEach(copy::push);
        return copy;
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
