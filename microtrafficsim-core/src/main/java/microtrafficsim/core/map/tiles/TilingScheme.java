package microtrafficsim.core.map.tiles;

import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.math.Rect2d;
import microtrafficsim.math.Vec2d;
import microtrafficsim.math.Vec2i;


/**
 * Scheme to describe a tile layout.
 *
 * @author Maximilian Luz
 */
public interface TilingScheme {

    /**
     * Returns the projection used in this tiling-scheme.
     *
     * @return the projection used in this tiling-scheme.
     */
    Projection getProjection();


    /**
     * Returns the tile at the given zoom-level in which the given point lies.
     *
     * @param xy   the point to return the tile for.
     * @param zoom the zoom-level for which the tile should be returned for.
     * @return the tile containing the given point at the given zoom-level.
     */
    TileId getTile(Vec2d xy, double zoom);

    /**
     * Returns the tile at the given zoom-level in which the given coordinate lies.
     *
     * @param c    the coordinate to return the tile for.
     * @param zoom the zoom-level for which the tile should be returned for.
     * @return the tile containing the given coordinate at the given zoom-level.
     */
    default TileId getTile(Coordinate c, double zoom) {
        return getTile(getProjection().project(c), zoom);
    }

    /**
     * Returns the single tile best fitting the given rectangle.
     *
     * @param r the rectangle for which the (best fitting single) tile should be returned for.
     * @return the best fitting single tile containing the given rectangle.
     */
    TileId getTile(Rect2d r);

    /**
     * Returns the single tile best fitting the given bounds.
     *
     * @param b the bounds for which the (best fitting single) tile should be returned for.
     * @return the best fitting single tile containing the given bounds.
     */
    default TileId getTile(Bounds b) {
        return getTile(getProjection().project(b));
    }


    /**
     * Returns the tile-rectangle covering the given rectangle at the given zoom level.
     *
     * @param r    the rectangle for which the tiles should be returned.
     * @param zoom the zoom-level for which the tiles should be returned.
     * @return the tile-rectangle covering the given rectangle at the given zoom level.
     */
    TileRect getTiles(Rect2d r, double zoom);

    /**
     * Returns the tile-rectangle covering the given rectangle at the given zoom level.
     *
     * @param r    the rectangle for which the tiles at the given zoom-level should be returned.
     * @param zoom the zoom-level for which the tiles should be returned.
     * @return the tile-rectangle covering the given rectangle at the given zoom level.
     */
    TileRect getTiles(TileRect r, double zoom);

    /**
     * Returns the tile-rectangle covering the given bounds at the given zoom level.
     *
     * @param b    the bounds for which the tiles at the given zoom-level should be returned.
     * @param zoom the zoom-level for which the tiles should be returned.
     * @return the tile-rectangle covering the given bounds at the given zoom level.
     */
    default TileRect getTiles(Bounds b, double zoom) {
        return getTiles(getProjection().project(b), zoom);
    }

    /**
     * Returns the tile-rectangle covering the given tile at the given zoom level.
     *
     * @param tx   the x-component of the tile-id.
     * @param ty   the y-component of the tile-id.
     * @param tz   the z-component of the tile-id.
     * @param zoom the zoom-level for which the tiles should be returned.
     * @return the tile-rectangle covering the given rectangle at the given zoom level.
     */
    TileRect getTiles(int tx, int ty, int tz, double zoom);

    /**
     * Returns the tile-rectangle covering the given tile at the given zoom level.
     *
     * @param tile the tile for which the rectangle should be returned.
     * @param zoom the zoom-level for which the tiles should be returned.
     * @return the tile-rectangle covering the given rectangle at the given zoom level.
     */
    default TileRect getTiles(TileId tile, double zoom) {
        return getTiles(tile.x, tile.y, tile.z, zoom);
    }


    /**
     * Returns the position of the given tile. See the implementation for the actual point on the tile corresponding
     * to the returned position.
     *
     * @param x the x-component of the tile-id.
     * @param y the y-component of the tile-id.
     * @param z the z-component of the tile-id.
     * @return the position of the tile.
     */
    Vec2d getPosition(int x, int y, int z);

    /**
     * Returns the position of the given tile. See the implementation for the actual point on the tile corresponding
     * to the returned position.
     *
     * @param tile the tile for which the position should be returned.
     * @return the position of the tile.
     */
    default Vec2d getPosition(TileId tile) {
        return getPosition(tile.x, tile.y, tile.z);
    }


    /**
     * Returns the bounds of the given tile.
     *
     * @param x the x-component of the tile-id.
     * @param y the y-component of the tile-id.
     * @param z the z-component of the tile-id.
     * @return the bounds of the given tile.
     */
    Rect2d getBounds(int x, int y, int z);

    /**
     * Returns the bounds of the given tile.
     *
     * @param tile the tile for which the bounds should be returned.
     * @return the bounds of the given tile.
     */
    default Rect2d getBounds(TileId tile) {
        return getBounds(tile.x, tile.y, tile.z);
    }

    /**
     * Returns the bounds of the given tile-rectangle
     *
     * @param tiles the tile-rectangle.
     * @return the bounds of the given tile-rectangle.
     */
    Rect2d getBounds(TileRect tiles);


    /**
     * Returns the size of the tiles in pixels.
     *
     * @return the size of the tiles in pixels.
     */
    Vec2i getTileSize();


    /**
     * Returns the un-projected bounds of the given tile.
     *
     * @param x the x-component of the tile-id.
     * @param y the y-component of the tile-id.
     * @param z the z-component of the tile-id.
     * @return the bounds of the given tile.
     */
    default Bounds getUnprojectedBounds(int x, int y, int z) {
        return getProjection().unproject(getBounds(x, y, z));
    }

    /**
     * Returns the un-projected bounds of the given tile.
     *
     * @param tile the tile to return the bounds for.
     * @return the bounds of the given tile.
     */
    default Bounds getUnprojectedBounds(TileId tile) {
        return getProjection().unproject(getBounds(tile.x, tile.y, tile.z));
    }
}
