package microtrafficsim.ui.preferences.model;

import microtrafficsim.ui.gui.statemachine.GUIController;

/**
 * @author Dominic Parga Cacheiro
 */
public class PreferencesFrameModel extends PreferencesModel {

    private final GUIController guiController;

    public PreferencesFrameModel(String title, GUIController guiController) {
        super(title);
        this.guiController = guiController;
    }


    public GUIController getGuiController() {
        return guiController;
    }
}
