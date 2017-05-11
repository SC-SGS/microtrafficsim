package microtrafficsim.core.convenience.filechoosing;

import microtrafficsim.core.convenience.utils.FileFilters;


/**
 * @author Maximilian Luz
 */
public class MapfileChooser extends MTSFileChooser {

    public MapfileChooser() {
        super();

        saveFilters.add(FileFilters.MAP_EXFMT);
        saveFilters.add(super.getAcceptAllFileFilter());

        openFilters.add(FileFilters.MAP_ALL);
        openFilters.add(FileFilters.MAP_EXFMT);
        openFilters.add(FileFilters.MAP_OSM_XML);
        openFilters.add(super.getAcceptAllFileFilter());

        saveSelected = saveFilters.get(0);
        openSelected = openFilters.get(0);
    }
}
