package microtrafficsim.core.serialization.kryo.impl.exfmt.components;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import microtrafficsim.core.exfmt.ecs.Entity;
import microtrafficsim.core.exfmt.ecs.components.TileGridComponent;
import microtrafficsim.core.map.tiles.TileRect;
import microtrafficsim.core.map.tiles.TilingScheme;

import java.util.Set;


public class TileGridComponentSerializer extends Serializer<TileGridComponent> {

    @Override
    public void write(Kryo kryo, Output output, TileGridComponent object) {
        kryo.writeClassAndObject(output, object.getEntity());
        kryo.writeClassAndObject(output, object.getAll());
    }

    @Override
    @SuppressWarnings("unchecked")
    public TileGridComponent read(Kryo kryo, Input input, Class<TileGridComponent> type) {
        TileGridComponent component = new TileGridComponent(null);
        kryo.reference(component);

        component.setEntity((Entity) kryo.readClassAndObject(input));
        component.addAll((Set<TileGridComponent.Entry>) kryo.readClassAndObject(input));

        return component;
    }


    public static class Entry extends Serializer<TileGridComponent.Entry> {

        @Override
        public void write(Kryo kryo, Output output, TileGridComponent.Entry object) {
            kryo.writeClassAndObject(output, object.getScheme());
            kryo.writeObject(output, object.getLevel());
            kryo.writeObject(output, object.getX());
            kryo.writeObject(output, object.getY());
        }

        @Override
        public TileGridComponent.Entry read(Kryo kryo, Input input, Class<TileGridComponent.Entry> type) {
            TilingScheme scheme = (TilingScheme)  kryo.readClassAndObject(input);
            TileRect level = kryo.readObject(input, TileRect.class);
            int x = kryo.readObject(input, Integer.class);
            int y = kryo.readObject(input, Integer.class);
            return new TileGridComponent.Entry(scheme, level, x, y);
        }
    }
}
