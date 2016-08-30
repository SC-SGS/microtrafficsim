package microtrafficsim.core.vis.tilebased;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import microtrafficsim.core.map.style.StyleSheet;
import microtrafficsim.core.vis.AbstractVisualization;
import microtrafficsim.core.vis.Overlay;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.input.KeyCommand;
import microtrafficsim.core.vis.input.KeyController;
import microtrafficsim.core.vis.input.OrthoInputController;
import microtrafficsim.core.vis.map.tiles.TileProvider;
import microtrafficsim.core.vis.view.OrthographicView;
import microtrafficsim.utils.concurrency.InterruptSafeExecutors;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.stream.Collectors;


/**
 * Tile-based visualization using an {@code OrthographicView}.
 *
 * @author Maximilian Luz
 */
public class TileBasedVisualization extends AbstractVisualization {

    private static final int    ZOOM_LEVEL_MIN = 0;
    private static final int    ZOOM_LEVEL_MAX = 19;
    private static final double ZOOM_FACTOR    = 0.1;

    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR  = 1000.f;

    private OrthoInputController controller;
    private MouseListener        mouseController = new MouseControllerImpl();
    private KeyController        keyController   = new KeyControllerImpl();


    /**
     * Constructs a new tile-based visualization using the given parameters.
     *
     * @param width          the width of the viewport.
     * @param height         the height of the viewport.
     * @param provider       the {@code TileProvider} providing the tiles to be displayed.
     * @param nWorkerThreads the number of worker-threads responsible for loading the tiles.
     */
    public TileBasedVisualization(int width, int height, TileProvider provider, int nWorkerThreads) {
        this(width, height, provider, InterruptSafeExecutors.newFixedThreadPool(nWorkerThreads));
    }

    /**
     * Constructs a new tile-based visualization using the given parameters.
     *
     * @param width    the width of the viewport.
     * @param height   the height of the viewport.
     * @param provider the {@code TileProvider} providing the tiles to be displayed.
     * @param worker   the {@code ExecutorService} providing the worker-threads.
     */
    public TileBasedVisualization(int width, int height, TileProvider provider, ExecutorService worker) {
        this(new OrthographicView(width, height, Z_NEAR, Z_FAR, ZOOM_LEVEL_MIN, ZOOM_LEVEL_MAX), provider, worker);
    }

    /**
     * Constructs a new tile-based visualization using the given parameters.
     *
     * @param view     the {@code OrthographicView} to be used for this visualization.
     * @param provider the {@code TileProvider} providing the tiles to be displayed.
     * @param worker   the {@code ExecutorService} providing the worker-threads.
     */
    private TileBasedVisualization(OrthographicView view, TileProvider provider, ExecutorService worker) {
        this(new RenderContext(), view, provider, worker);
    }

    /**
     * Constructs a new tile-based visualization using the given parameters.
     *
     * @param context  the {@code RenderContext} on which this visualization will be displayed.
     * @param view     the {@code OrthographicView} to be used for this visualization.
     * @param provider the {@code TileProvider} providing the tiles to be displayed.
     * @param worker   the {@code ExecutorService} providing the worker-threads.
     */
    private TileBasedVisualization(RenderContext context, OrthographicView view, TileProvider provider,
                                   ExecutorService worker) {
        super(context, new TileBasedVisualizer(context, view, provider, worker));
        this.controller = new OrthoInputController(view, ZOOM_FACTOR);
    }


    /**
     * Applies the given style-sheet to this visualization.
     *
     * @param style the style-sheet to apply.
     */
    public void apply(StyleSheet style) {
        ((TileBasedVisualizer) getVisualizer()).apply(style);
    }


    @Override
    public MouseListener getMouseController() {
        return mouseController;
    }

    @Override
    public KeyController getKeyController() {
        return keyController;
    }


