package microtrafficsim.core.serialization.kryo.impl.exfmt;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import microtrafficsim.core.exfmt.base.TileGridSet;
import microtrafficsim.core.exfmt.ecs.Entity;
import microtrafficsim.core.map.tiles.TileRect;
import microtrafficsim.core.map.tiles.TilingScheme;
import microtrafficsim.utils.collections.Grid;

import java.util.Set;


public class TileGridSetSerializer {
    private TileGridSetSerializer() {}


    public static class TileGrid extends Serializer<TileGridSet.TileGrid> {

        @Override
        public void write(Kryo kryo, Output output, TileGridSet.TileGrid object) {
            kryo.writeClassAndObject(output, object.scheme);
            kryo.writeObject(output, object.level);
            kryo.writeClassAndObject(output, object.entities);
        }

        @Override
        @SuppressWarnings("unchecked")
        public TileGridSet.TileGrid read(Kryo kryo, Input input, Class<TileGridSet.TileGrid> type) {
            TilingScheme scheme = (TilingScheme) kryo.readClassAndObject(input);
            TileRect level = kryo.readObject(input, TileRect.class);
            Grid<Set<Entity>> entities = (Grid<Set<Entity>>) kryo.readClassAndObject(input);

            return new TileGridSet.TileGrid(scheme, level, entities);
        }
    }

    public static class Key extends Serializer<TileGridSet.Key> {

        @Override
        public void write(Kryo kryo, Output output, TileGridSet.Key object) {
            kryo.writeClassAndObject(output, object.scheme);
            kryo.writeObject(output, object.level);
        }

        @Override
        public TileGridSet.Key read(Kryo kryo, Input input, Class<TileGridSet.Key> type) {
            TilingScheme scheme = (TilingScheme) kryo.readClassAndObject(input);
            TileRect level = kryo.readObject(input, TileRect.class);

            return new TileGridSet.Key(scheme, level);
        }
    }
}
