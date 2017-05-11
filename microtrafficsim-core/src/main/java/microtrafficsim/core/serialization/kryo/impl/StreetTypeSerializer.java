package microtrafficsim.core.serialization.kryo.impl;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import microtrafficsim.core.map.StreetType;


public class StreetTypeSerializer extends Serializer<StreetType> {

    @Override
    public void write(Kryo kryo, Output output, StreetType object) {
        kryo.writeObject(output, object.getBits());
    }

    @Override
    public StreetType read(Kryo kryo, Input input, Class<StreetType> type) {
        return new StreetType(kryo.readObject(input, Integer.class));
    }
}
