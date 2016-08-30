package microtrafficsim.core.vis;

import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.view.View;

import java.util.Collection;


/**
 * Visualizer for the simulation, responsible for the actual rendering process.
 *
 * @author Maximilian Luz
 */
public interface Visualizer extends Renderer {

    /**
     * Creates and return a default configuration.
     *
     * @return the default configuration.
     * @throws UnsupportedFeatureException if the system does not support the required OpenGL features.
     */
    VisualizerConfig getDefaultConfig() throws UnsupportedFeatureException;

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

    /**
     * Returns the view of this visualizer.
     *
     * @return the view of this visualizer.
     */
    View getView();

    /**
     * Resets the view of this visualizer to the default state.
     */
    void resetView();

    /**
     * Returns the {@code RenderContext} of this visualizer.
     *
     * @return the {@code RenderContext} used by this visualizer.
     */
    RenderContext getContext();
}
