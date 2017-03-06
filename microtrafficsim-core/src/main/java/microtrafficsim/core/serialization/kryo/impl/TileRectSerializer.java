package microtrafficsim.core.serialization.kryo.impl;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import microtrafficsim.core.map.tiles.TileRect;
import microtrafficsim.math.Rect2d;


public class TileRectSerializer extends Serializer<TileRect> {

    @Override
    public void write(Kryo kryo, Output output, TileRect object) {
        output.writeInt(object.xmin);
        output.writeInt(object.ymin);
        output.writeInt(object.xmax);
        output.writeInt(object.ymax);
        output.writeInt(object.zoom);
    }

    @Override
    public TileRect read(Kryo kryo, Input input, Class<TileRect> type) {
        return new TileRect(
                input.readInt(),
                input.readInt(),
                input.readInt(),
                input.readInt(),
                input.readInt()
        );
    }
}
