package microtrafficsim.core.exfmt.base;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.simulation.configs.*;


/**
 * @author Dominic Parga Cacheiro
 */
public class SimulationConfigInfo extends Container.Entry {

    /* general */
    private float metersPerCell;
    private int   globalMaxVelocity;
    private long  seed;

    /* crossing logic */
    private CrossingLogicConfig crossingLogic;

    /* vehicles */
    private int maxVehicleCount;

    /* street type priorities */
    private SimulationConfig.StreetPriorityFunction streetPriorityLevel;


    /**
     * <p>
     * Updates the given config to the values stored in this class.
     *
     * <p>
     * {@code ATTENTION!} Usually, the caller of this method (= this class) is updated by the given config file.
     * Here, it is the other way, the given config is updated by the caller.
     *
     * @param config This config file is getting updated by the values stored here.
     */
    public void update(SimulationConfig config) {
        /* general */
        config.metersPerCell     = metersPerCell;
        config.globalMaxVelocity = globalMaxVelocity;
        config.seed              = seed;

        /* crossing logic */
        config.crossingLogic.update(crossingLogic);

        /* vehicles */
        config.maxVehicleCount = maxVehicleCount;

        /* street type priorities */
        config.streetPriorityLevel = streetPriorityLevel;
    }

    /**
     * Sets all relevant config information in this info class to the value given by the config-parameter.
     *
     * @param config
     */
    public void set(SimulationConfig config) {
        /* general */
        this.metersPerCell     = config.metersPerCell;
        this.globalMaxVelocity = config.globalMaxVelocity;
        this.seed              = config.seed;

        /* crossing logic */
        this.crossingLogic = new CrossingLogicConfig(config.crossingLogic);

        /* vehicles */
        this.maxVehicleCount = config.maxVehicleCount;

        /* street type priorities */
        this.streetPriorityLevel = config.streetPriorityLevel;
    }
}
