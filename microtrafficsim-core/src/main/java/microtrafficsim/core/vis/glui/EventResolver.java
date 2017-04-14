package microtrafficsim.core.vis.glui;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import microtrafficsim.core.vis.glui.events.MouseEvent;
import microtrafficsim.core.vis.glui.events.MouseListener;
import microtrafficsim.core.vis.view.OrthographicView;
import microtrafficsim.math.*;

import java.util.HashSet;
import java.util.LinkedList;


public class EventResolver implements KeyListener, com.jogamp.newt.event.MouseListener {
    // TODO: proper support for multi-pointer events?

    private UIManager manager;
    private LinkedList<ChainNode> lastdown;
    private HashSet<ChainNode> mouseover;

    public EventResolver(UIManager manager) {
        this.manager = manager;
    }


    @Override
    public void keyPressed(KeyEvent e) {
        resolveKeyEvent(e, KeyListener::keyPressed);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        resolveKeyEvent(e, KeyListener::keyReleased);
    }


    @Override
    public void mouseClicked(com.jogamp.newt.event.MouseEvent e) {
        resolveMouseEvent(getResolveChain(e), MouseListener::mouseClicked);
    }

    @Override
    public void mousePressed(com.jogamp.newt.event.MouseEvent e) {
        lastdown = getResolveChain(e);
        updateFocus(lastdown);
        resolveMouseEvent(lastdown, MouseListener::mousePressed);
    }

    @Override
    public void mouseReleased(com.jogamp.newt.event.MouseEvent e) {
        resolveMouseEvent(lastdown, MouseListener::mouseReleased, e);
    }


    @Override
    public void mouseEntered(com.jogamp.newt.event.MouseEvent e) {
        resolveMouseEnter(e);
    }

    @Override
    public void mouseExited(com.jogamp.newt.event.MouseEvent e) {
        resolveMouseExit(e);
    }


    @Override
    public void mouseMoved(com.jogamp.newt.event.MouseEvent e) {
        LinkedList<ChainNode> chain = getResolveChain(e);
        resolveMouseEnterExit(chain, e);
        resolveMouseEvent(chain, MouseListener::mouseMoved);
    }

    @Override
    public void mouseDragged(com.jogamp.newt.event.MouseEvent e) {
        resolveMouseEvent(lastdown, MouseListener::mouseDragged, e);
    }

    @Override
    public void mouseWheelMoved(com.jogamp.newt.event.MouseEvent e) {
        resolveMouseEvent(getResolveChain(e), MouseListener::mouseWheelMoved);
    }



    private void updateFocus(LinkedList<ChainNode> chain) {
        for (ChainNode node : chain) {
            if (node.component.isFocusable()) {
                manager.setFocus(node.component);
                return;
            }
        }
    }

    private void resolveKeyEvent(KeyEvent event, KeyListenerFunction fn) {
        Component component = manager.getFocus();
        while (component != null) {
            for (KeyListener listener : component.getKeyListeners()) {
                fn.call(listener, event);
                if (event.isConsumed())
                    return;
            }

            component = component.getParent();
        }
    }

    private void resolveMouseEvent(LinkedList<ChainNode> chain, MouseListenerFunction fn) {
        for (ChainNode node : chain) {
            for (MouseListener listener : node.component.getMouseListeners()) {
                fn.call(listener, node.event);

                if (node.event.isConsumed())
                    return;
            }
        }
    }

    private void resolveMouseEvent(LinkedList<ChainNode> chain, MouseListenerFunction fn, com.jogamp.newt.event.MouseEvent base) {
        OrthographicView view = manager.getView();
        Rect2i viewport = new Rect2i(0, view.getSize().y, view.getSize().x, 0);
        MouseEvent tbase = translate(base, viewport, view.getViewportBounds());

        for (ChainNode node : chain) {
            MouseEvent event = transform(node.transform, tbase);

            for (MouseListener listener : node.component.getMouseListeners()) {
                fn.call(listener, event);

                if (event.isConsumed())
                    return;
            }
        }
    }

    private void resolveMouseEnter(com.jogamp.newt.event.MouseEvent e) {
        MouseEvent base = createMouseEnterEvent(e);
        HashSet<ChainNode> over = new HashSet<>();

        for (ChainNode node : getResolveChain(e)) {
            MouseEvent event = transform(node.transform, base);
            if (node.component.contains(event.getPointer())) {
                over.add(node);
                node.component.mouseover = true;

                for (MouseListener listener : node.component.getMouseListeners())
                    listener.mouseEntered(event);
            }
        }

        this.mouseover = over;
    }

    private void resolveMouseExit(com.jogamp.newt.event.MouseEvent e) {
        if (mouseover == null) return;

        MouseEvent base = createMouseExitEvent(e);

        for (ChainNode node : mouseover) {
            MouseEvent event = transform(node.transform, base);
            node.component.mouseover = false;
            for (MouseListener listener : node.component.getMouseListeners())
                listener.mouseExited(event);
        }

        mouseover = null;
    }

