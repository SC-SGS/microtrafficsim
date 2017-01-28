package microtrafficsim.core.simulation.configs;

import microtrafficsim.math.random.distributions.impl.Random;
import microtrafficsim.osm.parser.features.streets.info.StreetType;
import microtrafficsim.utils.Resettable;
import microtrafficsim.utils.id.ConcurrentLongIDGenerator;
import microtrafficsim.utils.id.ConcurrentSeedGenerator;
import microtrafficsim.utils.id.LongGenerator;

import java.util.function.Function;


/**
 * <p>
 * This class contains the following simulation parameters/constants like the street priorities. <br>
 * &bull {@link #longIDGenerator} <br>
 * &bull {@link #seedGenerator} depends on seed; if you want to set the seed, you also should probably set this
 * attribute <br>
 * &bull {@link #speedup} a simple factor defining, how many steps should be calculated per second (depending on the cpu etc.,
 * the real speedup could be less) <br>
 * &bull {@link #seed} this seed should be used for random variables and similar tasks; {@link #seedGenerator} also depends on
 * it per default <br>
 * &bull {@link #visualization} This configuration object contains attributes relevant for the visualization <br>
 * &bull {@link #crossingLogic} This configuration object contains attributes relevant for the crossing logic <br>
 * &bull {@link #maxVehicleCount} The initial number of vehicles on the streetgraph <br>
 * &bull {@link #streetPriorityLevel} This is a function returning the street priority depending on the street type <br>
 * &bull {@link #multiThreading} This configuration object contains attributes relevant for multi-threading
 *
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro
 */
public final class ScenarioConfig implements Resettable {
    // general
    public LongGenerator longIDGenerator;
    public LongGenerator seedGenerator;
    public float         metersPerCell;
    public int           globalMaxVelocity;
    // todo private int cellNumberScale; (depending on meters per cell!)
    public int            speedup;
    public long           seed;// TODO

    // visualization
    public VisualizationConfig visualization;

    // crossing logic
    public CrossingLogicConfig crossingLogic;

    // vehicles
    public int maxVehicleCount;

    // street type priorities
    public Function<StreetType, Byte> streetPriorityLevel;

    // multithreading
    public MultiThreadingConfig multiThreading;

    /**
     * Just calls {@link #reset()}.
     */
    public ScenarioConfig() {
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
    @Override
    public void reset() {
        // 1/3,6 = 25/90 = 0,277... => 0,277 m/cell means 1 cell/s <=> 1 km/h
        metersPerCell     = 7.5f; // Nagel-Schreckenberg-Model
        globalMaxVelocity = 6;
        speedup           = 1;
        seed              = new Random().nextLong();
        // visualization
        visualization.reset();
        // crossing logic
        crossingLogic.reset();
        // vehicles
        maxVehicleCount = 100;
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
        // general
        longIDGenerator = new ConcurrentLongIDGenerator();
        seedGenerator = new ConcurrentSeedGenerator(seed);
    }

    /**
     * Updates the parameter of this config file. This method keeps references of<br>
     * &bull {@link VisualizationConfig}<br>
     * &bull {@link CrossingLogicConfig}<br>
     * &bull {@link MultiThreadingConfig}<br>
     *
     * @param config All values of the new config instance are set to this config-values.
     */
    public void update(ScenarioConfig config) {
        // general
        longIDGenerator   = config.longIDGenerator;
        metersPerCell     = config.metersPerCell;
        globalMaxVelocity = config.globalMaxVelocity;
        speedup           = config.speedup;
        seed              = config.seed;
        // visualization
        visualization.update(config.visualization);
        // crossing logic
        crossingLogic.update(config.crossingLogic);
        // vehicles
        maxVehicleCount = config.maxVehicleCount;
        // street type priorities
        streetPriorityLevel = config.streetPriorityLevel;
        // multithreading
        multiThreading.update(config.multiThreading);
    }
}