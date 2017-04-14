package microtrafficsim.core.serialization.kryo.impl.exfmt.entities;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import microtrafficsim.core.exfmt.ecs.Component;
import microtrafficsim.core.exfmt.ecs.entities.PointEntity;
import microtrafficsim.core.map.Coordinate;

import java.util.Map;


public class PointEntitySerializer extends Serializer<PointEntity> {

    @Override
    public void write(Kryo kryo, Output output, PointEntity object) {
        kryo.writeObject(output, object.getId());
        kryo.writeObject(output, object.getCoordinate());
        kryo.writeClassAndObject(output, object.getAll());
    }

    @Override
    @SuppressWarnings("unchecked")
    public PointEntity read(Kryo kryo, Input input, Class<PointEntity> type) {
        long id = kryo.readObject(input, Long.class);
        Coordinate coordinate = kryo.readObject(input, Coordinate.class);

        PointEntity entity = new PointEntity(id, coordinate);
        kryo.reference(entity);

        entity.getAll().putAll((Map<Class<? extends Component>, Component>) kryo.readClassAndObject(input));

        return entity;
    }
}
