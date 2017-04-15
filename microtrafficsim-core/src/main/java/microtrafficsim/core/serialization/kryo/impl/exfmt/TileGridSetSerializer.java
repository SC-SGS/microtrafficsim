package microtrafficsim.core.serialization.kryo.impl.exfmt;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import microtrafficsim.core.exfmt.base.TileGridInfo;
import microtrafficsim.core.exfmt.ecs.Entity;
import microtrafficsim.core.map.tiles.TileRect;
import microtrafficsim.core.map.tiles.TilingScheme;
import microtrafficsim.utils.collections.Grid;

import java.util.Set;


public class TileGridSetSerializer {
    private TileGridSetSerializer() {}


    public static class Grid extends Serializer<TileGridInfo.Grid> {

        @Override
        public void write(Kryo kryo, Output output, TileGridInfo.Grid object) {
            kryo.writeClassAndObject(output, object.scheme);
            kryo.writeObject(output, object.level);
        }

        @Override
        public TileGridInfo.Grid read(Kryo kryo, Input input, Class<TileGridInfo.Grid> type) {
            TilingScheme scheme = (TilingScheme) kryo.readClassAndObject(input);
            TileRect level = kryo.readObject(input, TileRect.class);

            return new TileGridInfo.Grid(scheme, level);
        }
    }
}
