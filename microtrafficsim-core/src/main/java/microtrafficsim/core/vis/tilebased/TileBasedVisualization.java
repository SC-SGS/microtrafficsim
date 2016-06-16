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


public class TileBasedVisualization extends AbstractVisualization {

    private static final int ZOOM_LEVEL_MIN = 0;
    private static final int ZOOM_LEVEL_MAX = 19;
    private static final double ZOOM_FACTOR = 0.1;

    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 1000.f;

    private OrthoInputController controller;
    private MouseListener mouseController = new MouseControllerImpl();
    private KeyController keyController = new KeyControllerImpl();


    public TileBasedVisualization(int width, int height, TileProvider provider, int nWorkerThreads) {
        this(width, height, provider, InterruptSafeExecutors.newFixedThreadPool(nWorkerThreads));
    }

    public TileBasedVisualization(int width, int height, TileProvider provider, ExecutorService worker) {
        this(new OrthographicView(width, height, Z_NEAR, Z_FAR, ZOOM_LEVEL_MIN, ZOOM_LEVEL_MAX), provider, worker);
    }

    private TileBasedVisualization(OrthographicView view, TileProvider provider, ExecutorService worker) {
        this(new RenderContext(), view, provider, worker);
    }

    private TileBasedVisualization(RenderContext context, OrthographicView view, TileProvider provider, ExecutorService worker) {
        super(context, new TileBasedVisualizer(context, view, provider, worker));
        this.controller = new OrthoInputController(view, ZOOM_FACTOR);
    }


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

        @Override
        public void mouseExited(MouseEvent e) {
            resolve(e, Overlay.MouseListener::mouseExited, controller::mouseExited);
        }


        private void resolve(MouseEvent e, BiPredicate<Overlay.MouseListener, MouseEvent> call, Consumer<MouseEvent> toplevel) {
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
