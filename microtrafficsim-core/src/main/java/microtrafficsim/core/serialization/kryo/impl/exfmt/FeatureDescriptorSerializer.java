package microtrafficsim.core.serialization.kryo.impl.exfmt;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import microtrafficsim.core.map.FeatureDescriptor;
import microtrafficsim.core.map.FeaturePrimitive;


public class FeatureDescriptorSerializer extends Serializer<FeatureDescriptor> {
    @Override
    public void write(Kryo kryo, Output output, FeatureDescriptor object) {
        kryo.writeObject(output, object.getName());
        kryo.writeObject(output, object.getType());
    }

    @Override
    @SuppressWarnings("unchecked")
    public FeatureDescriptor read(Kryo kryo, Input input, Class<FeatureDescriptor> t) {
        String name = kryo.readObject(input, String.class);
        Class type = kryo.readObject(input, Class.class);

        return new FeatureDescriptor(name, (Class<? extends FeaturePrimitive>) type);
    }
}
