package microtrafficsim.ui.preferences.model;

import microtrafficsim.core.simulation.configs.ScenarioConfig;

/**
 * @author Dominic Parga Cacheiro
 */
public class CrossingLogicModel extends PreferencesModel {

    private final String[] combos = new String[] {
            "priority to the left",
            "priority to the right",
            "priority to the random"
    };

    public CrossingLogicModel() {
        super("Crossing logic");
    }

    public String[] getCombosPriorityToThe() {
        return combos;
    }

    public String getSelectedItem(ScenarioConfig config) {
        if (config.crossingLogic.priorityToTheRightEnabled)
            return combos[config.crossingLogic.drivingOnTheRight ? 1 : 0];
        else
            return combos[2];
    }

    public void updateConfig(ScenarioConfig config, int selectedIndex) {
        switch (selectedIndex) {
            case 0:    // combos[0] = "priority to the left"
                config.crossingLogic.priorityToTheRightEnabled = true;
                config.crossingLogic.drivingOnTheRight         = false;
                break;

            case 1:    // combos[1] = "priority to the right"
                config.crossingLogic.priorityToTheRightEnabled = true;
                config.crossingLogic.drivingOnTheRight         = true;
                break;

            case 2:    // combos[2] = "priority to the random"
                config.crossingLogic.priorityToTheRightEnabled = false;
                break;
        }
    }
}