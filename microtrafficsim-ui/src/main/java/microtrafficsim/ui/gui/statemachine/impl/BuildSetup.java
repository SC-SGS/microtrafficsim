package microtrafficsim.ui.gui.statemachine.impl;

import microtrafficsim.core.map.style.impl.MonochromeStyleSheet;
import microtrafficsim.core.mapviewer.MapViewer;
import microtrafficsim.core.mapviewer.impl.TileBasedMapViewer;
import microtrafficsim.core.simulation.builder.ScenarioBuilder;
import microtrafficsim.core.simulation.builder.impl.VehicleScenarioBuilder;
import microtrafficsim.core.simulation.configs.ScenarioConfig;
import microtrafficsim.core.simulation.core.Simulation;
import microtrafficsim.core.simulation.core.impl.VehicleSimulation;
import microtrafficsim.core.vis.simulation.SpriteBasedVehicleOverlay;
import microtrafficsim.core.vis.simulation.VehicleOverlay;

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
    public ScenarioConfig config;

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
        config = new ScenarioConfig();

        /* visualization and parsing */
        mapviewer = new TileBasedMapViewer(config.visualization.style);
        overlay   = new SpriteBasedVehicleOverlay(mapviewer.getProjection(), config.visualization.style);

        /* simulation */
        simulation          = new VehicleSimulation();
        scenarioBuilder     = new VehicleScenarioBuilder(config.seed, overlay.getVehicleFactory());

        /* gui */
        this.frameTitle = "MicroTrafficSim - GUI Example";
    }
}
