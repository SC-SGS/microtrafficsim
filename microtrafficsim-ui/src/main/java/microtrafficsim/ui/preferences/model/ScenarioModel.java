package microtrafficsim.ui.preferences.model;

import microtrafficsim.core.simulation.scenarios.Scenario;

import java.util.ArrayList;

/**
 * @author Dominic Parga Cacheiro
 */
public class ScenarioModel extends PreferencesModel {

    private final ArrayList<Class<? extends Scenario>> scenarios;

    public ScenarioModel() {
        super("Scenario");

        scenarios = new ArrayList<>();
    }


    public void clearAllScenarios() {
        scenarios.clear();
    }

    public void addScenario(Class<? extends Scenario> scenario) {
        scenarios.add(scenario);
    }

    public ArrayList<Class<? extends Scenario>> getScenarios() {
        return new ArrayList<>(scenarios);
    }

    public Class<? extends Scenario> get(int index) {
        return scenarios.get(index);
    }
}