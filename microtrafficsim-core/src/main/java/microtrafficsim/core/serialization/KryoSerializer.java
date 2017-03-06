package microtrafficsim.core.serialization;

import com.esotericsoftware.kryo.Kryo;
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
import microtrafficsim.core.serialization.kryo.impl.features.*;
import microtrafficsim.core.serialization.kryo.impl.segments.*;
import microtrafficsim.core.vis.map.projections.MercatorProjection;
import microtrafficsim.core.vis.map.projections.PlateCarreeProjection;
import microtrafficsim.math.Rect2d;


public class KryoSerializer {

    public static Kryo create() {
        Kryo kryo = new Kryo();

        // NOTE: any change of the following statements breaks file-compatibility
        //       with the exception of appending new serializers

        kryo.register(Coordinate.class, new CoordinateSerializer());
        kryo.register(Bounds.class, new BoundsSerializer());

        kryo.register(PlateCarreeProjection.class);
        kryo.register(MercatorProjection.class);

        kryo.register(Feature.class, new FeatureSerializer());
        kryo.register(Point.class, new PointSerializer());
        kryo.register(MultiLine.class, new MultiLineSerializer());
        kryo.register(Street.class, new StreetSerializer());
        kryo.register(Polygon.class, new PolygonSerializer());

        kryo.register(Rect2d.class, new Rect2dSerializer());

        kryo.register(TileRect.class, new TileRectSerializer());

        kryo.register(MapSegment.class, new MapSegmentSerializer());
        kryo.register(QuadTreeTiledMapSegment.class, new QuadTreeTiledMapSegmentSerializer());
        kryo.register(QuadTreeTiledMapSegment.TileGroup.class, new QuadTreeTiledMapSegmentSerializer.TileGroup());
        kryo.register(QuadTreeTiledMapSegment.TileData.class, new QuadTreeTiledMapSegmentSerializer.TileData());

        kryo.register(QuadTreeTilingScheme.class, new QuadTreeTilingSchemeSerializer());

        return kryo;
    }

}
