package microtrafficsim.core.simulation.configs;

import microtrafficsim.core.map.features.info.StreetType;
import microtrafficsim.core.simulation.SimulationLogger;
import microtrafficsim.utils.id.ConcurrentLongIDGenerator;
import microtrafficsim.utils.id.LongIDGenerator;

import java.util.Random;
import java.util.function.Function;


/**
 * This class contains simulation parameters/constants like the street priorities.
 *
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro
 */
public final class SimulationConfig {
    // general
    public LongIDGenerator longIDGenerator;
    public float           metersPerCell;
    // todo private int cellNumberScale; (depending on meters per cell!)
    public int              speedup;
    public long             seed;
    public SimulationLogger logger;
    public int              ageForPause;

    // visualization
    public VisualizationConfig visualization;

    // crossing logic
    public CrossingLogicConfig crossingLogic;

    // vehicles
    public int     maxVehicleCount;
    public boolean printVehicles;

    // street type priorities
    public Function<StreetType, Byte> streetPriorityLevel;

    // multithreading
    public MultiThreadingConfig multiThreading;

    /**
     * Just calls {@link #reset()}.
     */
    public SimulationConfig() {
        visualization  = new VisualizationConfig();
        crossingLogic  = new CrossingLogicConfig();
        multiThreading = new MultiThreadingConfig();
        reset();
    }

    /**
     * Resets the parameter of this config file. This method keeps references of<br>
     * &bull {@link VisualizationConfig}<br>
     * &bull {@link CrossingLogicConfig}<br>
     * &bull {@link MultiThreadingConfig}<br>
     */
    public void reset() {
        // general
        longIDGenerator = new ConcurrentLongIDGenerator();
        // 1/3,6 = 25/90 = 0,277... => 0,277 m/cell means 1 cell/s <=> 1 km/h
        metersPerCell = 7.5f;
        speedup       = 1;
        seed          = new Random().nextLong();
        logger        = new SimulationLogger(false);
        ageForPause   = -1;
        // visualization
        visualization.reset();
        // crossing logic
        crossingLogic.reset();
        // vehicles
        maxVehicleCount = 100;
        printVehicles   = false;
        // street type priorities
        streetPriorityLevel = streetType -> {
            byte prioLevel = 0;
            switch (streetType) {
            case ROUNDABOUT: prioLevel++;
            case MOTORWAY: prioLevel++;
            case MOTORWAY_LINK: prioLevel++;
            case TRUNK: prioLevel++;
            case TRUNK_LINK: prioLevel++;
            case PRIMARY: prioLevel++;
            case PRIMARY_LINK: prioLevel++;
            case SECONDARY: prioLevel++;
            case SECONDARY_LINK: prioLevel++;
            case TERTIARY: prioLevel++;
            case TERTIARY_LINK: prioLevel++;
            case UNCLASSIFIED: prioLevel++;
            case RESIDENTIAL: prioLevel++;
            case LIVING_STREET: prioLevel++;
            case SERVICE: prioLevel++;
            case TRACK: prioLevel++;
            case ROAD: prioLevel++;
            }
            return prioLevel;
        };
        // multithreading
        multiThreading.reset();
    }

    /**
     * Updates the parameter of this config file. This method keeps references of<br>
     * &bull {@link VisualizationConfig}<br>
     * &bull {@link CrossingLogicConfig}<br>
     * &bull {@link MultiThreadingConfig}<br>
     *
     * @param config All values of the new config instance are set to this config-values.
     */
    public void update(SimulationConfig config) {
        // general
        longIDGenerator = config.longIDGenerator;
        metersPerCell   = config.metersPerCell;
        speedup         = config.speedup;
        seed            = config.seed;
        logger          = config.logger;
        ageForPause     = config.ageForPause;
        // visualization
        visualization.update(config.visualization);
        // crossing logic
        crossingLogic.update(config.crossingLogic);
        // vehicles
        maxVehicleCount = config.maxVehicleCount;
        printVehicles   = config.printVehicles;
        // street type priorities
        streetPriorityLevel = config.streetPriorityLevel;
        // multithreading
        multiThreading.update(config.multiThreading);
    }
}