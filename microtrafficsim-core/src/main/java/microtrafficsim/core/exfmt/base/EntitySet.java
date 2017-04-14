package microtrafficsim.core.exfmt.base;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ecs.entities.LineEntity;
import microtrafficsim.core.exfmt.ecs.entities.PointEntity;
import microtrafficsim.core.exfmt.ecs.entities.PolygonEntity;
import microtrafficsim.core.map.Bounds;
import microtrafficsim.math.Rect2d;

import java.util.HashMap;
import java.util.Map;


// contains all entities
public class EntitySet extends Container.Entry {
    private Bounds bounds;

    private Map<Long, PointEntity> points = new HashMap<>();
    private Map<Long, LineEntity> lines = new HashMap<>();
    private Map<Long, PolygonEntity> polygons = new HashMap<>();


    public EntitySet() {
        this(null);
    }

    public EntitySet(Bounds bounds) {
        this.bounds = bounds;
    }


    public Bounds getBounds() {
        return this.bounds;
    }

    public void setBounds(Bounds bounds) {
        this.bounds = bounds;
    }

    public void updateBounds(Bounds bounds) {
        if (this.bounds != null)
            this.bounds.join(bounds);
        else
            this.bounds = bounds;
    }


    public Map<Long, PointEntity> getPoints() {
        return points;
    }

    public Map<Long, LineEntity> getLines() {
        return lines;
    }

    public Map<Long, PolygonEntity> getPolygons() {
        return polygons;
    }
}
