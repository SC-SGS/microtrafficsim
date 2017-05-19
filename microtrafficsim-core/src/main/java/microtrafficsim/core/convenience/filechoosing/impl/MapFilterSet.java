package microtrafficsim.core.convenience.filechoosing.impl;

import microtrafficsim.core.convenience.filechoosing.MTSFileChooser;

/**
 * @author Dominic Parga Cacheiro
 */
public class MapFilterSet extends MTSFileChooser.FilterSet {
    public MapFilterSet() {
        super();

        getSaveFilters().add(MTSFileChooser.Filters.MAP_EXFMT);
        addAllSaveFilters = true;

        getOpenFilters().add(MTSFileChooser.Filters.MAP_ALL);
        getOpenFilters().add(MTSFileChooser.Filters.MAP_EXFMT);
        getOpenFilters().add(MTSFileChooser.Filters.MAP_OSM_XML);
        addAllOpenFilters = true;

        saveSelected = getSaveFilters().get(0);
        openSelected = getOpenFilters().get(0);
    }
}
