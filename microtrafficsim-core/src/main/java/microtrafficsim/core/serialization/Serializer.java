package microtrafficsim.core.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import microtrafficsim.core.entities.street.StreetEntity;
import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.map.Feature;
import microtrafficsim.core.map.MapSegment;
import microtrafficsim.core.map.features.MultiLine;
import microtrafficsim.core.map.features.Point;
import microtrafficsim.core.map.features.Polygon;
import microtrafficsim.core.map.features.Street;
import microtrafficsim.core.map.tiles.QuadTreeTiledMapSegment;
import microtrafficsim.core.map.tiles.QuadTreeTilingScheme;
import microtrafficsim.core.map.tiles.TileRect;
import microtrafficsim.core.serialization.kryo.impl.*;
import microtrafficsim.core.serialization.kryo.impl.entities.StreetEntitySerializer;
import microtrafficsim.core.serialization.kryo.impl.features.MultiLineSerializer;
import microtrafficsim.core.serialization.kryo.impl.features.PointSerializer;
import microtrafficsim.core.serialization.kryo.impl.features.PolygonSerializer;
import microtrafficsim.core.serialization.kryo.impl.features.StreetSerializer;
import microtrafficsim.core.serialization.kryo.impl.segments.MapSegmentSerializer;
import microtrafficsim.core.serialization.kryo.impl.segments.QuadTreeTiledMapSegmentSerializer;
import microtrafficsim.core.vis.map.projections.MercatorProjection;
import microtrafficsim.core.vis.map.projections.PlateCarreeProjection;
import microtrafficsim.math.Rect2d;

import java.io.*;


public class Serializer {
    public static final Version VERSION = new Version(0, 1, 0);

    private final Kryo kryo;


    public static Serializer create() {
        return new Serializer(createKryo());
    }

    public Serializer(Kryo kryo) {
        this.kryo = kryo;
    }


    public void write(File file, Container container) throws IOException {
        try (OutputStream os = new FileOutputStream(file)) {
            write(os, container);
        }
    }

    public void write(OutputStream os, Container container) throws IOException {
        Output out = new Output(os);
        kryo.writeClassAndObject(out, container);
        out.flush();
    }


    public Container read(File file) throws IOException {
        try (InputStream is = new FileInputStream(file)) {
            return read(is);
        }
    }

    public Container read(InputStream is) throws IOException {
        return (Container) kryo.readClassAndObject(new Input(is));
    }


    private static Kryo createKryo() {
        Kryo kryo = new Kryo();

        // NOTE: any change of the following statements breaks file-compatibility
        //       with the exception of appending new serializers

        kryo.register(Version.class, new VersionSerializer());
        kryo.register(Container.class);

        kryo.register(Coordinate.class, new CoordinateSerializer());
        kryo.register(Bounds.class, new BoundsSerializer());

        kryo.register(Rect2d.class, new Rect2dSerializer());

        kryo.register(TileRect.class, new TileRectSerializer());

        kryo.register(PlateCarreeProjection.class);
        kryo.register(MercatorProjection.class);

        kryo.register(QuadTreeTilingScheme.class, new QuadTreeTilingSchemeSerializer());

        kryo.register(Feature.class, new FeatureSerializer());
        kryo.register(Point.class, new PointSerializer());
        kryo.register(MultiLine.class, new MultiLineSerializer());
        kryo.register(Street.class, new StreetSerializer());
        kryo.register(Polygon.class, new PolygonSerializer());

        kryo.register(StreetEntity.class, new StreetEntitySerializer());

        kryo.register(MapSegment.class, new MapSegmentSerializer());
        kryo.register(QuadTreeTiledMapSegment.class, new QuadTreeTiledMapSegmentSerializer());
        kryo.register(QuadTreeTiledMapSegment.TileGroup.class, new QuadTreeTiledMapSegmentSerializer.TileGroup());
        kryo.register(QuadTreeTiledMapSegment.TileData.class, new QuadTreeTiledMapSegmentSerializer.TileData());

        // kryo.register(StreetGraph.class, new StreetGraphSerializer());

        return kryo;
    }
}
