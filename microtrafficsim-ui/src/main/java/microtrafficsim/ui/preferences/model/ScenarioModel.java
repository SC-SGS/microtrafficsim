package microtrafficsim.ui.preferences.model;

import microtrafficsim.core.simulation.scenarios.Scenario;
import microtrafficsim.core.simulation.scenarios.impl.RandomRouteScenario;

import java.util.ArrayList;

/**
 * @author Dominic Parga Cacheiro
 */
public class ScenarioModel extends PreferencesModel {

    private final ArrayList<Class<? extends Scenario>> scenarios;

    public ScenarioModel() {
        super("Scenario");

        scenarios = new ArrayList<>();
        scenarios.add(RandomRouteScenario.class);
    }


    public ArrayList<Class<? extends Scenario>> getScenarios() {
        return new ArrayList<>(scenarios);
    }

    public Class<? extends Scenario> get(int index) {
        return scenarios.get(index);
    }
}