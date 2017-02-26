package microtrafficsim.core.map.tiles;

import microtrafficsim.core.map.features.MultiLine;
import microtrafficsim.core.map.features.Point;
import microtrafficsim.core.map.features.Polygon;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.math.Rect2d;
import microtrafficsim.math.Vec2d;
import microtrafficsim.math.geometry.Lines;

import static microtrafficsim.math.MathUtils.clamp;


/**
 * Tile intersection tests for various geometry.
 *
 * @author Maximilian Luz
 */
public class TileIntersectors {
    private TileIntersectors() {}


    /**
     * Tests the given point projected using the given projection for intersection against the given tile.
     *
     * @param point      the point to test for intersection.
     * @param tile       the tile to test the point against.
     * @param projection the projection used to project the given point.
     * @return {@code true} if the projected point intersects with the given tile.
     */
    public static boolean intersect(Point point, Rect2d tile, Projection projection) {
        Vec2d c = projection.project(point.coordinate);
        return c.x >= tile.xmin && c.x <= tile.xmax && c.y >= tile.ymin && c.y <= tile.ymax;
    }

    /**
     * Tests the given line projected using the given projection for intersection against the given tile.
     *
     * @param line       the line to test for intersection.
     * @param tile       the tile to test the line against.
     * @param projection the projection used to project the given line.
     * @return {@code true} if the projected line intersects with the given tile.
     */
    public static boolean intersect(MultiLine line, Rect2d tile, Projection projection) {

        Vec2d a = projection.project(line.coordinates[0]);
        for (int i = 1; i < line.coordinates.length; i++) {
            Vec2d b = projection.project(line.coordinates[i]);

            // if completely out of bounds, continue
            if ((a.x < tile.xmin && b.x < tile.xmin) || (a.x > tile.xmax && b.x > tile.xmax)) continue;
            if ((a.y < tile.ymin && b.y < tile.ymin) || (a.y > tile.ymax && b.y > tile.ymax)) continue;

            // clamp line-segment to tile
            double cxa = clamp(a.x, tile.xmin, tile.xmax);
            double cxb = clamp(b.x, tile.xmin, tile.xmax);
            double cya = clamp(a.y, tile.ymin, tile.ymax);
            double cyb = clamp(b.y, tile.ymin, tile.ymax);

            // transform the clamped coordinates to line coordinates
            double sxa = (cxa - a.x) / (b.x - a.x);
            double sxb = (cxb - a.x) / (b.x - a.x);
            double sya = (cya - a.y) / (b.y - a.y);
            double syb = (cyb - a.y) / (b.y - a.y);

            // check if line-segments intersect
            if (sxb >= sxa && syb >= sya) return true;

            a = b;
        }

        return false;
    }

    /**
     * Tests the given polygon projected using the given projection for intersection against the given tile.
     *
     * @param polygon    the polygon to test for intersection.
     * @param tile       the tile to test the polygon against.
     * @param projection the projection used to project the given polygon.
     * @return {@code true} if the projected polygon intersects with the given tile.
     */
    public static boolean intersect(Polygon polygon, Rect2d tile, Projection projection) {
        Vec2d[] outline = new Vec2d[polygon.outline.length];
        Rect2d aabb = new Rect2d(Double.MAX_VALUE, Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);

        // calculate bounding box and project vertices
        for (int i = 0; i < outline.length; i++) {
            outline[i] = projection.project(polygon.outline[i]);

            if (aabb.xmin > outline[i].x) aabb.xmin = outline[i].x;
            if (aabb.xmax < outline[i].x) aabb.xmax = outline[i].x;
            if (aabb.ymin > outline[i].y) aabb.ymin = outline[i].y;
            if (aabb.ymax < outline[i].y) aabb.ymax = outline[i].y;
        }

        // check if bounding boxes intersect
        if (!aabb.intersects(tile)) {
            return false;
        }

        // check if any point of the polygon is inside of the AABB
        for (Vec2d v : outline) {
            if (tile.contains(v)) {
                return true;
            }
        }

        // check if any edge of the polygon intersects any edge of the AABB
        Vec2d pa = outline[outline.length - 1];
        for (Vec2d pb : outline) {
            Vec2d b0 = new Vec2d(tile.xmin, tile.ymin);
            Vec2d b1 = new Vec2d(tile.xmin, tile.ymax);
            Vec2d b2 = new Vec2d(tile.xmax, tile.ymax);
            Vec2d b3 = new Vec2d(tile.xmax, tile.ymin);

            if (Lines.segmentIntersectsNonCoincidental(pa, pb, b0, b1)) {
                return true;
            } else if (Lines.segmentIntersectsNonCoincidental(pa, pb, b1, b2)) {
                return true;
            } else if (Lines.segmentIntersectsNonCoincidental(pa, pb, b2, b3)) {
                return true;
            } else if (Lines.segmentIntersectsNonCoincidental(pa, pb, b3, b0)) {
                return true;
            }
        }

        return false;
    }
}
