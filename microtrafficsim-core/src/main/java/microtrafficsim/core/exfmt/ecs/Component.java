package microtrafficsim.core.exfmt.ecs;


public abstract class Component {
    private Entity entity;

    public Component(Entity entity) {
        this.entity = entity;
    }


    public Class<? extends Component> getType() {
        return this.getClass();
    }


    public Entity getEntity() {
        return this.entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }
}
