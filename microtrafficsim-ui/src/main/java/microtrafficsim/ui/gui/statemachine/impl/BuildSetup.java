package microtrafficsim.ui.gui.statemachine.impl;

import microtrafficsim.core.convenience.TileBasedMapViewer;
import microtrafficsim.core.simulation.builder.ScenarioBuilder;
import microtrafficsim.core.simulation.builder.impl.VehicleScenarioBuilder;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.core.Simulation;
import microtrafficsim.core.simulation.core.impl.VehicleSimulation;
import microtrafficsim.core.simulation.scenarios.impl.AreaScenario;
import microtrafficsim.core.simulation.scenarios.impl.EndOfTheWorldScenario;
import microtrafficsim.core.simulation.scenarios.impl.RandomRouteScenario;
import microtrafficsim.core.vis.simulation.SpriteBasedVehicleOverlay;
import microtrafficsim.core.vis.simulation.VehicleOverlay;
import microtrafficsim.utils.Descriptor;

/**
 * <p>
 * Serves attributes for easy initializing of {@link SimulationController}.
 *
 * <p>
 * This class is made for developers, therefore all attributes are public. You should only set them once and deliver
 * this instance to the {@code simulation controller}. Different usage could crash the simulation.
 *
 * @author Dominic Parga Cacheiro
 */
public class BuildSetup {

    /* general */
    public SimulationConfig config;

    /* visualization and parsing */
    public TileBasedMapViewer mapviewer;
    public VehicleOverlay overlay;

    /* simulation */
    public Simulation simulation;
    public ScenarioBuilder scenarioBuilder;

    public BuildSetup() {

        /* general */
        config = new SimulationConfig();

        config.scenario.supportedClasses.add(new Descriptor<>(
                AreaScenario.class,
                "own defined areas"));
        config.scenario.supportedClasses.add(new Descriptor<>(
                EndOfTheWorldScenario.class,
                "everywhere -> border"));
        config.scenario.supportedClasses.add(new Descriptor<>(
                RandomRouteScenario.class,
                "everywhere -> everywhere"));
        config.scenario.selectedClass = config.scenario.supportedClasses.get(0);

        /* visualization and parsing */
        mapviewer = new TileBasedMapViewer(config.visualization.style);
        overlay   = new SpriteBasedVehicleOverlay(mapviewer.getProjection(), config.visualization.style);

        /* simulation */
        simulation          = new VehicleSimulation();
        scenarioBuilder     = new VehicleScenarioBuilder(config.seed, overlay.getVehicleFactory());
    }
}
