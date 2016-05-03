package microtrafficsim.core.map.tiles;

import microtrafficsim.math.Vec2i;
import microtrafficsim.utils.hashing.FNVHashBuilder;


public class TileRect {
    public int xmin, ymin, xmax, ymax;
    public int zoom;

    public TileRect(int xmin, int ymin, int xmax, int ymax, int zoom) {
        this.xmin = xmin;
        this.ymin = ymin;
        this.xmax = xmax;
        this.ymax = ymax;
        this.zoom = zoom;
    }

    public TileRect(Vec2i min, Vec2i max, int zoom) {
        this.xmin = min.x;
        this.ymin = min.y;
        this.xmax = max.x;
        this.ymax = max.y;
        this.zoom = zoom;
    }

    public TileRect(TileId tile) {
        this.xmin = this.xmax = tile.x;
        this.ymin = this.ymax = tile.y;
        this.zoom = tile.z;
    }

    public TileRect(TileRect other) {
        this.xmin = other.xmin;
        this.ymin = other.ymin;
        this.xmax = other.xmax;
        this.ymax = other.ymax;
        this.zoom = other.zoom;
    }

    public TileId min() {
        return new TileId(xmin, ymin, zoom);
    }

    public TileId max() {
        return new TileId(xmax, ymax, zoom);
    }


    public boolean contains(TileId tile) {
        return zoom == tile.z
                && xmin <= tile.x && xmax >= tile.x
                && ymin <= tile.y && ymax >= tile.y;
    }


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TileRect))
            return false;

        TileRect other = (TileRect) obj;

        return this.xmin == other.xmin
                && this.ymin == other.ymin
                && this.xmax == other.xmax
                && this.ymax == other.ymax
                && this.zoom == other.zoom;
    }

    @Override
    public int hashCode() {
        return new FNVHashBuilder()
                .add(xmin)
                .add(ymin)
                .add(xmax)
                .add(ymax)
                .add(zoom)
                .getHash();
    }

    @Override
    public String toString() {
        return this.getClass() + " {" + xmin + ", " + ymin + ", " + xmax + ", " + ymax +  ", " + zoom  + "}";
    }
}
