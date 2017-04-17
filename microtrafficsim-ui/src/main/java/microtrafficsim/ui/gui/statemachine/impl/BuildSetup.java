package microtrafficsim.ui.gui.statemachine.impl;

import microtrafficsim.core.convenience.MapViewer;
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
import microtrafficsim.math.random.distributions.impl.Random;

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
    public MapViewer mapviewer;
    public VehicleOverlay overlay;

    /* simulation */
    public Simulation simulation;
    public ScenarioBuilder scenarioBuilder;

    /* gui */
    public String frameTitle;

    public BuildSetup() {

        /* general */
        config = new SimulationConfig();
        config.scenario.classes.add(RandomRouteScenario.class);
        config.scenario.classes.add(EndOfTheWorldScenario.class);
        config.scenario.selectedClass = RandomRouteScenario.class;

        /* visualization and parsing */
        mapviewer = new TileBasedMapViewer(config.visualization.style);
        overlay   = new SpriteBasedVehicleOverlay(mapviewer.getProjection(), config.visualization.style);

        /* simulation */
        simulation          = new VehicleSimulation();
        scenarioBuilder     = new VehicleScenarioBuilder(config.seed, overlay.getVehicleFactory());

        /* gui */
        this.frameTitle = "MicroTrafficSim";
    }
}
