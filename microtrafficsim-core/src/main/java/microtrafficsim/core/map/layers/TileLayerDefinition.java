package microtrafficsim.core.map.layers;


public class TileLayerDefinition {
    private final String    name;
    private int             index;
    private int             minzoom;
    private int             maxzoom;
    private TileLayerSource source;

    public TileLayerDefinition(String name, int index, int minzoom, int maxzoom, TileLayerSource source) {
        this.name    = name;
        this.index   = index;
        this.minzoom = minzoom;
        this.maxzoom = maxzoom;
        this.source  = source;
    }


    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public TileLayerSource getSource() {
        return source;
    }

    public int getMinimumZoomLevel() {
        return minzoom;
    }

    public int getMaximumZoomLevel() {
        return maxzoom;
    }
}
