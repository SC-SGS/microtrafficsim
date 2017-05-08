package microtrafficsim.ui.preferences.model;

import microtrafficsim.core.simulation.scenarios.Scenario;
import microtrafficsim.utils.Descriptor;

import java.util.ArrayList;

/**
 * @author Dominic Parga Cacheiro
 */
public class ScenarioModel extends PreferencesModel {

    private final ArrayList<Descriptor<Class<? extends Scenario>>> scenarios;

    public ScenarioModel() {
        super("Scenario");

        scenarios = new ArrayList<>();
    }


    public void clearAllScenarios() {
        scenarios.clear();
    }

    public void addScenario(Descriptor<Class<? extends Scenario>> scenario) {
        scenarios.add(scenario);
    }

    public ArrayList<Descriptor<Class<? extends Scenario>>> getScenarios() {
        return new ArrayList<>(scenarios);
    }

    public Descriptor<Class<? extends Scenario>> get(int index) {

        return scenarios.get(index);
    }
}