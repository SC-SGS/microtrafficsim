package microtrafficsim.core.map.layers;


/**
 * Definition for a visual layer.
 *
 * @author Maximilian Luz
 */
public class LayerDefinition {
    private final String    name;
    private int             index;
    private int             minzoom;
    private int             maxzoom;
    private LayerSource source;

    /**
     * Constructs a new {@code LayerDefinition}.
     *
     * @param name    the name of the layer.
     * @param index   the visual index of the layer.
     * @param minzoom the minimum zoom level at which the layer is active.
     * @param maxzoom the maximum zoom level at which the layer is active.
     * @param source  the source of the layer.
     */
    public LayerDefinition(String name, int index, int minzoom, int maxzoom, LayerSource source) {
        this.name    = name;
        this.index   = index;
        this.minzoom = minzoom;
        this.maxzoom = maxzoom;
        this.source  = source;
    }

    /**
     * Returns the name name of the layer.
     *
     * @return the name name of the layer.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the visual index of the layer.
     *
     * @return the visual index of the layer.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Returns the source of the layer.
     *
     * @return the source of the layer.
     */
    public LayerSource getSource() {
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
