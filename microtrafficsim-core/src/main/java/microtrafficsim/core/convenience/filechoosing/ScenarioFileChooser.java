package microtrafficsim.core.convenience.filechoosing;

import microtrafficsim.core.convenience.utils.FileFilters;


/**
 * @author Dominic Parga Cacheiro
 */
public class ScenarioFileChooser extends MTSFileChooser {

    public ScenarioFileChooser() {
        super();

        saveFilters.add(FileFilters.SCENARIO);
        saveFilters.add(super.getAcceptAllFileFilter());

        openFilters.add(FileFilters.SCENARIO);
        openFilters.add(super.getAcceptAllFileFilter());

        saveSelected = saveFilters.get(0);
        openSelected = openFilters.get(0);
    }
}
