package map.tiles;

import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.map.features.MultiLine;
import microtrafficsim.core.map.tiles.TileIntersectors;
import microtrafficsim.core.vis.map.projections.MercatorProjection;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.math.Rect2d;
import org.junit.Test;

import static org.junit.Assert.assertTrue;


/**
 * Test for the QuadTree tiling scheme.
 *
 * @author Maximilian Luz
 */
public class TileIntersectorTest {

    @Test
    public void testMultilineStandard() {
        Projection projection = new MercatorProjection();
        Rect2d tile = projection.getProjectedMaximumBounds();

        MultiLine line = new MultiLine(0, new Coordinate[]{
                new Coordinate(9.430237891928732, 48.93995315),
                new Coordinate(9.429912203478601, 48.93995625),
        });

        assertTrue(TileIntersectors.intersect(line, tile, projection));
    }

    @Test
    public void testMultilineHorizontal() {
        Projection projection = new MercatorProjection();
        Rect2d tile = projection.getProjectedMaximumBounds();

        MultiLine line = new MultiLine(0, new Coordinate[]{
                new Coordinate(9.430237891928732, 48.93995315),
                new Coordinate(9.430237891928732, 48.93995625),
        });

        assertTrue(TileIntersectors.intersect(line, tile, projection));
    }

    @Test
    public void testMultilineVertical() {
        Projection projection = new MercatorProjection();
        Rect2d tile = projection.getProjectedMaximumBounds();

        MultiLine line = new MultiLine(0, new Coordinate[]{
                new Coordinate(9.429930552131852, 48.94107999999999),
                new Coordinate(9.429536055871896, 48.94107999999999),
        });

        assertTrue(TileIntersectors.intersect(line, tile, projection));
    }
}
