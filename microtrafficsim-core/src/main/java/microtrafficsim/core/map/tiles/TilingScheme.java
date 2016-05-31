package microtrafficsim.core.map.tiles;

import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.math.Rect2d;
import microtrafficsim.math.Vec2d;
import microtrafficsim.math.Vec2i;


public interface TilingScheme {
    Projection getProjection();

    TileId getTile(Vec2d xy, double zoom);
    TileId getTile(Rect2d r);

    TileRect getTiles(Rect2d b, double zoom);
    TileRect getTiles(TileRect b, double zoom);
    TileRect getTiles(int tx, int ty, int tz, double zoom);

    Vec2d getPosition(int x, int y, int z);
    Rect2d getBounds(int x, int y, int z);
    Rect2d getBounds(TileRect tiles);

    Vec2i getTileSize();


    default TileId getTile(Coordinate c, double zoom) {
        return getTile(getProjection().project(c), zoom);
    }

    default TileId getTile(Bounds b) {
        return getTile(getProjection().project(b));
    }

    default TileRect getTiles(Bounds b, double zoom) {
        return getTiles(getProjection().project(b), zoom);
    }

    default TileRect getTiles(TileId tile, double zoom) {
        return getTiles(tile.x, tile.y, tile.z, zoom);
    }

    default Vec2d getPosition(TileId tile) {
        return getPosition(tile.x, tile.y, tile.z);
    }

    default Rect2d getBounds(TileId tile) {
        return getBounds(tile.x, tile.y, tile.z);
    }

    default Bounds getUnprojectedBounds(int x, int y, int z) {
        return getProjection().unproject(getBounds(x, y, z));
    }

    default Bounds getUnprojectedBounds(TileId tile) {
        return getProjection().unproject(getBounds(tile.x, tile.y, tile.z));
    }

    default Bounds getUnprojectedBounds(TileRect tiles) {
        return getProjection().unproject(getBounds(tiles));
    }
}
