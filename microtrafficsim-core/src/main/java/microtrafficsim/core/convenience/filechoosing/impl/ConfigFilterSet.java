package microtrafficsim.core.convenience.filechoosing.impl;

import microtrafficsim.core.convenience.filechoosing.MTSFileChooser;

/**
 * @author Dominic Parga Cacheiro
 */
public class ConfigFilterSet extends MTSFileChooser.FilterSet {
    public ConfigFilterSet() {
        super();

        getSaveFilters().add(MTSFileChooser.Filters.CONFIG);
        addAllSaveFilters = true;

        getOpenFilters().add(MTSFileChooser.Filters.CONFIG);
        addAllOpenFilters = true;

        saveSelected = getSaveFilters().get(0);
        openSelected = getOpenFilters().get(0);
    }
}