    /**
     * Basic key-listener to distribute the mouse-events to the specific overlays and finally the controller of
     * this visualization.
     */
    private class MouseControllerImpl implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            resolve(e, Overlay.MouseListener::mouseClicked, controller::mouseClicked);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            resolve(e, Overlay.MouseListener::mousePressed, controller::mousePressed);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            resolve(e, Overlay.MouseListener::mouseReleased, controller::mouseReleased);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            resolve(e, Overlay.MouseListener::mouseMoved, controller::mouseMoved);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            resolve(e, Overlay.MouseListener::mouseDragged, controller::mouseDragged);
        }

        @Override
        public void mouseWheelMoved(MouseEvent e) {
            resolve(e, Overlay.MouseListener::mouseWheelMoved, controller::mouseWheelMoved);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            resolve(e, Overlay.MouseListener::mouseEntered, controller::mouseEntered);
        }
        /**
         * Resolves the given event by distributing it to the overlays and the top-level controller. The
         * distribution-process stops as soon as any overlay fully consumes the event (i.e. the callback of this event
         * returns {@code true}) and thus blocks subsequent overlays from receiving this event.
         *
         * @param e        the event to resolve.
         * @param call     the call to be executed for each overlay.
         * @param toplevel the top-level receiver of the event.
         */
        @Override
        public void mouseExited(MouseEvent e) {
            resolve(e, Overlay.MouseListener::mouseExited, controller::mouseExited);
        }


        /**
         * Resolves the given event by distributing it to the overlays and the top-level controller. The
         * distribution-process stops as soon as any overlay fully consumes the event (i.e. the callback of this event
         * returns {@code true}) and thus blocks subsequent overlays from receiving this event.
         *
         * @param e        the event to resolve.
         * @param call     the call to be executed for each overlay.
         * @param toplevel the top-level receiver of the event.
         */
        private void resolve(MouseEvent e, BiPredicate<Overlay.MouseListener, MouseEvent> call,
                             Consumer<MouseEvent> toplevel) {
            ArrayList<Overlay.MouseListener> listeners = getAllOverlays().stream()
                    .map(Overlay::getMouseListener)
                    .filter(listener -> listener != null)
                    .collect(Collectors.toCollection(ArrayList::new));

            for (int i = listeners.size() - 1; i >= 0; i--)
                if (call.test(listeners.get(i), e))
                    return;

            toplevel.accept(e);
        }
    }

    /**
     * Basic key-listener to distribute the key-events to the specific overlays and finally the controller of
     * this visualization.
     */
    private class KeyControllerImpl implements KeyController {

        @Override
        public KeyCommand addKeyCommand(short event, short vk, KeyCommand command) {
            return controller.addKeyCommand(event, vk, command);
        }

        @Override
        public KeyCommand removeKeyCommand(short event, short vk) {
            return controller.removeKeyCommand(event, vk);
        }

        @Override
        public void keyPressed(KeyEvent e) {
            resolve(e, Overlay.KeyListener::keyPressed, controller::keyPressed);
        }

        @Override
        public void keyReleased(KeyEvent e) {
            resolve(e, Overlay.KeyListener::keyReleased, controller::keyReleased);
        }


        /**
         * Resolves the given event by distributing it to the overlays and the top-level controller. The
         * distribution-process stops as soon as any overlay fully consumes the event (i.e. the callback of this event
         * returns {@code true}) and thus blocks subsequent overlays from receiving this event.
         *
         * @param e        the event to resolve.
         * @param call     the call to be executed for each overlay.
         * @param toplevel the top-level receiver of the event.
         */
        private void resolve(KeyEvent e, BiPredicate<Overlay.KeyListener, KeyEvent> call, Consumer<KeyEvent> toplevel) {
            ArrayList<Overlay.KeyListener> listeners = getAllOverlays().stream()
                    .map(Overlay::getKeyListeners)
                    .filter(listener -> listener != null)
                    .collect(Collectors.toCollection(ArrayList::new));

            for (int i = listeners.size() - 1; i > 0; i--)
                if (call.test(listeners.get(i), e))
                    return;

            toplevel.accept(e);
        }
    }
}
