package microtrafficsim.core.serialization.kryo.impl.exfmt.components;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import microtrafficsim.core.exfmt.ecs.Entity;
import microtrafficsim.core.exfmt.ecs.components.GraphNodeComponent;
import microtrafficsim.core.simulation.configs.CrossingLogicConfig;

import java.util.Set;


public class GraphNodeComponentSerializer extends Serializer<GraphNodeComponent> {

    @Override
    public void write(Kryo kryo, Output output, GraphNodeComponent object) {
        kryo.writeClassAndObject(output, object.getEntity());
        kryo.writeClassAndObject(output, object.getCrossingLogicConfig());
        kryo.writeClassAndObject(output, object.getEdges());
        kryo.writeClassAndObject(output, object.getConnectors());
    }

    @Override
    @SuppressWarnings("unchecked")
    public GraphNodeComponent read(Kryo kryo, Input input, Class<GraphNodeComponent> type) {
        GraphNodeComponent component = new GraphNodeComponent(null);
        kryo.reference(component);

        component.setEntity((Entity) kryo.readClassAndObject(input));
        component.setCrossingLogicConfig((CrossingLogicConfig) kryo.readClassAndObject(input));
        component.setEdges((Set<Long>) kryo.readClassAndObject(input));
        component.setConnectors((Set<GraphNodeComponent.Connector>) kryo.readClassAndObject(input));

        return component;
    }


    public static class Connector extends Serializer<GraphNodeComponent.Connector> {

        @Override
        public void write(Kryo kryo, Output output, GraphNodeComponent.Connector object) {
            kryo.writeObject(output, object.fromEdge);
            kryo.writeObject(output, object.fromLane);
            kryo.writeObject(output, object.toEdge);
            kryo.writeObject(output, object.toLane);
        }

        @Override
        public GraphNodeComponent.Connector read(Kryo kryo, Input input, Class<GraphNodeComponent.Connector> type) {
            long fromEdge = kryo.readObject(input, Long.class);
            int  fromLane = kryo.readObject(input, Integer.class);
            long toEdge = kryo.readObject(input, Long.class);
            int  toLane = kryo.readObject(input, Integer.class);

            return new GraphNodeComponent.Connector(fromEdge, fromLane, toEdge, toLane);
        }
    }
}
