package microtrafficsim.ui.preferences.model;

import microtrafficsim.core.simulation.configs.SimulationConfig;

/**
 * @author Dominic Parga Cacheiro
 */
public class PreferencesModel {

    private final String TITLE;
    private final SimulationConfig.EnableLexicon lexicon;

    public PreferencesModel(String title) {
        TITLE = title;
        lexicon = new SimulationConfig.EnableLexicon();
    }


    public String getTitle() {
        return TITLE;
    }

    public SimulationConfig.EnableLexicon getEnableLexicon() {
        return lexicon;
    }
}