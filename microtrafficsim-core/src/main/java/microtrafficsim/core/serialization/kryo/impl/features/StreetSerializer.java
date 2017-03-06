package microtrafficsim.core.serialization.kryo.impl.features;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import microtrafficsim.core.entities.street.StreetEntity;
import microtrafficsim.core.map.features.Street;
import microtrafficsim.core.serialization.kryo.Utils;


public class StreetSerializer extends Serializer<Street> {

    @Override
    public void write(Kryo kryo, Output output, Street object) {
        output.writeLong(object.id);
        Utils.writeCoordinates(output, object.coordinates);
        output.writeDouble(object.layer);
        output.writeDouble(object.length);
        output.writeInt(object.distances.length);
        output.writeDoubles(object.distances);
        kryo.writeClassAndObject(output, object.getEntity());
    }

    @Override
    public Street read(Kryo kryo, Input input, Class<Street> type) {
        Street street = new Street(
                input.readLong(),
                Utils.readCoordinates(input),
                input.readDouble(),
                input.readDouble(),
                input.readDoubles(input.readInt())
        );

        street.setEntity((StreetEntity) kryo.readClassAndObject(input));
        return street;
    }
}
