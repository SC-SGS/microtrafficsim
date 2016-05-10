package microtrafficsim.core.map.tiles;


import microtrafficsim.utils.hashing.FNVHashBuilder;

public class TileId {
    public final int x;
    public final int y;
    public final int z;

    public TileId(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public TileId(TileId other) {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
    }


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TileId))
            return false;

        TileId other = (TileId) obj;

        return this.x == other.x
                && this.y == other.y
                && this.z == other.z;
    }

    @Override
    public int hashCode() {
        return new FNVHashBuilder()
                .add(x)
                .add(y)
                .add(z)
                .getHash();
    }
}
