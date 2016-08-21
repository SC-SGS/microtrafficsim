package microtrafficsim.core.vis;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.MouseEvent;
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
     * @param context the {@¢ode RenderContext} with which this overlay will be displayed.
     */
    void init(RenderContext context);

    /**
     * Dispose this overlay.
     *
     * @param context the {@¢ode RenderContext} with which this overlay is being displayed.
     */
    void dispose(RenderContext context);

    /**
     * Resize this overlay.
     *
     * @param context the {@¢ode RenderContext} with which this overlay is being displayed.
     */
    void resize(RenderContext context);

    /**
     * Display this overlay:
     *
     * @param context the {@¢ode RenderContext} with which this overlay is being displayed.
     * @param map     the render-buffer in which the displayed map is being stored.
     */
    void display(RenderContext context, MapBuffer map);

    /**
     * Enable this overlay. Disabled overlays should not be displayed and receive user events.
     */
    void    enable();

    /**
     * Disable this overlay. Disabled overlays should not be displayed and receive user events.
     */
    void    disable();

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
    default KeyListener getKeyListeners() {
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
     * Key-listener for the {@code Overlay}.
     */
    interface KeyListener {

        /**
         * Called when a key is being pressed.
         *
         * @param e the {@code KeyEvent} for this action.
         * @return true if this listener consumed this event, i.e. no overlay further down the event-chain should be
         * notified of this event.
         */
        default boolean keyPressed(KeyEvent e) { return false; }

        /**
         * Called when a key is being released.
         *
         * @param e the {@code KeyEvent} for this action.
         * @return true if this listener consumed this event, i.e. no overlay further down the event-chain should be
         * notified of this event.
         */
        default boolean keyReleased(KeyEvent e) { return false; }
    }

    /**
     * Mouse-listener for the {@code Overlay}.
     */
    interface MouseListener {

        /**
         * Called when a mouse-button is being clicked.
         *
         * @param e the {@code MouseEvent} for this action.
         * @return {@code true} if this listener consumed this event, i.e. no overlay further down the event-chain should be
         * notified of this event.
         */
        default boolean mouseClicked(MouseEvent e) { return false; }

        /**
         * Called when a mouse-button is being pressed.
         *
         * @param e the {@code MouseEvent} for this action.
         * @return {@code true} if this listener consumed this event, i.e. no overlay further down the event-chain should be
         * notified of this event.
         */
        default boolean mousePressed(MouseEvent e) { return false; }

        /**
         * Called when a mouse-button is being released.
         *
         * @param e the {@code MouseEvent} for this action.
         * @return {@code true} if this listener consumed this event, i.e. no overlay further down the event-chain should be
         * notified of this event.
         */
        default boolean mouseReleased(MouseEvent e) { return false; }

        /**
         * Called when the mouse is being moved.
         *
         * @param e the {@code MouseEvent} for this action.
         * @return {@code true} if this listener consumed this event, i.e. no overlay further down the event-chain should be
         * notified of this event.
         */
        default boolean mouseMoved(MouseEvent e) { return false; }

        /**
         * Called when the mouse is being dragged.
         *
         * @param e the {@code MouseEvent} for this action.
         * @return true if this listener consumed this event, i.e. no overlay further down the event-chain should be
         * notified of this event.
         */
        default boolean mouseDragged(MouseEvent e) { return false; }

        /**
         * Called when the mouse-wheel is being moved.
         *
         * @param e the {@code MouseEvent} for this action.
         * @return {@code true} if this listener consumed this event, i.e. no overlay further down the event-chain should be
         * notified of this event.
         */
        default boolean mouseWheelMoved(MouseEvent e) { return false; }

        /**
         * Called when the mouse entered this overlay.
         *
         * @param e the {@code MouseEvent} for this action.
         * @return {@code true} if this listener consumed this event, i.e. no overlay further down the event-chain
         * should be notified of this event. For this event-type, the event should normally not be consumed, thus the
         * method should always return false.
         */
        default boolean mouseEntered(MouseEvent e) { return false; }

        /**
         * Called when the mouse left this overlay.
         *
         * @param e the {@code MouseEvent} for this action.
         * @return {@code true} if this listener consumed this event, i.e. no overlay further down the event-chain
         * should be notified of this event. For this event-type, the event should normally not be consumed, thus the
         * method should always return false.
         */
        default boolean mouseExited(MouseEvent e) { return false; }
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
