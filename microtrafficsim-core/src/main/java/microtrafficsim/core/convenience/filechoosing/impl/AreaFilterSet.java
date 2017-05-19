package microtrafficsim.core.convenience.filechoosing.impl;

import microtrafficsim.core.convenience.filechoosing.MTSFileChooser;

/**
 * @author Dominic Parga Cacheiro
 */
public class AreaFilterSet extends MTSFileChooser.FilterSet {
    public AreaFilterSet() {
        super();

        getSaveFilters().add(MTSFileChooser.Filters.AREA);
        addAllSaveFilters = true;

        getOpenFilters().add(MTSFileChooser.Filters.AREA);
        addAllOpenFilters = true;

        saveSelected = getSaveFilters().get(0);
        openSelected = getOpenFilters().get(0);
    }
}
