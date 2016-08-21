package microtrafficsim.core.vis;

import microtrafficsim.core.vis.context.RenderContext;


/**
 * The renderer executing the render-code.
 *
 * @author Maximilian Luz
 */
public interface Renderer {

    /**
     * Initializes the renderer. Called by the {@code RenderContext} on which this renderer is going to be executed.
     *
     * @param context the context on which this renderer is going to be executed.
     * @throws Exception if an exception occurs during the initialization, see the implementation for details.
     */
    void init(RenderContext context) throws Exception;

    /**
     * Disposes the renderer. Called by the {@code RenderContext} on which this renderer is going to be executed.
     *
     * @param context the context on which this renderer is going to be executed.
     * @throws Exception if an exception occurs during the dispose-operation, see the implementation for details.
     */
    void dispose(RenderContext context) throws Exception;

    /**
     * Displays the renderer. Called by the {@code RenderContext} on which this renderer is going to be executed.
     *
     * @param context the context on which this renderer is going to be executed.
     * @throws Exception if an exception occurs during the display-operation, see the implementation for details.
     */
    void display(RenderContext context) throws Exception;

    /**
     * Reshapes the viewport of the renderer. Called by the {@code RenderContext} on which this renderer is going to
     * be executed.
     *
     * @param context the context on which this renderer is going to be executed.
     * @param x       the minimum x value of the (new) viewport.
     * @param y       the minimum y value of the (new) viewport.
     * @param width   the width of the (new) viewport.
     * @param height  the height of the (new) viewport.
     * @throws Exception if an exception occurs during the reshape-operation, see the implementation for details.
     */
    void reshape(RenderContext context, int x, int y, int width, int height) throws Exception;
}
