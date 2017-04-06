package microtrafficsim.core.serialization.kryo.impl.segments;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.map.Feature;
import microtrafficsim.core.map.FeaturePrimitive;
import microtrafficsim.core.map.MapSegment;
import microtrafficsim.core.map.tiles.QuadTreeTiledMapSegment;
import microtrafficsim.core.map.tiles.TileRect;
import microtrafficsim.core.map.tiles.TilingScheme;

import java.util.ArrayList;
import java.util.Map;


public class QuadTreeTiledMapSegmentSerializer extends Serializer<QuadTreeTiledMapSegment> {

    @Override
    public void write(Kryo kryo, Output output, QuadTreeTiledMapSegment object) {
        kryo.writeClassAndObject(output, object.getTilingScheme());
        kryo.writeObject(output, object.getBounds());
        kryo.writeObject(output, object.getLeafTiles());
        kryo.writeClassAndObject(output, object.getFeatureSet());
    }

    @Override
    @SuppressWarnings("unchecked")
    public QuadTreeTiledMapSegment read(Kryo kryo, Input input, Class<QuadTreeTiledMapSegment> type) {
        return new QuadTreeTiledMapSegment(
                (TilingScheme) kryo.readClassAndObject(input),
                kryo.readObject(input, Bounds.class),
                kryo.readObject(input, TileRect.class),
                (Map<String, QuadTreeTiledMapSegment.TileGroup<?>>) kryo.readClassAndObject(input)
        );
    }


    public static class TileGroup<T extends FeaturePrimitive> extends Serializer<QuadTreeTiledMapSegment.TileGroup<T>> {

        @Override
        public void write(Kryo kryo, Output output, QuadTreeTiledMapSegment.TileGroup<T> object) {
            kryo.writeObject(output, object.name);
            kryo.writeObject(output, object.type);
            kryo.writeObject(output, object.data);
        }

        @Override
        @SuppressWarnings("unchecked")
        public QuadTreeTiledMapSegment.TileGroup<T> read(Kryo kryo, Input input, Class<QuadTreeTiledMapSegment.TileGroup<T>> type) {
            return new QuadTreeTiledMapSegment.TileGroup<T>(
                    kryo.readObject(input, String.class),
                    kryo.readObject(input, Class.class),
                    kryo.readObject(input, QuadTreeTiledMapSegment.TileData.class)
            );
        }
    }


    public static class TileData<T extends FeaturePrimitive> extends Serializer<QuadTreeTiledMapSegment.TileData<T>> {

        @Override
        public void write(Kryo kryo, Output output, QuadTreeTiledMapSegment.TileData<T> object) {
            output.writeInt(object.xlen);
            output.writeInt(object.ylen);
            kryo.writeObject(output, object.data);
        }

        @Override
        @SuppressWarnings("unchecked")
        public QuadTreeTiledMapSegment.TileData<T> read(Kryo kryo, Input input, Class<QuadTreeTiledMapSegment.TileData<T>> type) {
            return new QuadTreeTiledMapSegment.TileData<T>(
                    input.readInt(),
                    input.readInt(),
                    kryo.readObject(input, ArrayList.class)
            );
        }
    }
}
