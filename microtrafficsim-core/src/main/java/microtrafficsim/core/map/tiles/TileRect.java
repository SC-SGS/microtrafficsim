package microtrafficsim.core.map.tiles;

import microtrafficsim.math.MathUtils;
import microtrafficsim.math.Vec2i;
import microtrafficsim.utils.hashing.FNVHashBuilder;

import java.util.HashSet;
import java.util.Set;


/**
 * Tile rectangle (tile-IDs are inclusive).
 *
 * @author Maximilian Luz
 */
public class TileRect {
    public int xmin, ymin, xmax, ymax;
    public int zoom;

    /**
     * Constructs a new tile rectangle.
     *
     * @param xmin the minimum x tile.
     * @param ymin the minimum y tile.
     * @param xmax the maximum x tile.
     * @param ymax the maximum y tile.
     * @param zoom the zoom-level of the tiles.
     */
    public TileRect(int xmin, int ymin, int xmax, int ymax, int zoom) {
        this.xmin = xmin;
        this.ymin = ymin;
        this.xmax = xmax;
        this.ymax = ymax;
        this.zoom = zoom;
    }

    /**
     * Constructs a new tile rectangle.
     *
     * @param min the xy-minimum tile.
     * @param max the xy-maximum tile.
     * @param zoom the zoom-level of the tiles.
     */
    public TileRect(Vec2i min, Vec2i max, int zoom) {
        this.xmin = min.x;
        this.ymin = min.y;
        this.xmax = max.x;
        this.ymax = max.y;
        this.zoom = zoom;
    }

    /**
     * Constructs a new tile rectangle from the given tile-id.
     *
     * @param tile the tile to create the recangle from.
     */
    public TileRect(TileId tile) {
        this.xmin = this.xmax = tile.x;
        this.ymin = this.ymax = tile.y;
        this.zoom             = tile.z;
    }

    /**
     * Copy-constructs a new tile rectangle.
     *
     * @param other the tile rectangle to copy.
     */
    public TileRect(TileRect other) {
        this.xmin = other.xmin;
        this.ymin = other.ymin;
        this.xmax = other.xmax;
        this.ymax = other.ymax;
        this.zoom = other.zoom;
    }

    /**
     * Subtracts two tile rectangles.
     *
     * @param a the tile rectangle to subtract from.
     * @param b the tile rectangle to subtract.
     * @return the resulting set of tiles.
     */
    public static Set<TileId> subtract(TileRect a, TileRect b) {
        if (a.zoom != b.zoom) return null;

        // intersection (part that is 'removed' from a)
        int xmin = MathUtils.clamp(b.xmin, a.xmin, a.xmax + 1);
        int ymin = MathUtils.clamp(b.ymin, a.ymin, a.ymax + 1);
        int xmax = MathUtils.clamp(b.xmax, a.xmin - 1, a.xmax);
        int ymax = MathUtils.clamp(b.ymax, a.ymin - 1, a.ymax);

        // pre-compute size of the result
        int size = ((xmin - a.xmin) + (a.xmax - xmax)) * (a.ymax - a.ymin)    // full left & rigth (x) segments
                   + ((ymin - a.ymin) + (a.ymax - ymax)) * (xmax - xmin);     // partial top & bottom (y) segments

        Set<TileId> result = new HashSet<>(size);

        // full x-segments
        for (int y = a.ymin; y <= a.ymax; y++) {
            for (int x = a.xmin; x < xmin; x++)
                result.add(new TileId(x, y, a.zoom));

            for (int x = a.xmax; x > xmax; x--)
                result.add(new TileId(x, y, a.zoom));
        }

        // partial y-segments
        for (int x = xmin; x <= xmax; x++) {
            for (int y = a.ymin; y < ymin; y++)
                result.add(new TileId(x, y, a.zoom));

            for (int y = a.ymax; y > ymax; y--)
                result.add(new TileId(x, y, a.zoom));
        }

        return result;
    }

    /**
     * Intersects two tile rectangles.
     *
     * @param a the first tile rectangle to intersect.
     * @param b the second tile rectangle to intersect.
     * @return the intersection of both rectangles.
     */
    public static TileRect intersect(TileRect a, TileRect b) {
        if (a.zoom != b.zoom) return null;

        return new TileRect(Math.max(a.xmin, b.xmin),
                            Math.max(a.ymin, b.ymin),
                            Math.min(a.xmax, b.xmax),
                            Math.min(a.ymax, b.ymax),
                            a.zoom);
    }

    /**
     * Returns the minimum xy tile of this rectangle.
     *
     * @return the minimum xy tile of this rectangle.
     */
    public TileId min() {
        return new TileId(xmin, ymin, zoom);
    }

    /**
     * Returns the maximum xy tile of this rectangle.
     *
     * @return the maximum xy tile of this rectangle.
     */
    public TileId max() {
        return new TileId(xmax, ymax, zoom);
    }

    /**
     * Checks if this rectangle contains the given tile.
     *
     * @param tile the tile to test against.
     * @return {@code true} if this rectangle contains the given tile.
     */
    public boolean contains(TileId tile) {
        return zoom == tile.z && xmin <= tile.x && xmax >= tile.x && ymin <= tile.y && ymax >= tile.y;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TileRect)) return false;

        TileRect other = (TileRect) obj;

        return this.xmin == other.xmin && this.ymin == other.ymin && this.xmax == other.xmax && this.ymax == other.ymax
                && this.zoom == other.zoom;
    }

    @Override
    public int hashCode() {
        return new FNVHashBuilder().add(xmin).add(ymin).add(xmax).add(ymax).add(zoom).getHash();
    }

    @Override
    public String toString() {
        return this.getClass() + " {" + xmin + ", " + ymin + ", " + xmax + ", " + ymax + ", " + zoom + "}";
    }

    /**
     * Returns the set of all tiles contained in this rectangle.
     *
     * @return the set of all tiles contained in this rectangle.
     */
    public Set<TileId> getAll() {
        int sx = Math.max(0, xmax - xmin + 1);
        int sy = Math.max(0, ymax - ymin + 1);

        Set<TileId> all = new HashSet<>(sx * sy);
        for (int x = xmin; x <= xmax; x++)
            for (int y = ymin; y <= ymax; y++)
                all.add(new TileId(x, y, zoom));

        return all;
    }
}
