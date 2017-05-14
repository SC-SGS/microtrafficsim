package microtrafficsim.core.convenience.filechoosing.impl;

import microtrafficsim.core.convenience.filechoosing.MTSFileChooser;

/**
 * @author Dominic Parga Cacheiro
 */
public class RouteFilterSet extends MTSFileChooser.FilterSet {
    public RouteFilterSet() {
        super();

        getSaveFilters().add(MTSFileChooser.Filters.ROUTE);
        addAllSaveFilters = true;

        getOpenFilters().add(MTSFileChooser.Filters.ROUTE);
        addAllOpenFilters = true;

        saveSelected = getSaveFilters().get(0);
        openSelected = getOpenFilters().get(0);
    }
}
