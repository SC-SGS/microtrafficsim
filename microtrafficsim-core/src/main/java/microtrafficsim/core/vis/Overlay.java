package microtrafficsim.core.vis;

import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseListener;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.view.OrthographicView;


/**
 * Overlay for the visualization, supporting user-input.
 *
 * @author Maximilian Luz
 */
public interface Overlay {

    /**
     * Sets the view of this overlay. This method is going to be called by the visualizer, do not call it manually.
     *
     * @param view the new view for this overlay.
     */
    void setView(OrthographicView view);

    /**
     * Initialize this overlay.
     *
     * @param context the {@code RenderContext} with which this overlay will be displayed.
     * @throws Exception if any exception occurs during initializing.
     */
    void initialize(RenderContext context) throws Exception;

    /**
     * Dispose this overlay.
     *
     * @param context the {@code RenderContext} with which this overlay is being displayed.
     * @throws Exception if any exception occurs during disposing.
     */
    void dispose(RenderContext context) throws Exception;

    /**
     * Resize this overlay.
     *
     * @param context the {@code RenderContext} with which this overlay is being displayed.
     * @throws Exception if any exception occurs during resizing.
     */
    void resize(RenderContext context) throws Exception;

    /**
     * Display this overlay:
     *
     * @param context the {@code RenderContext} with which this overlay is being displayed.
     * @param map     the render-buffer in which the displayed map is being stored.
     * @throws Exception if any exception occurs during the display-operation.
     */
    void display(RenderContext context, MapBuffer map) throws Exception;

    /**
     * Enable this overlay. Disabled overlays should not be displayed and receive user events.
     *
     * @param enabled set to {@code true} to enable this layer.
     */
    void setEnabled(boolean enabled);

    /**
     * Checks if this overlay is enabled.
     *
     * @return {@code true} if this overlay is enabled, {@code false} otherwise.
     */
    boolean isEnabled();

    /**
     * Returns the {@code KeyListener} handling the key-events for this overlay.
     *
     * @return the {@code KeyListener} handling the key-events for this overlay or {@code null} if none is present.
     */
    default KeyListener getKeyListener() {
        return null;
    }

    /**
     * Returns the {@code MouseListener} handling the mouse-events for this overlay.
     *
     * @return the {@code MouseListener} handling the mouse-events for this overlay or {@code null} if none is present.
     */
    default MouseListener getMouseListener() {
        return null;
    }


    /**
     * Render-buffer for the map being displayed.
     */
    class MapBuffer {
        /**
         * The frame-buffer storing the map.
         */
        public final int fbo;

        /**
         * The color-attachment (color texture) of the frame-buffer storing the map.
         */
        public final int color;

        /**
         * The depth-attachment (depth texture) of the frame-buffer storing the map.
         */
        public final int depth;

        /**
         * Cronstructs a new map-buffer with the given parameters.
         *
         * @param fbo   the frame-buffer storing the map being displayed.
         * @param color the color attachment (color texture of the map).
         * @param depth the depth attachment (depth texture of the map).
         */
        public MapBuffer(int fbo, int color, int depth) {
            this.fbo   = fbo;
            this.color = color;
            this.depth = depth;
        }
    }
}