    private void resolveMouseEnterExit(LinkedList<ChainNode> chain, com.jogamp.newt.event.MouseEvent e) {
        // get all components where the mouse is over
        HashSet<ChainNode> newover = new HashSet<>();
        HashSet<ChainNode> allover = new HashSet<>();

        for (ChainNode node : chain) {
            if (node.component.contains(node.event.getPointer())) {
                allover.add(node);

                if (!node.component.mouseover)
                    newover.add(node);
            }
        }

        // exit events (call exit before enter)
        MouseEvent exit = createMouseExitEvent(e);
        if (mouseover != null) {
            mouseover.removeAll(allover);
            for (ChainNode node : mouseover) {
                node.component.mouseover = false;

                MouseEvent event = transform(node.transform, exit);
                for (MouseListener listener : node.component.getMouseListeners())
                    listener.mouseExited(event);
            }
        }

        // enter events
        for (ChainNode node : newover) {
            node.component.mouseover = true;

            MouseEvent event = node.event.createVariant(MouseEvent.EVENT_MOUSE_ENTERED);
            for (MouseListener listener : node.component.getMouseListeners())
                listener.mouseEntered(event);
        }

        mouseover = allover;
    }


    private LinkedList<ChainNode> getResolveChain(com.jogamp.newt.event.MouseEvent event) {
        OrthographicView view = manager.getView();
        Rect2i viewport = new Rect2i(0, view.getSize().y, view.getSize().x, 0);

        // dfs search for pointer
        Component root = manager.getRootComponent();
        LinkedList<ChainNode> stack = new LinkedList<>();
        LinkedList<ChainNode> queue = new LinkedList<>();

        MouseEvent e = translate(event, viewport, view.getViewportBounds());
        e = transform(root.getInverseTransform(), e);

        if (root.getBounds().contains(e.getPointer()))
            stack.push(new ChainNode(root, root.getInverseTransform(), e, false));

        while (!stack.isEmpty()) {
            ChainNode node = stack.poll();

            if (!node.visited) {        // lastdown-traversal: add children
                node.visited = true;
                stack.push(node);

                for (int i = node.component.getComponents().size() - 1; i >= 0; i--) {
                    Component child = node.component.getComponents().get(i);
                    if (!child.isVisible())
                        continue;

                    MouseEvent evt = transform(node.component.getInverseTransform(), node.event);
                    if (child.getBounds().contains(evt.getPointer())) {
                        Mat3d m = Mat3d.mul(node.transform, node.component.getInverseTransform());
                        stack.push(new ChainNode(child, m, evt, false));
                    }
                }

            } else {                    // up-traversal: add to queue
                if (node.component.contains(node.event.getPointer()))
                    queue.add(node);
            }
        }

        return queue;
    }


    private MouseEvent createMouseEnterEvent(com.jogamp.newt.event.MouseEvent event) {
        return createMouseEventWithType(event, com.jogamp.newt.event.MouseEvent.EVENT_MOUSE_ENTERED);
    }

    private MouseEvent createMouseExitEvent(com.jogamp.newt.event.MouseEvent event) {
        return createMouseEventWithType(event, com.jogamp.newt.event.MouseEvent.EVENT_MOUSE_EXITED);
    }

    private MouseEvent createMouseEventWithType(com.jogamp.newt.event.MouseEvent from, short type) {
        OrthographicView view = manager.getView();
        Rect2i viewport = new Rect2i(0, view.getSize().y, view.getSize().x, 0);

        return translate(from.createVariant(type), viewport, view.getViewportBounds());
    }


    private static MouseEvent translate(com.jogamp.newt.event.MouseEvent event, Rect2i from, Rect2d to) {
        Vec2d[] pointer = new Vec2d[event.getPointerCount()];
        Rect2d f = Rect2d.from(from);

        for (int i = 0; i < pointer.length; i++)
            pointer[i] = Rect2d.project(f, to, new Vec2d(event.getAllX()[i], event.getAllY()[i]));

        return new MouseEvent(pointer, event);
    }

    private static Vec2d transform(Mat3d m, Vec2d p) {
        Vec3d v = m.mul(new Vec3d(p.x, p.y, 1.0));
        return new Vec2d(v.x / v.z, v.y / v.z);
    }

    private static Vec2d[] transform(Mat3d m, Vec2d[] p) {
        Vec2d[] result = new Vec2d[p.length];

        for (int i = 0; i < p.length; i++)
            result[i] = transform(m, p[i]);

        return result;
    }

    private static MouseEvent transform(Mat3d m, MouseEvent event) {
        return new MouseEvent(transform(m, event.getAllPointers()), event.getBaseEvent());
    }


    private static class ChainNode {
        Component component;
        Mat3d transform;
        MouseEvent event;
        boolean visited;

        ChainNode(Component component, Mat3d transform, MouseEvent event, boolean visited) {
            this.component = component;
            this.transform = transform;
            this.event = event;
            this.visited = visited;
        }

        @Override
        public int hashCode() {
            return component.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof ChainNode) && this.component == ((ChainNode) obj).component;
        }
    }

    private interface KeyListenerFunction {
        void call(KeyListener listener, KeyEvent event);
    }

    private interface MouseListenerFunction {
        void call(MouseListener listener, MouseEvent event);
    }
}
