package microtrafficsim.core.map.tiles;

import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.map.features.MultiLine;
import microtrafficsim.core.map.features.Point;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.math.Rect2d;
import microtrafficsim.math.Vec2d;
import microtrafficsim.math.Vec2f;


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

            // get x-axis coverage
            double cxmin, cxmax;
            {
                double min = Math.min(a.x, b.x);
                double max = Math.max(a.x, b.x);

                if (max < tile.xmin || min > tile.xmax)
                    continue;

                cxmin = Math.max(min, tile.xmin);
                cxmax = Math.min(max, tile.xmax);
            }

            // get y-axis coverage
            double cymin, cymax;
            {
                double min = Math.min(a.y, b.y);
                double max = Math.max(a.y, b.y);

                if (max < tile.ymin || min > tile.ymax)
                    continue;

                cymin = Math.max(min, tile.ymin);
                cymax = Math.min(max, tile.ymax);
            }

            // transform x-axis-coverage to line-coverage
            double sxmin, sxmax;
            {
                double abx = b.x - a.x;
                double min = clamp((cxmin - a.x) / abx, 0, 1);
                double max = clamp((cxmax - a.x) / abx, 0, 1);

                sxmin = Math.min(min, max);
                sxmax = Math.max(min, max);
            }

            // transform y-axis-coverage to line-coverage
            double symin, symax;
            {
                double aby = b.y - a.y;
                double min = clamp((cymin - a.y) / aby, 0, 1);
                double max = clamp((cymax - a.y) / aby, 0, 1);

                symin = Math.min(min, max);
                symax = Math.max(min, max);
            }

            // if line-coverages overlap, the line intersects with the rectangle
            if (sxmax >= symin && symax >= sxmin)
                return true;

            a = b;
        }

        return false;
    }


    private static double clamp(double val, double min, double max) {
        return val < min ? min : val > max ? max : val;
    }
}
