package microtrafficsim.core.convenience.filechoosing;

import microtrafficsim.core.convenience.utils.FileFilters;

/**
 * @author Dominic Parga Cacheiro
 */
public class ConfigFileChooser extends MTSFileChooser {

    public ConfigFileChooser() {
        saveFilters.add(FileFilters.CONFIG);
        saveFilters.add(super.getAcceptAllFileFilter());

        openFilters.add(FileFilters.CONFIG);
        openFilters.add(super.getAcceptAllFileFilter());

        saveSelected = saveFilters.get(0);
        openSelected = openFilters.get(0);
    }
}
