package microtrafficsim.core.serialization.kryo.impl.segments;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.map.Feature;
import microtrafficsim.core.map.MapSegment;

import java.util.Map;


public class MapSegmentSerializer extends Serializer<MapSegment> {

    @Override
    public void write(Kryo kryo, Output output, MapSegment object) {
        kryo.writeObject(output, object.getBounds());
        kryo.writeClassAndObject(output, object.getFeatures());
    }

    @Override
    @SuppressWarnings("unchecked")
    public MapSegment read(Kryo kryo, Input input, Class<MapSegment> type) {
        return new MapSegment(
                kryo.readObject(input, Bounds.class),
                (Map<String, Feature<?>>) kryo.readClassAndObject(input)
        );
    }
}
