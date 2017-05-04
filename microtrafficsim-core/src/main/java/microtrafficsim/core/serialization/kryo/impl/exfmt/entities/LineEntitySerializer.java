package microtrafficsim.core.serialization.kryo.impl.exfmt.entities;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import microtrafficsim.core.exfmt.ecs.Component;
import microtrafficsim.core.exfmt.ecs.entities.LineEntity;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.serialization.kryo.Utils;

import java.util.Map;


public class LineEntitySerializer extends Serializer<LineEntity> {

    @Override
    public void write(Kryo kryo, Output output, LineEntity object) {
        kryo.writeObject(output, object.getId());
        Utils.writeCoordinates(output, object.getCoordinates());
        kryo.writeClassAndObject(output, object.getAll());
    }

    @Override
    @SuppressWarnings("unchecked")
    public LineEntity read(Kryo kryo, Input input, Class<LineEntity> type) {
        long id = kryo.readObject(input, Long.class);
        Coordinate[] coordinates = Utils.readCoordinates(input);

        LineEntity entity = new LineEntity(id, coordinates);
        kryo.reference(entity);

        entity.getAll().putAll((Map<Class<? extends Component>, Component>) kryo.readClassAndObject(input));

        return entity;
    }
}
