package microtrafficsim.core.serialization.kryo.impl.features;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import microtrafficsim.core.map.features.MultiLine;
import microtrafficsim.core.serialization.kryo.Utils;


public class MultiLineSerializer extends Serializer<MultiLine> {

    @Override
    public void write(Kryo kryo, Output output, MultiLine object) {
        output.writeLong(object.id);
        Utils.writeCoordinates(output, object.coordinates);
    }

    @Override
    public MultiLine read(Kryo kryo, Input input, Class<MultiLine> type) {
        return new MultiLine(
                input.readLong(),
                Utils.readCoordinates(input)
        );
    }
}
