package microtrafficsim.core.map.tiles;

import microtrafficsim.utils.hashing.FNVHashBuilder;


/**
 * Tile identifier.
 *
 * @author Maximilian Luz
 */
public class TileId {
    /**
     * The x-component of the id.
     */
    public final int x;

    /**
     * The y-component of the id.
     */
    public final int y;

    /**
     * The z-component (zoom-level) of the id.
     */
    public final int z;

    /**
     * Constructs a new {@code TileId}.
     *
     * @param x the x-component of the id.
     * @param y the y-component of the id.
     * @param z the z-component of the id.
     */
    public TileId(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Constructs a new {@code TileId} by copying the specified one.
     *
     * @param other the {@code TileId} to copy.
     */
    public TileId(TileId other) {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
    }


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TileId)) return false;

        TileId other = (TileId) obj;
        return this.x == other.x && this.y == other.y && this.z == other.z;
    }

    @Override
    public int hashCode() {
        return new FNVHashBuilder()
                .add(x)
                .add(y)
                .add(z)
                .getHash();
    }

    @Override
    public String toString() {
        return this.getClass().getCanonicalName() + " { " + x + ", " + y + ", " + z + " }";
    }
}
