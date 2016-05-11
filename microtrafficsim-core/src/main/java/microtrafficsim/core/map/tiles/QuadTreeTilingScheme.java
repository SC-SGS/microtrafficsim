package microtrafficsim.core.map.tiles;

import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.math.Rect2d;
import microtrafficsim.math.Vec2d;
import microtrafficsim.utils.hashing.FNVHashBuilder;

import java.util.Objects;


public class QuadTreeTilingScheme implements TilingScheme {

    private Projection projection;
    private int minlevel;
    private int maxlevel;

    public QuadTreeTilingScheme(Projection projection, int minlevel, int maxlevel) {
        this.projection = projection;
        this.minlevel = minlevel;
        this.maxlevel = maxlevel;
    }


    @Override
    public TileId getTile(Vec2d xy, double zoom) {
        Rect2d max = projection.getProjectedMaximumBounds();

        int level = clamp((int) Math.ceil(zoom), minlevel, maxlevel);
        int tiles = 1 << level;

        return new TileId(
                (int) (((xy.x - max.xmin) / (max.xmax - max.xmin)) * tiles),
                (int) ((1.0 - (xy.y - max.ymin) / (max.ymax - max.ymin)) * tiles),
                level
        );
    }

    @Override
    public TileId getTile(Rect2d r) {
        for (int z = maxlevel; z >= minlevel; z--) {
            TileRect bounds = getTiles(r, z);
            if (bounds.xmin == bounds.xmax && bounds.ymin == bounds.ymax)
                return new TileId(bounds.xmin, bounds.ymin, bounds.zoom);
        }

        return null;
    }


    @Override
    public TileRect getTiles(TileRect b, double zoom) {
        int level = clamp((int) Math.ceil(zoom), minlevel, maxlevel);

        if (level > b.zoom) {           // child tiles
            int diff = level - b.zoom;
            return new TileRect(
                    b.xmin << diff,
                    b.ymin << diff,
                    (b.xmax << diff) + (1 << diff) - 1,
                    (b.ymax << diff) + (1 << diff) - 1,
                    level
            );

        } else if (level < b.zoom) {    // parent tiles
            int ptiles = 1 << level;
            int ctiles = 1 << b.zoom;

            int xmin = (int) ((b.xmin / (double) ctiles) * ptiles);
            int xmax = (int) ((b.xmax / (double) ctiles) * ptiles);
            int ymin = (int) ((b.ymin / (double) ctiles) * ptiles);
            int ymax = (int) ((b.ymax / (double) ctiles) * ptiles);

            return new TileRect(
                    clamp(xmin, 0, ptiles - 1),
                    clamp(xmax, 0, ptiles - 1),
                    clamp(ymin, 0, ptiles - 1),
                    clamp(ymax, 0, ptiles - 1),
                    level
            );

        } else {
            return new TileRect(b);
        }
    }

    @Override
    public TileRect getTiles(int tx, int ty, int tz, double zoom) {
        int level = clamp((int) Math.ceil(zoom), minlevel, maxlevel);

        if (level > tz) {               // child tiles
            int diff = level - tz;
            return new TileRect(
                    tx << diff,
                    ty << diff,
                    (tx << diff) + (1 << diff) - 1,
                    (ty << diff) + (1 << diff) - 1,
                    level
            );

        } else if (level < tz) {        // parent tile
            int ptiles = 1 << level;
            int ctiles = 1 << tz;
            int x = clamp((int) ((tx / (double) ctiles) * ptiles), 0, ptiles - 1);
            int y = clamp((int) ((ty / (double) ctiles) * ptiles), 0, ptiles - 1);
            return new TileRect(x, y, x, y, level);

        } else {
            return new TileRect(tx, ty, tx, ty, level);
        }
    }

