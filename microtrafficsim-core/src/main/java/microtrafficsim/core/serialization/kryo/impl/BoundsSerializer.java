package microtrafficsim.core.serialization.kryo.impl;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.map.Coordinate;


public class BoundsSerializer extends Serializer<Bounds> {

    @Override
    public void write(Kryo kryo, Output output, Bounds object) {
        output.writeDouble(object.minlat);
        output.writeDouble(object.minlon);
        output.writeDouble(object.maxlat);
        output.writeDouble(object.maxlon);
    }

    @Override
    public Bounds read(Kryo kryo, Input input, Class<Bounds> type) {
        return new Bounds(
                input.readDouble(),
                input.readDouble(),
                input.readDouble(),
                input.readDouble()
        );
    }
}
