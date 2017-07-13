package microtrafficsim.core.serialization.kryo.impl;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import microtrafficsim.core.map.MapProperties;


public class MapPropertiesSerializer extends Serializer<MapProperties> {

    @Override
    public void write(Kryo kryo, Output output, MapProperties object) {
        output.writeBoolean(object.drivingOnTheRight);
    }

    @Override
    public MapProperties read(Kryo kryo, Input input, Class<MapProperties> type) {
        final boolean drivingOnTheRight = input.readBoolean();
        return new MapProperties(drivingOnTheRight);
    }
}