    @Override
    public TileRect getTiles(Rect2d b, double zoom) {
        if (b == null) return null;
        Rect2d max = projection.getProjectedMaximumBounds();

        int level = clamp((int) Math.ceil(zoom), minlevel, maxlevel);
        int tiles = 1 << level;

        int xmin = (int) (((b.xmin - max.xmin) / (max.xmax - max.xmin)) * tiles);
        int ymin = (int) ((1.0 - (b.ymax- max.ymin) / (max.ymax - max.ymin)) * tiles);
        int xmax = (int) (((b.xmax - max.xmin) / (max.xmax - max.xmin)) * tiles);
        int ymax = (int) ((1.0 - (b.ymin- max.ymin) / (max.ymax - max.ymin)) * tiles);

        return new TileRect(
                clamp(xmin, 0, tiles - 1),
                clamp(ymin, 0, tiles - 1),
                clamp(xmax, 0, tiles - 1),
                clamp(ymax, 0, tiles - 1),
                level
        );
    }


    public TileRect getLeafTiles(TileId tile) {
        return getLeafTiles(tile.x, tile.y, tile.z);
    }

    public TileRect getLeafTiles(int x, int y, int z) {
        int diff = maxlevel - z;

        return new TileRect(
                x << diff,
                y << diff,
                (x << diff) + (1 << diff) - 1,
                (y << diff) + (1 << diff) - 1,
                maxlevel
        );
    }

    public TileRect getLeafTiles(TileRect bounds) {
        int diff = maxlevel - bounds.zoom;

        return new TileRect(
                bounds.xmin << diff,
                bounds.ymin << diff,
                (bounds.xmax << diff) + (1 << diff) - 1,
                (bounds.ymax << diff) + (1 << diff) - 1,
                maxlevel
        );
    }


    @Override       // top-left tile vertex
    public Vec2d getPosition(int x, int y, int z) {
        Rect2d max = projection.getProjectedMaximumBounds();
        int tiles = 1 << z;

        return new Vec2d(
                max.xmin + (x / (double) tiles) * (max.xmax - max.xmin),
                max.ymin + (1 - y / (double) tiles) * (max.ymax - max.ymin)
        );
    }

    @Override
    public Rect2d getBounds(int x, int y, int z) {
        Rect2d max = projection.getProjectedMaximumBounds();
        int tiles = 1 << z;

        return new Rect2d(
                max.xmin + (x / (double) tiles) * (max.xmax - max.xmin),
                max.ymin + (1 - (y + 1) / (double) tiles) * (max.ymax - max.ymin),
                max.xmin + ((x + 1) / (double) tiles) * (max.xmax - max.xmin),
                max.ymin + (1 - y / (double) tiles) * (max.ymax - max.ymin)
        );
    }

    @Override
    public Rect2d getBounds(TileRect tiles) {
        Rect2d max = projection.getProjectedMaximumBounds();
        int n = 1 << tiles.zoom;

        Rect2d r =  new Rect2d(
                max.xmin + (tiles.xmin / (double) n) * (max.xmax - max.xmin),
                max.ymin + (1 - (tiles.ymax + 1) / (double) n) * (max.ymax - max.ymin),
                max.xmin + ((tiles.xmax + 1) / (double) n) * (max.xmax - max.xmin),
                max.ymin + (1 - tiles.ymin / (double) n) * (max.ymax - max.ymin)
        );

        return r;
    }


    public int getMaximumZoomLevel() {
        return maxlevel;
    }

    public int getMinimumZoomLevel() {
        return minlevel;
    }


    @Override
    public Projection getProjection() {
        return projection;
    }


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof QuadTreeTilingScheme))
            return false;

        QuadTreeTilingScheme other = (QuadTreeTilingScheme) obj;

        return this.projection.equals(other.getProjection());
    }

    @Override
    public int hashCode() {
        return new FNVHashBuilder()
                .add(projection)
                .getHash();
    }


    private static int clamp(int value, int min, int max) {
        return value < min ? min : (value > max ? max : value);
    }
}
