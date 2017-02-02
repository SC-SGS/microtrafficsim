package microtrafficsim.core.vis.tilebased;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import microtrafficsim.core.map.style.MapStyleSheet;
import microtrafficsim.core.vis.AbstractVisualization;
import microtrafficsim.core.vis.Overlay;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.input.KeyCommand;
import microtrafficsim.core.vis.input.KeyController;
import microtrafficsim.core.vis.input.OrthoInputController;
import microtrafficsim.core.vis.map.tiles.TileProvider;
import microtrafficsim.core.vis.view.OrthographicView;
import microtrafficsim.utils.concurrency.interruptsafe.InterruptSafeExecutors;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
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
    private MouseListener        mouseController = new MouseListenerImpl();
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
    public void apply(MapStyleSheet style) {
        ((TileBasedVisualizer) getVisualizer()).apply(style);
    }


    @Override
    public MouseListener getMouseListener() {
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
    private class MouseListenerImpl implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            resolve(e, MouseListener::mouseClicked, controller::mouseClicked);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            resolve(e, MouseListener::mousePressed, controller::mousePressed);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            resolve(e, MouseListener::mouseReleased, controller::mouseReleased);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            resolve(e, MouseListener::mouseMoved, controller::mouseMoved);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            resolve(e, MouseListener::mouseDragged, controller::mouseDragged);
        }

        @Override
        public void mouseWheelMoved(MouseEvent e) {
            resolve(e, MouseListener::mouseWheelMoved, controller::mouseWheelMoved);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            resolve(e, MouseListener::mouseEntered, controller::mouseEntered);
        }
        /**
         * Resolves the given event by distributing it to the overlays and the top-level controller. The
         * distribution-process stops as soon as any overlay fully consumes the event (i.e. the callback of this event
         * returns {@code true}) and thus blocks subsequent overlays from receiving this event.
         *
         * @param e        the event to resolveKeyEvent.
         */
        @Override
        public void mouseExited(MouseEvent e) {
            resolve(e, MouseListener::mouseExited, controller::mouseExited);
        }


        /**
         * Resolves the given event by distributing it to the overlays and the top-level controller. The
         * distribution-process stops as soon as any overlay fully consumes the event (i.e. the callback of this event
         * returns {@code true}) and thus blocks subsequent overlays from receiving this event.
         *
         * @param e        the event to resolveKeyEvent.
         * @param fn       the function to be executed for each overlay.
         * @param toplevel the top-level receiver of the event.
         */
        private void resolve(MouseEvent e, MouseListenerFunction fn, Consumer<MouseEvent> toplevel) {
            ArrayList<MouseListener> listeners = getAllOverlays().stream()
                    .map(Overlay::getMouseListener)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(ArrayList::new));

            for (int i = listeners.size() - 1; i >= 0; i--) {
                fn.call(listeners.get(i), e);
                if (e.isConsumed())
                    return;
            }

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
            resolve(e, KeyListener::keyPressed, controller::keyPressed);
        }

        @Override
        public void keyReleased(KeyEvent e) {
            resolve(e, KeyListener::keyReleased, controller::keyReleased);
        }


        /**
         * Resolves the given event by distributing it to the overlays and the top-level controller. The
         * distribution-process stops as soon as any overlay fully consumes the event (i.e. the callback of this event
         * returns {@code true}) and thus blocks subsequent overlays from receiving this event.
         *
         * @param e        the event to resolveKeyEvent.
         * @param fn       the function to be executed for each overlay.
         * @param toplevel the top-level receiver of the event.
         */
        private void resolve(KeyEvent e, KeyListenerFunction fn, Consumer<KeyEvent> toplevel) {
            ArrayList<KeyListener> listeners = getAllOverlays().stream()
                    .map(Overlay::getKeyListener)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(ArrayList::new));

            for (int i = listeners.size() - 1; i >= 0; i--) {
                fn.call(listeners.get(i), e);
                if (e.isConsumed())
                    return;
            }

            toplevel.accept(e);
        }
    }


    private interface MouseListenerFunction {
        void call(MouseListener listener, MouseEvent event);
    }

    private interface KeyListenerFunction {
        void call(KeyListener listener, KeyEvent event);
    }
}