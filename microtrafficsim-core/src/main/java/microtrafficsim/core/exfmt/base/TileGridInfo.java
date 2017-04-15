package microtrafficsim.core.exfmt.base;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.map.tiles.TileRect;
import microtrafficsim.core.map.tiles.TilingScheme;
import microtrafficsim.utils.hashing.FNVHashBuilder;

import java.util.HashSet;
import java.util.Set;


// contains info about stored tile-grids
public class TileGridInfo extends Container.Entry {
    private HashSet<Grid> grids = new HashSet<>();


    public boolean add(Grid grid) {
        return this.grids.add(grid);
    }

    public boolean add(TilingScheme scheme, TileRect level) {
        return add(new Grid(scheme, level));
    }

    public boolean remove(Grid grid) {
        return this.grids.remove(grid);
    }

    public boolean remove(TilingScheme scheme, TileRect level) {
        return remove(new Grid(scheme, level));
    }

    public Set<Grid> getAll() {
        return grids;
    }


    public static class Grid {
        public final TilingScheme scheme;
        public final TileRect level;

        public Grid(TilingScheme scheme, TileRect level) {
            this.scheme = scheme;
            this.level = level;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Grid))
                return false;

            Grid other = (Grid) obj;

            return this.scheme.equals(other.scheme)
                    && this.level.equals(other.level);
        }

        @Override
        public int hashCode() {
            return new FNVHashBuilder()
                    .add(scheme)
                    .add(level)
                    .getHash();
        }
    }
}
