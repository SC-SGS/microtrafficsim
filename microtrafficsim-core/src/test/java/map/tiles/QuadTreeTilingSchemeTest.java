package map.tiles;

import microtrafficsim.core.map.tiles.QuadTreeTilingScheme;
import microtrafficsim.core.map.tiles.TileId;
import microtrafficsim.core.vis.map.projections.MercatorProjection;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.math.Rect2d;
import microtrafficsim.math.Vec2d;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


/**
 * Test for the QuadTree tiling scheme.
 *
 * @author Maximilian Luz
 */
public class QuadTreeTilingSchemeTest {

    private static final int    LEVEL_MIN            = 0;
    private static final int    LEVEL_MAX            = 19;
    private static final double PROJECTION_SCALE     = 256;
    private static final Projection PROJECTION       = new MercatorProjection(PROJECTION_SCALE);
    private static final QuadTreeTilingScheme SCHEME = new QuadTreeTilingScheme(PROJECTION, LEVEL_MIN, LEVEL_MAX);


    /**
     * Tests the getTile(Vec2d position, double zoom) method.
     */
    @Test
    public void testGetTile() {
        // TODO: more test-cases
        Vec2d  pos  = new Vec2d(PROJECTION_SCALE, -PROJECTION_SCALE);
        TileId tile = SCHEME.getTile(pos, 10);
        assertEquals(new TileId(1024, 1024, 10), tile);
    }

    /**
     * Tests the getBounds(TileId id) method.
     */
    @Test
    public void testGetBounds() {
        // TODO: more test-cases
        TileId tile   = new TileId(1023, 1023, 10);
        Rect2d bounds = SCHEME.getBounds(tile);
        assertEquals(new Rect2d(255.5, -256, 256, -255.5), bounds);
    }

    // TODO: add missing tests
}
