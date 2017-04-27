package microtrafficsim.core.exfmt.base;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.simulation.configs.SimulationConfig;


/**
 * @author Dominic Parga Cacheiro
 */
public class SimulationConfigInfo extends Container.Entry {
    private SimulationConfig info = new SimulationConfig();


    /**
     * Calls {@link SimulationConfig#update(SimulationConfig)}
     */
    public void update(SimulationConfig config) {
        info.update(config);
    }
}
