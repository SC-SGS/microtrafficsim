package microtrafficsim.core.serialization.kryo.impl.exfmt.components;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import microtrafficsim.core.exfmt.ecs.Entity;
import microtrafficsim.core.exfmt.ecs.components.GraphEdgeComponent;
import microtrafficsim.core.map.StreetType;
import microtrafficsim.math.Vec2d;


public class GraphEdgeComponentSerializer extends Serializer<GraphEdgeComponent> {

    @Override
    public void write(Kryo kryo, Output output, GraphEdgeComponent object) {
        kryo.writeClassAndObject(output, object.getEntity());
        kryo.writeObject(output, object.getLength());
        kryo.writeObject(output, object.getStreetType());
        kryo.writeObject(output, object.getForwardLanes());
        kryo.writeObject(output, object.getBackwardLanes());
        kryo.writeObject(output, object.getForwardMaxVelocity());
        kryo.writeObject(output, object.getBackwardMaxVelocity());
        kryo.writeObject(output, object.getOrigin());
        kryo.writeObject(output, object.getDestination());
        kryo.writeObjectOrNull(output, object.getOriginDirection(), Vec2d.class);
        kryo.writeObjectOrNull(output, object.getDestinationDirection(), Vec2d.class);
    }

    @Override
    public GraphEdgeComponent read(Kryo kryo, Input input, Class<GraphEdgeComponent> type) {
        GraphEdgeComponent component = new GraphEdgeComponent(null);
        kryo.reference(component);

        component.setEntity((Entity) kryo.readClassAndObject(input));
        component.setLength(kryo.readObject(input, Double.class));
        component.setStreetType(kryo.readObject(input, StreetType.class));
        component.setForwardLanes(kryo.readObject(input, Integer.class));
        component.setBackwardLanes(kryo.readObject(input, Integer.class));
        component.setForwardMaxVelocity(kryo.readObject(input, Float.class));
        component.setBackwardMaxVelocity(kryo.readObject(input, Float.class));
        component.setOrigin(kryo.readObject(input, Long.class));
        component.setDestination(kryo.readObject(input, Long.class));
        component.setOriginDirection(kryo.readObjectOrNull(input, Vec2d.class));
        component.setDestinationDirection(kryo.readObjectOrNull(input, Vec2d.class));

        return component;
    }
}
