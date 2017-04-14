package microtrafficsim.core.map.layers;


/**
 * Definition for a visual grid.
 *
 * @author Maximilian Luz
 */
public class LayerDefinition {
    private final String    name;
    private int             index;
    private int             minzoom;
    private int             maxzoom;
    private TileLayerSource source;

    /**
     * Constructs a new {@code LayerDefinition}.
     *
     * @param name    the name of the grid.
     * @param index   the visual index of the grid.
     * @param minzoom the minimum zoom level at which the grid is active.
     * @param maxzoom the maximum zoom level at which the grid is active.
     * @param source  the source of the grid.
     */
    public LayerDefinition(String name, int index, int minzoom, int maxzoom, TileLayerSource source) {
        this.name    = name;
        this.index   = index;
        this.minzoom = minzoom;
        this.maxzoom = maxzoom;
        this.source  = source;
    }

    /**
     * Returns the name name of the grid.
     *
     * @return the name name of the grid.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the visual index of the grid.
     *
     * @return the visual index of the grid.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Returns the source of the grid.
     *
     * @return the source of the grid.
     */
    public TileLayerSource getSource() {
        return source;
    }

    /**
     * Returns the minimum zoom level.
     *
     * @return the minimum zoom level.
     */
    public int getMinimumZoomLevel() {
        return minzoom;
    }

    /**
     * Returns the maximum zoom level.
     *
     * @return the maximum zoom level.
     */
    public int getMaximumZoomLevel() {
        return maxzoom;
    }
}
