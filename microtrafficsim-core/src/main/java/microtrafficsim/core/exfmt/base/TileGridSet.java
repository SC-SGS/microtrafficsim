package microtrafficsim.core.exfmt.base;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ecs.Entity;
import microtrafficsim.core.map.tiles.TileRect;
import microtrafficsim.core.map.tiles.TilingScheme;
import microtrafficsim.utils.collections.Grid;
import microtrafficsim.utils.hashing.FNVHashBuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


// contains tile-grids: links to entities in ecs grouped by tiles
public class TileGridSet extends Container.Entry {
    private HashMap<Key, TileGrid> grids = new HashMap<>();


    public TileGrid set(TilingScheme scheme, TileRect level, TileGrid grid) {
        return this.grids.put(new Key(scheme, level), grid);
    }

    public TileGrid get(TilingScheme scheme, TileRect level) {
        return this.grids.get(new Key(scheme, level));
    }

    public TileGrid getOrCreate(TilingScheme scheme, TileRect level) {
        return this.grids.computeIfAbsent(new Key(scheme, level), (key) -> new TileGrid(scheme, level));
    }

    public TileGrid remove(TilingScheme scheme, TileRect level) {
        return this.grids.remove(new Key(scheme, level));
    }

    public Map<Key, TileGrid> getAll() {
        return grids;
    }


    public static class TileGrid {
        public TilingScheme scheme;
        public TileRect level;
        public Grid<Set<Entity>> entities;

        public TileGrid(TilingScheme scheme, TileRect level) {
            this(scheme, level, new Grid<>(level.xmax - level.xmin + 1, level.ymax - level.ymin + 1));

            System.err.println(entities.getSizeX());
            System.err.println(entities.getSizeY());

            for (int y = 0; y < entities.getSizeY(); y++) {
                for (int x = 0; x < entities.getSizeX(); x++) {
                    entities.set(x, y, new HashSet<>());
                }
            }
        }

        public TileGrid(TilingScheme scheme, TileRect level, Grid<Set<Entity>> entities) {
            this.scheme = scheme;
            this.level = level;
            this.entities = entities;
        }
    }

    public static class Key {
        public final TilingScheme scheme;
        public final TileRect level;

        public Key(TilingScheme scheme, TileRect level) {
            this.scheme = scheme;
            this.level = level;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Key))
                return false;

            Key other = (Key) obj;

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
