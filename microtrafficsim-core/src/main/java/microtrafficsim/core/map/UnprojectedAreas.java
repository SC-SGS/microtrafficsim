package microtrafficsim.core.map;

import microtrafficsim.core.map.area.polygons.TypedPolygonArea;
import microtrafficsim.core.vis.map.projections.Projection;

import java.util.LinkedHashSet;

/**
 * @author Dominic Parga Cacheiro
 */
public class UnprojectedAreas extends LinkedHashSet<TypedPolygonArea> {

    public ProjectedAreas toProjectedAreas(Projection projection) {
        ProjectedAreas areas = new ProjectedAreas();

        for (TypedPolygonArea area : this) {
            areas.add(area.getProjectedArea(projection, area.getType()));
        }

        return areas;
    }
}
