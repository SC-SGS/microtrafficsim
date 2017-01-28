package microtrafficsim.core.vis;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.input.KeyCommand;
import microtrafficsim.core.vis.input.KeyController;
import microtrafficsim.core.vis.input.OrthoInputController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.stream.Collectors;


/**
 * Abstract implementation of the {@code Visualization}, providing the basic features.
 *
 * @author Maximilian Luz
 */
public abstract class AbstractVisualization implements Visualization {

    private RenderContext context;
    private Visualizer    visualizer;

    private MouseListener        mouseController = new AbstractVisualization.MouseControllerImpl();
    private KeyController        keyController   = new AbstractVisualization.KeyControllerImpl();


    /**
     * Constructs a new {@code AbstractVisualization} for the given context and visualizer.
     *
     * @param context    the {@code RenderContext} on which this visualization is going to be displayed.
     * @param visualizer the visualizer providing the actual render-code.
     */
    public AbstractVisualization(RenderContext context, Visualizer visualizer) {
        this.context    = context;
        this.visualizer = visualizer;

        context.setRenderer(visualizer);
    }


    @Override
    public VisualizerConfig getDefaultConfig() throws UnsupportedFeatureException {
        return visualizer.getDefaultConfig();
    }


    @Override
    public RenderContext getRenderContext() {
        return context;
    }

    @Override
    public Visualizer getVisualizer() {
        return visualizer;
    }


    @Override
    public MouseListener getMouseController() {
        return mouseController;
    }

    @Override
    public KeyController getKeyController() {
        return keyController;
    }


    @Override
    public Overlay putOverlay(int index, Overlay overlay) {
        return visualizer.putOverlay(index, overlay);
    }

    @Override
    public Overlay removeOverlay(int index) {
        return visualizer.removeOverlay(index);
    }

    @Override
    public Overlay getOverlay(int index) {
        return visualizer.getOverlay(index);
    }

    @Override
    public Collection<Overlay> getAllOverlays() {
        return visualizer.getAllOverlays();
    }


    protected abstract OrthoInputController getController();


    /**
     * Basic key-listener to distribute the mouse-events to the specific overlays and finally the controller of
     * this visualization.
     */
    private class MouseControllerImpl implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            resolve(e, Overlay.MouseListener::mouseClicked, getController()::mouseClicked);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            resolve(e, Overlay.MouseListener::mousePressed, getController()::mousePressed);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            resolve(e, Overlay.MouseListener::mouseReleased, getController()::mouseReleased);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            resolve(e, Overlay.MouseListener::mouseMoved, getController()::mouseMoved);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            resolve(e, Overlay.MouseListener::mouseDragged, getController()::mouseDragged);
        }

        @Override
        public void mouseWheelMoved(MouseEvent e) {
            resolve(e, Overlay.MouseListener::mouseWheelMoved, getController()::mouseWheelMoved);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            resolve(e, Overlay.MouseListener::mouseEntered, getController()::mouseEntered);
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
            resolve(e, Overlay.MouseListener::mouseExited, getController()::mouseExited);
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
            return getController().addKeyCommand(event, vk, command);
        }

        @Override
        public KeyCommand removeKeyCommand(short event, short vk) {
            return getController().removeKeyCommand(event, vk);
        }

        @Override
        public void keyPressed(KeyEvent e) {
            resolve(e, Overlay.KeyListener::keyPressed, getController()::keyPressed);
        }

        @Override
        public void keyReleased(KeyEvent e) {
            resolve(e, Overlay.KeyListener::keyReleased, getController()::keyReleased);
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
