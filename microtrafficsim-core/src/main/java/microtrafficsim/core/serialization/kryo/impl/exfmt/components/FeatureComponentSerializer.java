package microtrafficsim.core.serialization.kryo.impl.exfmt.components;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import microtrafficsim.core.exfmt.ecs.Entity;
import microtrafficsim.core.exfmt.ecs.components.FeatureComponent;
import microtrafficsim.core.map.FeatureDescriptor;

import java.util.Set;


public class FeatureComponentSerializer extends Serializer<FeatureComponent> {

    @Override
    public void write(Kryo kryo, Output output, FeatureComponent object) {
        kryo.writeClassAndObject(output, object.getEntity());
        kryo.writeClassAndObject(output, object.getAll());
    }

    @Override
    @SuppressWarnings("unchecked")
    public FeatureComponent read(Kryo kryo, Input input, Class<FeatureComponent> type) {
        FeatureComponent component = new FeatureComponent(null);
        kryo.reference(component);

        component.setEntity((Entity) kryo.readClassAndObject(input));
        component.addAll((Set<FeatureDescriptor>) kryo.readClassAndObject(input));

        return component;
    }
}
