package microtrafficsim.core.map.tiles;

import microtrafficsim.core.map.features.MultiLine;
import microtrafficsim.core.map.features.Point;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.math.Rect2d;
import microtrafficsim.math.Vec2d;

import static microtrafficsim.math.MathUtils.clamp;


public class TileIntersectors {
    private TileIntersectors() {}


    public static boolean intersect(Point point, Rect2d tile, Projection projection) {
        Vec2d c = projection.project(point.coordinate);

        return c.x >= tile.xmin && c.x <= tile.xmax
                && c.y >= tile.ymin && c.y <= tile.ymax;

    }

    public static boolean intersect(MultiLine line, Rect2d tile, Projection projection) {

        Vec2d a = projection.project(line.coordinates[0]);
        for (int i = 1; i < line.coordinates.length; i++) {
            Vec2d b = projection.project(line.coordinates[i]);

            // if completely out of bounds, continue
            if ((a.x < tile.xmin && b.x < tile.xmin) || (a.x > tile.xmax && b.x > tile.xmax))
                continue;
            if ((a.y < tile.ymin && b.y < tile.ymin) || (a.y > tile.ymax && b.y > tile.ymax))
                continue;

            // clamp to tile
            double cxa = clamp(a.x, tile.xmin, tile.xmax);
            double cxb = clamp(b.x, tile.xmin, tile.xmax);
            double cya = clamp(a.y, tile.ymin, tile.ymax);
            double cyb = clamp(b.y, tile.ymin, tile.ymax);

            // transform to line coordinates
            double sxa = (cxa - a.x) / (b.x - a.x);
            double sxb = (cxb - a.x) / (b.x - a.x);
            double sya = (cya - a.y) / (b.y - a.y);
            double syb = (cyb - a.y) / (b.y - a.y);

            // check if line-segments intersect
            if (sxb >= sxa && syb >= sya)
                return true;

            a = b;
        }

        return false;
    }
}
