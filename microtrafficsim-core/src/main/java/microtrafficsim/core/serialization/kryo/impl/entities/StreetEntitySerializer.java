package microtrafficsim.core.serialization.kryo.impl.entities;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import microtrafficsim.core.entities.street.LogicStreetEntity;
import microtrafficsim.core.entities.street.StreetEntity;
import microtrafficsim.core.map.features.Street;


public class StreetEntitySerializer extends Serializer<StreetEntity> {

    @Override
    public void write(Kryo kryo, Output output, StreetEntity object) {
        kryo.writeClassAndObject(output, object.getForwardEdge());
        kryo.writeClassAndObject(output, object.getBackwardEdge());
        kryo.writeClassAndObject(output, object.getGeometry());
    }

    @Override
    public StreetEntity read(Kryo kryo, Input input, Class<StreetEntity> type) {
        kryo.reference(new StreetEntity(null, null, null));     // use dummy-entity to parse back-references

        LogicStreetEntity forward = (LogicStreetEntity) kryo.readClassAndObject(input);
        LogicStreetEntity backward = (LogicStreetEntity) kryo.readClassAndObject(input);
        Street geometry = (Street) kryo.readClassAndObject(input);

        StreetEntity entity = new StreetEntity(forward, backward, geometry);
        forward.setEntity(entity);
        backward.setEntity(entity);
        geometry.setEntity(entity);

        return entity;
    }
}
