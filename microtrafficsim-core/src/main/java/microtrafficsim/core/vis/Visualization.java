package microtrafficsim.core.vis;

import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseListener;
import microtrafficsim.core.vis.context.RenderContext;

import java.util.Collection;


/**
 * The core visualization, combining rendering ({@code Visualizer}) and input ({@code MouseListener},
 * {@code KeyListener}) aspects.
 *
 * @author Maximilian Luz
 */
public interface Visualization {

    /**
     * Returns the default configuration of the {@code Visualizer} associated with this {@code Visualization}.
     *
     * @return the default configuration for the associated {@code Visualizer}.
     * @throws UnsupportedFeatureException if the system does not support one or more required OpenGL features.
     */
    VisualizerConfig getDefaultConfig() throws UnsupportedFeatureException;

    /**
     * Returns the {@code MouseListener} used for this visualization.
     *
     * @return the {@code MouseListener} used for this visualization.
     */
    MouseListener getMouseController();

    /**
     * Returns the {@code KeyListener} used for this visualization.
     *
     * @return the {@code KeyListener} used for this visualization.
     */
    KeyListener   getKeyController();

    /**
     * Returns the {@code RenderContext} used for this visualization.
     *
     * @return the {@code RenderContext} used for this visualization.
     */
    RenderContext getRenderContext();

    /**
     * Returns the {@code Visualizer} used for this visualization.
     *
     * @return the {@code Visualizer} used for this visualization.
     */
    Visualizer    getVisualizer();

    /**
     * Adds the specified overlay to be rendered at the given index, only one overlay can be associated with an index.
     * The index denotes the order in which the overlays are going to be rendered, lower indices first.
     *
     * @param index   the index for which the overlay should be added.
     * @param overlay the overlay to be added.
     * @return the overlay previously associated with the given index.
     */
    Overlay putOverlay(int index, Overlay overlay);

    /**
     * Removes the overlay associated with the specified index and return it.
     *
     * @param index the index for which the overlay should be removed.
     * @return the overlay previously associated with the given index.
     */
    Overlay removeOverlay(int index);

    /**
     * Returns the overlay associated with the given index.
     *
     * @param index the index for which the overlay should be returned.
     * @return the overlay associated with the given index.
     */
    Overlay getOverlay(int index);

    /**
     * Returns all overlays for this visualizer.
     *
     * @return the overlays on this visualizer.
     */
    Collection<Overlay> getAllOverlays();
}
