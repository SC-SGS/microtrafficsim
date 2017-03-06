package microtrafficsim.core.serialization.kryo.impl;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import microtrafficsim.core.map.Bounds;
import microtrafficsim.math.Rect2d;


public class Rect2dSerializer extends Serializer<Rect2d> {

    @Override
    public void write(Kryo kryo, Output output, Rect2d object) {
        output.writeDouble(object.xmin);
        output.writeDouble(object.ymin);
        output.writeDouble(object.xmax);
        output.writeDouble(object.ymax);
    }

    @Override
    public Rect2d read(Kryo kryo, Input input, Class<Rect2d> type) {
        return new Rect2d(
                input.readDouble(),
                input.readDouble(),
                input.readDouble(),
                input.readDouble()
        );
    }
}
