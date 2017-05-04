package microtrafficsim.core.exfmt.ecs.entities;

import microtrafficsim.core.exfmt.ecs.Entity;
import microtrafficsim.core.map.Coordinate;


public class PointEntity extends Entity {
    private Coordinate coordinate;


    public PointEntity(long id, Coordinate coordinate) {
        super(id);
        this.coordinate = coordinate;
    }

    public Coordinate getCoordinate() {
        return this.coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }
}
