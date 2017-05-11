package microtrafficsim.core.exfmt.ecs.entities;

import microtrafficsim.core.exfmt.ecs.Entity;
import microtrafficsim.core.map.Coordinate;


public class LineEntity extends Entity {
    private Coordinate[] coordinates;


    public LineEntity(long id, Coordinate[] coordinates) {
        super(id);
        this.coordinates = coordinates;
    }

    public Coordinate[] getCoordinates() {
        return this.coordinates;
    }

    public void setCoordinates(Coordinate[] coordinates) {
        this.coordinates = coordinates;
    }
}
