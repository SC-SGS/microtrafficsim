package microtrafficsim.core.simulation.controller.configs;

import microtrafficsim.core.map.features.info.StreetType;
import microtrafficsim.core.simulation.controller.SimulationLogger;
import microtrafficsim.utils.id.ConcurrentLongIDGenerator;
import microtrafficsim.utils.id.LongIDGenerator;
import microtrafficsim.utils.valuewrapper.LazyFinalValue;
import microtrafficsim.utils.valuewrapper.observable.GenericObservable;
import microtrafficsim.utils.valuewrapper.observable.GenericObserver;
import microtrafficsim.utils.valuewrapper.observable.ObservableValue;

import java.util.HashMap;
import java.util.Random;

/**
 * This class contains simulation parameters/constants like the street priorities.
 * 
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro
 */
public final class SimulationConfig {

    /*
    |=========================|
    | configuration variables |
    |=========================|
    */
    // general
	private LazyFinalValue<LongIDGenerator> longIDGenerator;
    private LazyFinalValue<Float> metersPerCell;
    // todo private final LazyFinalValue<Integer> cellNumberScale;
    private ObservableValue<Integer, Void> speedup;
    private LazyFinalValue<Long> seed;
    private SimulationLogger logger;
    public int ageForPause;
    // visualization
    private VisualizationConfig visualization;
	// crossing logic
	private CrossingLogicConfig crossingLogic;
	// vehicles
	public int maxVehicleCount;
	public boolean printVehicles;
	// street type priorities
	public HashMap<StreetType, Byte> streetPrios;
	// multithreading
	private MultiThreadingConfig multiThreading;

    /*
    |================|
    | default values |
    |================|
    */
    /**
     * Resets the parameter of this config file. This method is
     * important, because some values are (lazy-)final, because not all values can be changed after a simulation
     * has started. Using this method, the simulation classes can keep the reference to their config file and you can
     * reset it without resetting the whole simulation.
     */
    public void reset() {
        // general
        longIDGenerator = new LazyFinalValue<>(new ConcurrentLongIDGenerator());
        // 1/3,6 = 25/90 = 0,277... => 0,277 m/cell means 1 cell/s <=> 1 km/h
        metersPerCell = new LazyFinalValue<>(25f/90f);
        speedup = new ObservableValue<>();
        speedup.set(1);
        seed = new LazyFinalValue<>(new Random().nextLong());
        logger = new SimulationLogger(false);
        ageForPause = -1;
        // visualization
        visualization = new VisualizationConfig();
        // crossing logic
        crossingLogic = new CrossingLogicConfig();
        // vehicles
        maxVehicleCount = 100;
        printVehicles = false;
        // street type priorities
        streetPrios = new HashMap<StreetType, Byte>();
        byte PRIO_COUNTER = 0;
        streetPrios.put(StreetType.ROAD, PRIO_COUNTER++);
        streetPrios.put(StreetType.TRACK, PRIO_COUNTER++);
        streetPrios.put(StreetType.SERVICE, PRIO_COUNTER++);
        streetPrios.put(StreetType.LIVING_STREET, PRIO_COUNTER++);
        streetPrios.put(StreetType.RESIDENTIAL, PRIO_COUNTER++);
        streetPrios.put(StreetType.UNCLASSIFIED, PRIO_COUNTER++);
        streetPrios.put(StreetType.TERTIARY_LINK, PRIO_COUNTER++);
        streetPrios.put(StreetType.TERTIARY, PRIO_COUNTER++);
        streetPrios.put(StreetType.SECONDARY_LINK, PRIO_COUNTER++);
        streetPrios.put(StreetType.SECONDARY, PRIO_COUNTER++);
        streetPrios.put(StreetType.PRIMARY_LINK, PRIO_COUNTER++);
        streetPrios.put(StreetType.PRIMARY, PRIO_COUNTER++);
        streetPrios.put(StreetType.TRUNK_LINK, PRIO_COUNTER++);
        streetPrios.put(StreetType.TRUNK, PRIO_COUNTER++);
        streetPrios.put(StreetType.MOTORWAY_LINK, PRIO_COUNTER++);
        streetPrios.put(StreetType.MOTORWAY, PRIO_COUNTER++);
        // multithreading
        multiThreading = new MultiThreadingConfig();
    }

    /**
     * Resets the parameter of this config file. This method is
     * important, because some values are (lazy-)final, because not all values can be changed after a simulation
     * has started. Using this method, the simulation classes can keep the reference to their config file and you can
     * reset it without resetting the whole simulation.
     *
     * @param config All values of the new config instance are initialized with this config-values as default.
     */
    public void reset(SimulationConfig config) {
        // general
        longIDGenerator = new LazyFinalValue<>(new ConcurrentLongIDGenerator());
        // 1/3,6 = 25/90 = 0,277... => 0,277 m/cell means 1 cell/s <=> 1 km/h
        metersPerCell = new LazyFinalValue<>(25f/90f);
        speedup.set(config.speedup.get());
        seed = new LazyFinalValue<>(new Random().nextLong());
        logger = new SimulationLogger(false);
        ageForPause = config.ageForPause;
        // visualization
        visualization.reset(config.visualization);
        // crossing logic
        crossingLogic.reset(config.crossingLogic);
        // vehicles
        maxVehicleCount = config.maxVehicleCount;
        printVehicles = config.printVehicles;
        // street type priorities
        streetPrios = config.streetPrios;
        // multithreading
        multiThreading.reset(config.multiThreading);
    }

    /**
     * Sets all default values.
     */
	public SimulationConfig() {
		reset();
	}

    /**
     * Initializes a new config instance with all values set to the values of the given config.
     *
     * @param config All values of the new config instance are initialized with this config-values as default.
     */
    public SimulationConfig(SimulationConfig config) {
        reset(config);
    }

    /*
    |========|
    | getter |
    |========|
    */
    // general
    public LazyFinalValue<LongIDGenerator> longIDGenerator() { return longIDGenerator; }
    public LazyFinalValue<Float> metersPerCell() { return metersPerCell; }
    public ObservableValue<Integer, Void> speedup() { return speedup; }
    public LazyFinalValue<Long> seed() { return seed; }
    public SimulationLogger logger() { return logger; }
    // visualization
    public VisualizationConfig visualization() { return visualization; }
    // crossing logic
    public CrossingLogicConfig crossingLogic() { return crossingLogic; }
    // multithreading
    public MultiThreadingConfig multiThreading() { return multiThreading; }
}