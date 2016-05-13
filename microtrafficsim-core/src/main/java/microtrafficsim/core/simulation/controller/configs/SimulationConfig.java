package microtrafficsim.core.simulation.controller.configs;

import microtrafficsim.core.map.features.info.StreetType;
import microtrafficsim.core.simulation.controller.SimulationLogger;
import microtrafficsim.utils.observable.GenericObserver;
import microtrafficsim.utils.observable.ObservableValue;
import microtrafficsim.utils.id.BasicLongIDGenerator;
import microtrafficsim.utils.id.LongIDGenerator;

import java.util.HashMap;
import java.util.Observer;
import java.util.Random;

/**
 * This class contains simulation parameters/constants like the street priorities.
 * 
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro
 */
public final class SimulationConfig {

    // NOTE: You have to add a new variable at 4 positions:
    // - configuration variables
    // - configuration variables' ids (in enum Id)
    // - default values
    // - observable array

    private static int observableIdx = 0;
    public enum Id {

        /*
        |==============================|
        | configuration variables' ids |
        |==============================|
        */
        msPerTimeStep(observableIdx++);

        private final int idx;

        Id(int idx) {
            this.idx = idx;
        }
    }

    /*
    |=========================|
    | configuration variables |
    |=========================|
    */
    // general
	public LongIDGenerator longIDGenerator;
	public final float metersPerCell;
    public final ObservableValue<Long, Id> msPerTimeStep;
    public long seed;
    public SimulationLogger logger;
    public int ageForPause;
    // visualization
    public VisualizationConfig visualization;
	// crossing logic
	public CrossingLogicConfig crossingLogic;
	// vehicles
	public int maxVehicleCount;
	public boolean printVehicles;
	// street type priorities
	public HashMap<StreetType, Byte> streetPrios;
	// multithreading
	public MultiThreadingConfig multiThreading;

    /*
    |================|
    | default values |
    |================|
    */
	{
		// general
		longIDGenerator = new BasicLongIDGenerator();
		metersPerCell = 7.5f;
        msPerTimeStep = new ObservableValue<>(Id.msPerTimeStep);
		msPerTimeStep.set(200L);
        seed = new Random().nextLong();
        logger = new SimulationLogger(false);
        ageForPause = -1;
        // visualization
        visualization = new VisualizationConfig();
		// crossing logic
		crossingLogic = new CrossingLogicConfig();
		// vehicles
		maxVehicleCount = 1000;
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

    /*
    |==================|
    | observable array |
    |==================|
    */
    private final ObservableValue[] observables = new ObservableValue[] {
            msPerTimeStep
    };

    public void addObserver(GenericObserver o) {
        for (Id id : Id.values())
            observables[id.idx].addObserver(o);
    }

    public void deleteObserver(GenericObserver o) {
        for (Id id : Id.values())
            observables[id.idx].deleteObserver(o);
    }
}