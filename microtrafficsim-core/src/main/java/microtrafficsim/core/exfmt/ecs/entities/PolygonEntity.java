package microtrafficsim.core.exfmt.ecs.entities;

import microtrafficsim.core.exfmt.ecs.Entity;
import microtrafficsim.core.map.Coordinate;


public class PolygonEntity extends Entity {
    private Coordinate[] outline;


    public PolygonEntity(long id, Coordinate[] outline) {
        super(id);
        this.outline = outline;
    }

    public Coordinate[] getOutline() {
        return this.outline;
    }

    public void setOutline(Coordinate[] outline) {
        this.outline = outline;
    }
}
