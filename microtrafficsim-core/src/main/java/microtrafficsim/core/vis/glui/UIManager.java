package microtrafficsim.core.vis.glui;

import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseListener;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.view.OrthographicView;


public abstract class UIManager {

    private EventResolver resolver;
    private Component focus;

    public UIManager() {
        this.resolver = new EventResolver(this);
        this.focus = null;
    }


    public abstract void redraw(Component component);
    public abstract OrthographicView getView();
    public abstract Component getRootComponent();
    public abstract RenderContext getContext();


    public void setEventsEnabled(boolean enabled) {
        resolver.setEnabled(enabled);
    }

    public boolean hasEventsEnabled() {
        return resolver.isEnabled();
    }


    public void setFocus(Component component) {
        if (this.focus == component) return;

        if (this.focus != null) {
            this.focus.focused = false;
            redraw(this.focus);
        }

        this.focus = component;
        if (component != null) {
            component.focused = true;
            redraw(component);
        }
    }

    public Component getFocus() {
        return focus;
    }


    public EventResolver getEventResolver() {
        return resolver;
    }

    public KeyListener getKeyListener() {
        return resolver;
    }

    public MouseListener getMouseListener() {
        return resolver;
    }
}
