package microtrafficsim.core.vis.glui;

import com.jogamp.newt.event.KeyListener;
import microtrafficsim.core.vis.glui.events.MouseListener;
import microtrafficsim.core.vis.glui.renderer.ComponentRenderPass;
import microtrafficsim.math.Mat3d;
import microtrafficsim.math.Rect2d;
import microtrafficsim.math.Vec2d;

import java.util.ArrayList;


public abstract class Component {

    protected UIManager manager = null;

    protected ComponentRenderPass[] renderer = null;

    protected Mat3d transform = Mat3d.identity();
    protected Mat3d invtransform = Mat3d.identity();
    protected Rect2d aabb = null;

    protected Component parent = null;
    protected ArrayList<Component> children = new ArrayList<>();

    protected boolean focusable = true;
    protected boolean focused = false;

    protected boolean mouseover = false;

    protected boolean active = true;
    protected boolean visible = true;

    private ArrayList<MouseListener> mouseListeners = new ArrayList<>();
    private ArrayList<KeyListener> keyListeners = new ArrayList<>();


    protected Component(ComponentRenderPass... renderer) {
        this.renderer = renderer;
    }


    protected void setUIManager(UIManager manager) {
        this.manager = manager;
        for (Component c : children)
            c.setUIManager(manager);

        if (manager != null)
            manager.redraw(this);
    }

    public UIManager getUIManager() {
        return manager;
    }

    protected ComponentRenderPass[] getRenderPasses() {
        return renderer;
    }


    public void setTransform(Mat3d transform) {
        this.transform = transform;
        this.invtransform = Mat3d.invert(transform);
        redraw();
    }

    public Mat3d getTransform() {
        return transform;
    }

    public Mat3d getInverseTransform() {
        return invtransform;
    }


    protected void setBounds(Rect2d aabb) {
        this.aabb = aabb;
        redraw();
    }

    protected Rect2d getBounds() {
        return aabb;
    }


    protected abstract boolean contains(Vec2d p);


    public Component getParent() {
        return parent;
    }


    protected void add(Component child) {
        if (child.parent != null)
            child.parent.remove(child);

        children.add(child);
        child.parent = this;
        child.setUIManager(manager);

        updateBounds();
        redraw();
    }

    protected boolean remove(Component child) {
        if (children.remove(child)) {
            child.parent = null;
            child.setUIManager(null);

            updateBounds();
            redraw();

            return true;
        }

        return false;
    }

    protected ArrayList<Component> getComponents() {
        return children;
    }


    protected void updateBounds() {
        Rect2d aabb = new Rect2d(Double.MAX_VALUE, Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);

        for (Component c : children) {
            Rect2d cbb = Rect2d.transform(c.transform, c.getBounds());

            if (aabb.xmin > cbb.xmin) aabb.xmin = cbb.xmin;
            if (aabb.xmax < cbb.xmax) aabb.xmax = cbb.xmax;
            if (aabb.ymin > cbb.ymin) aabb.ymin = cbb.ymin;
            if (aabb.ymax < cbb.ymax) aabb.ymax = cbb.ymax;
        }

        this.aabb = aabb;
    }

    public void redraw(boolean recursive) {
        if (manager != null) {
            if (recursive)
                for (Component c : children)
                    c.redraw(true);

            manager.redraw(this);
        }
    }

    public void redraw() {
        redraw(false);
    }


    public void setFocusable(boolean focusable) {
        this.focusable = focusable;

        if (manager != null)
            manager.redraw(this);
    }

    public boolean isFocusable() {
        return focusable;
    }


    public void setFocused(boolean focused) {
        if (!focusable) return;
        if (this.manager != null)
            this.manager.setFocus(this);
    }

    public boolean isFocused() {
        return focused;
    }


    public boolean isMouseOver() {
        return mouseover;
    }


    public void setActive(boolean active) {
        this.active = active;

        if (manager != null)
            manager.redraw(this);
    }

    public boolean isActive() {
        return active;
    }


    public void setVisible(boolean visible) {
        this.visible = visible;

        if (manager != null)
            manager.redraw(this);
    }

    public boolean isVisible() {
        return visible;
    }


    public void addMouseListener(MouseListener listener) {
        mouseListeners.add(listener);
    }

    public void removeMouseListener(MouseListener listener) {
        mouseListeners.remove(listener);
    }

    public ArrayList<MouseListener> getMouseListeners() {
        return mouseListeners;
    }


    public void addKeyListener(KeyListener listener) {
        keyListeners.add(listener);
    }

    public void removeKeyListener(KeyListener listener) {
        keyListeners.remove(listener);
    }

    public ArrayList<KeyListener> getKeyListeners() {
        return keyListeners;
    }
}
