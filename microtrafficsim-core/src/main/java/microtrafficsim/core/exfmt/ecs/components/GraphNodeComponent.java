package microtrafficsim.core.exfmt.ecs.components;

import microtrafficsim.core.exfmt.ecs.Component;
import microtrafficsim.core.exfmt.ecs.Entity;
import microtrafficsim.core.simulation.configs.CrossingLogicConfig;
import microtrafficsim.utils.hashing.FNVHashBuilder;

import java.util.HashSet;
import java.util.Set;


public class GraphNodeComponent extends Component {

    private CrossingLogicConfig crossingLogicConfig = null;
    private Set<Long> edges = new HashSet<>();
    private Set<Connector> connectors = new HashSet<>();

    public GraphNodeComponent(Entity entity) {
        super(entity);
    }


    public CrossingLogicConfig getCrossingLogicConfig() {
        return crossingLogicConfig;
    }

    public void setCrossingLogicConfig(CrossingLogicConfig crossingLogicConfig) {
        this.crossingLogicConfig = crossingLogicConfig;
    }


    public Set<Long> getEdges() {
        return edges;
    }

    public void setEdges(Set<Long> edges) {
        this.edges = edges;
    }


    public Set<Connector> getConnectors() {
        return connectors;
    }

    public void setConnectors(Set<Connector> connectors) {
        this.connectors = connectors;
    }


    public static class Connector {
        public final long fromEdge;
        public final int  fromLane;
        public final long toEdge;
        public final int  toLane;

        public Connector(long fromEdge, int fromLane, long toEdge, int toLane) {
            this.fromEdge = fromEdge;
            this.fromLane = fromLane;
            this.toLane = toLane;
            this.toEdge = toEdge;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Connector))
                return false;

            Connector other = (Connector) obj;

            return this.fromEdge == other.fromEdge
                    && this.fromLane == other.fromLane
                    && this.toEdge == other.toEdge
                    && this.toLane == other.toLane;
        }

        @Override
        public int hashCode() {
            return new FNVHashBuilder()
                    .add(fromEdge)
                    .add(fromLane)
                    .add(toEdge)
                    .add(toLane)
                    .getHash();
        }
    }
}
