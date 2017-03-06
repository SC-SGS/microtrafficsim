package microtrafficsim.core.serialization.kryo.impl.features;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.map.features.MultiLine;
import microtrafficsim.core.map.features.Point;
import microtrafficsim.core.serialization.kryo.Utils;


public class PointSerializer extends Serializer<Point> {

    @Override
    public void write(Kryo kryo, Output output, Point object) {
        output.writeLong(object.id);
        output.writeDouble(object.coordinate.lat);
        output.writeDouble(object.coordinate.lon);
    }

    @Override
    public Point read(Kryo kryo, Input input, Class<Point> type) {
        return new Point(
                input.readLong(),
                new Coordinate(
                        input.readDouble(),
                        input.readDouble()
                )
        );
    }
}
