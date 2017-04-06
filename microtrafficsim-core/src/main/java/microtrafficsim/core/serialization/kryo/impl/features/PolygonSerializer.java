package microtrafficsim.core.serialization.kryo.impl.features;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import microtrafficsim.core.map.features.Polygon;
import microtrafficsim.core.serialization.kryo.Utils;


public class PolygonSerializer extends Serializer<Polygon> {

    @Override
    public void write(Kryo kryo, Output output, Polygon object) {
        output.writeLong(object.id);
        Utils.writeCoordinates(output, object.outline);
    }

    @Override
    public Polygon read(Kryo kryo, Input input, Class<Polygon> type) {
        return new Polygon(
                input.readLong(),
                Utils.readCoordinates(input)
        );
    }
}
