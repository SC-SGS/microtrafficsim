package microtrafficsim.core.serialization.kryo.impl;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import microtrafficsim.core.map.Feature;
import microtrafficsim.core.map.FeaturePrimitive;


public class FeatureSerializer<T extends FeaturePrimitive> extends Serializer<Feature<T>> {

    @Override
    public void write(Kryo kryo, Output output, Feature<T> object) {
        kryo.writeObject(output, object.getName());
        kryo.writeObject(output, object.getType());
        kryo.writeClassAndObject(output, object.getData());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Feature<T> read(Kryo kryo, Input input, Class<Feature<T>> type) {
        return new Feature<T>(
                kryo.readObject(input, String.class),
                kryo.readObject(input, Class.class),
                (T[]) kryo.readClassAndObject(input)
        );
    }
}
