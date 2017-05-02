package microtrafficsim.core.serialization.kryo.impl.exfmt.entities;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import microtrafficsim.core.exfmt.ecs.Component;
import microtrafficsim.core.exfmt.ecs.entities.PolygonEntity;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.serialization.kryo.Utils;

import java.util.Map;


public class PolygonEntitySerializer extends Serializer<PolygonEntity> {

    @Override
    public void write(Kryo kryo, Output output, PolygonEntity object) {
        kryo.writeObject(output, object.getId());
        Utils.writeCoordinates(output, object.getOutline());
        kryo.writeClassAndObject(output, object.getAll());
    }

    @Override
    @SuppressWarnings("unchecked")
    public PolygonEntity read(Kryo kryo, Input input, Class<PolygonEntity> type) {
        long id = kryo.readObject(input, Long.class);
        Coordinate[] outline = Utils.readCoordinates(input);

        PolygonEntity entity = new PolygonEntity(id, outline);
        kryo.reference(entity);

        entity.getAll().putAll((Map<Class<? extends Component>, Component>) kryo.readClassAndObject(input));

        return entity;
    }
}
