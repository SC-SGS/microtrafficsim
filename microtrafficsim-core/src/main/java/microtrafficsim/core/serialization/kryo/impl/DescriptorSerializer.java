package microtrafficsim.core.serialization.kryo.impl;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import microtrafficsim.utils.Descriptor;

/**
 * @author Dominic Parga Cacheiro
 */
public class DescriptorSerializer extends Serializer<Descriptor<?>> {

    @Override
    public void write(Kryo kryo, Output output, Descriptor<?> object) {
        kryo.writeClassAndObject(output, object.getObj());
        kryo.writeObject(output, object.getDescription());
    }

    @Override
    public Descriptor<?> read(Kryo kryo, Input input, Class<Descriptor<?>> type) {
        Object obj = kryo.readClassAndObject(input);
        String str = kryo.readObject(input, String.class);
        return new Descriptor<>(obj, str);
    }
}
