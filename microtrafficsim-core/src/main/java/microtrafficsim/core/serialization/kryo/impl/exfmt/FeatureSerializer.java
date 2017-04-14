package microtrafficsim.core.serialization.kryo.impl.exfmt;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import microtrafficsim.core.exfmt.base.FeatureSet;
import microtrafficsim.core.exfmt.ecs.Entity;

import java.util.Set;


public class FeatureSerializer extends Serializer<FeatureSet.Feature> {

    @Override
    public void write(Kryo kryo, Output output, FeatureSet.Feature object) {
        kryo.writeObject(output, object.name);
        kryo.writeObject(output, object.type);
        kryo.writeClassAndObject(output, object.entities);
    }

    @Override
    @SuppressWarnings("unchecked")
    public FeatureSet.Feature read(Kryo kryo, Input input, Class<FeatureSet.Feature> t) {
        String name = kryo.readObject(input, String.class);
        Class type = kryo.readObject(input, Class.class);
        Set<Entity> entities = (Set<Entity>) kryo.readClassAndObject(input);

        return new FeatureSet.Feature(name, type, entities);
    }
}
