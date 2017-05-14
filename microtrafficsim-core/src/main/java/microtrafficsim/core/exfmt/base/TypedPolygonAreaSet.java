package microtrafficsim.core.exfmt.base;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.map.area.polygons.TypedPolygonArea;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


public class TypedPolygonAreaSet extends Container.Entry {
    private HashSet<TypedPolygonArea> areas = new HashSet<>();


    public boolean add(TypedPolygonArea area) {
        return this.areas.add(area);
    }

    public boolean addAll(Collection<? extends TypedPolygonArea> areas) {
        return this.areas.addAll(areas);
    }

    public boolean remove(TypedPolygonArea area) {
        return this.remove(area);
    }

    public void clear() {
        this.areas.clear();
    }

    public Set<TypedPolygonArea> getAll() {
        return this.areas;
    }
}
