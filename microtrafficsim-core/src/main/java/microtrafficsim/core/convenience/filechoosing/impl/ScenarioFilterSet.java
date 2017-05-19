package microtrafficsim.core.convenience.filechoosing.impl;

import microtrafficsim.core.convenience.filechoosing.MTSFileChooser;

/**
 * @author Dominic Parga Cacheiro
 */
public class ScenarioFilterSet extends MTSFileChooser.FilterSet {
    public ScenarioFilterSet() {
        super();

        getSaveFilters().add(MTSFileChooser.Filters.SCENARIO);
        addAllSaveFilters = true;

        getOpenFilters().add(MTSFileChooser.Filters.SCENARIO);
        addAllOpenFilters = true;

        saveSelected = getSaveFilters().get(0);
        openSelected = getOpenFilters().get(0);
    }
}
