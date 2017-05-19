package microtrafficsim.core.exfmt.ecs;

import microtrafficsim.utils.collections.Composite;


public abstract class Entity extends Composite<Component> {
    private long id;

    public Entity(long id) {
        this.id = id;
    }


    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public Component set(Component component) {
        return super.getAll().put(component.getType(), component);
    }
}
