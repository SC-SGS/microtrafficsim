package microtrafficsim.core.serialization.kryo.impl.exfmt.components;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import microtrafficsim.core.exfmt.ecs.Entity;
import microtrafficsim.core.exfmt.ecs.components.StreetComponent;


public class StreetComponentSerializer extends Serializer<StreetComponent> {

    @Override
    public void write(Kryo kryo, Output output, StreetComponent object) {
        kryo.writeClassAndObject(output, object.getEntity());
        kryo.writeObject(output, object.getLayer());
        kryo.writeObject(output, object.getLength());
        kryo.writeObject(output, object.getDistances());
        kryo.writeObject(output, object.getLanesFwd());
        kryo.writeObject(output, object.getLanesBwd());
    }

    @Override
    public StreetComponent read(Kryo kryo, Input input, Class<StreetComponent> type) {
        StreetComponent component = new StreetComponent(null, 0, 0, null, 0, 0);
        kryo.reference(component);

        component.setEntity((Entity) kryo.readClassAndObject(input));
        component.setLayer(kryo.readObject(input, Double.class));
        component.setLength(kryo.readObject(input, Double.class));
        component.setDistances(kryo.readObject(input, double[].class));
        component.setLanesFwd(kryo.readObject(input, Integer.class));
        component.setLanesBwd(kryo.readObject(input, Integer.class));

        return component;
    }
}
