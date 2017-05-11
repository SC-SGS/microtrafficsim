package microtrafficsim.core.exfmt.ecs.components;


import microtrafficsim.core.exfmt.ecs.Component;
import microtrafficsim.core.exfmt.ecs.Entity;
import microtrafficsim.core.map.tiles.TileRect;
import microtrafficsim.core.map.tiles.TilingScheme;
import microtrafficsim.utils.hashing.FNVHashBuilder;

import java.util.Collection;
import java.util.HashSet;


public class TileGridComponent extends Component {
    private HashSet<Entry> grids = new HashSet<>();

    public TileGridComponent(Entity entity) {
        super(entity);
    }


    public boolean add(Entry entry) {
        return grids.add(entry);
    }

    public boolean addAll(Collection<Entry> entries) {
        return grids.addAll(entries);
    }

    public boolean remove(Entry entry) {
        return grids.remove(entry);
    }

    public boolean in(Entry entry) {
        return grids.contains(entry);
    }

    public HashSet<Entry> getAll() {
        return grids;
    }


    public static class Entry {
        private TilingScheme scheme;
        private TileRect level;
        private int x;
        private int y;


        public Entry(TilingScheme scheme, TileRect level, int x, int y) {
            this.scheme = scheme;
            this.level = level;
            this.x = x;
            this.y = y;
        }

        public TilingScheme getScheme() {
            return scheme;
        }

        public TileRect getLevel() {
            return level;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }


        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Entry))
                return false;

            Entry other = (Entry) obj;

            return this.scheme.equals(other.scheme)
                    && this.level.equals(other.level)
                    && this.x == other.x
                    && this.y == other.y;
        }

        @Override
        public int hashCode() {
            return new FNVHashBuilder()
                    .add(scheme)
                    .add(level)
                    .add(x)
                    .add(y)
                    .getHash();
        }
    }
}
