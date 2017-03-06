package microtrafficsim.core.serialization.kryo.impl;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import microtrafficsim.core.map.Coordinate;


public class CoordinateSerializer extends Serializer<Coordinate> {

    @Override
    public void write(Kryo kryo, Output output, Coordinate object) {
        output.writeDouble(object.lat);
        output.writeDouble(object.lon);
    }

    @Override
    public Coordinate read(Kryo kryo, Input input, Class<Coordinate> type) {
        return new Coordinate(
                input.readDouble(),
                input.readDouble()
        );
    }
}
