package microtrafficsim.core.exfmt.context;

import microtrafficsim.core.map.tiles.TileRect;
import microtrafficsim.core.map.tiles.TilingScheme;


public class TileGridContext {
    public TilingScheme scheme;
    public TileRect grid;
    public int x;
    public int y;

    public TileGridContext(TilingScheme scheme, TileRect grid) {
        this.scheme = scheme;
        this.grid = grid;
    }
}
