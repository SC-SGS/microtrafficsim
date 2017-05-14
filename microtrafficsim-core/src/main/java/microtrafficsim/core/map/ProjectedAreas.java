package microtrafficsim.core.map;

import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.core.vis.scenario.areas.Area;

import java.util.ArrayList;

/**
 * @author Dominic Parga Cacheiro
 */
public class ProjectedAreas extends ArrayList<Area> {

    public UnprojectedAreas toUnprojectedAreas(Projection projection) {
        UnprojectedAreas areas = new UnprojectedAreas();

        for (Area area : this) {
            areas.add(area.getUnprojectedArea(projection));
        }

        return areas;
    }
}
