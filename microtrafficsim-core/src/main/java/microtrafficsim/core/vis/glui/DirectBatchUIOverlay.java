package microtrafficsim.core.vis.glui;

import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import microtrafficsim.core.vis.Overlay;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.view.OrthographicView;
import microtrafficsim.math.Rect2d;
import microtrafficsim.math.Vec2d;

import java.util.ArrayList;


/**
 * User Interface designed to be used in {@link microtrafficsim.core.vis.Overlay Overlay}s.
 *
 * @author Maximilian Luz
 */
public class DirectBatchUIOverlay extends UIManager implements Overlay {

    private OrthographicView view;
    private boolean enabled = true;

    private RenderContext context;
    private BatchUIRenderer renderer;
    private Component root;

    private final MouseListener mouseListener;


    public DirectBatchUIOverlay() {
        mouseListener = new MouseListenerImpl();
        root = new RootComponent();
        renderer = new BatchUIRenderer(root);
        root.setBounds(new Rect2d(-Double.MAX_VALUE, -Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE));
        root.setUIManager(this);
        root.setFocused(true);
    }


    @Override
    public void redraw(Component component) {
        renderer.redraw(component);
    }

    @Override
    public OrthographicView getView() {
        return view;
    }

    @Override
    public Component getRootComponent() {
        return root;
    }

    public RenderContext getContext() {
        return context;
    }


    @Override
    public void initialize(RenderContext context) throws Exception {
        this.context = context;
        renderer.initialize(context);
    }

    @Override
    public void dispose(RenderContext context) throws Exception {
        renderer.dispose(context);
    }

    @Override
    public void resize(RenderContext context) throws Exception {}

    @Override
    public void display(RenderContext context, MapBuffer map) throws Exception {
        if (!enabled) return;

        // disable depth test
        context.DepthTest.disable(context.getDrawable().getGL());

        renderer.update(context);
        renderer.display(context);
    }


    public void addComponent(Component c) {
        root.addComponent(c);
    }

    public ArrayList<Component> getComponents() {
        return root.getComponents();
    }


    @Override
    public void setView(OrthographicView view) {
        this.view = view;
    }


    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }


    @Override
    public MouseListener getMouseListener() {
        return mouseListener;
    }


    private static class RootComponent extends Component {

        @Override
        public boolean contains(Vec2d p) {
            return true;
        }

        @Override
        protected void updateBounds() {}
    }

    private class MouseListenerImpl implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            DirectBatchUIOverlay.super.getMouseListener().mouseClicked(e);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            DirectBatchUIOverlay.super.getMouseListener().mouseEntered(e);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            DirectBatchUIOverlay.super.getMouseListener().mouseExited(e);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            DirectBatchUIOverlay.super.getMouseListener().mousePressed(e);

            // NOTE:
            // the underlying input-controller for the visualization requires _all_ pressed/released events
            // for consistent drag-vector calculation.
            e.setConsumed(false);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            DirectBatchUIOverlay.super.getMouseListener().mouseReleased(e);

            // NOTE:
            // the underlying input-controller for the visualization requires _all_ pressed/released events
            // for consistent drag-vector calculation.
            e.setConsumed(false);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            DirectBatchUIOverlay.super.getMouseListener().mouseMoved(e);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            DirectBatchUIOverlay.super.getMouseListener().mouseDragged(e);
        }

        @Override
        public void mouseWheelMoved(MouseEvent e) {
            DirectBatchUIOverlay.super.getMouseListener().mouseWheelMoved(e);
        }
    }
}
