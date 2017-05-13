package microtrafficsim.core.simulation.configs;

import microtrafficsim.core.map.StreetType;
import microtrafficsim.math.random.distributions.impl.Random;

import java.util.HashMap;


/**
 * <p>
 * This class contains the following simulation parameters/constants like the street priorities. <br>
 * &bull {@link #speedup} a simple factor defining, how many steps should be calculated per second (depending on the cpu etc.,
 * the real speedup could be less) <br>
 * &bull {@link #seed} this seed should be used for random variables and similar tasks
 * &bull {@link #scenario} This configuration object contains attributes about the scenario <br>
 * &bull {@link #crossingLogic} This configuration object contains attributes relevant for the crossing logic <br>
 * &bull {@link #visualization} This configuration object contains attributes relevant for the visualization <br>
 * &bull {@link #maxVehicleCount} The initial number of vehicles on the streetgraph <br>
 * &bull {@link #streetPriorityLevel} This is a function returning the street priority depending on the street type <br>
 * &bull {@link #multiThreading} This configuration object contains attributes relevant for multi-threading
 *
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro
 */
public final class SimulationConfig {

    /* general */
    public float metersPerCell;
    public int   globalMaxVelocity;
    public int   speedup;
    public long  seed;

    /* scenario */
    public final ScenarioConfig scenario;

    /* visualization */
    public final VisualizationConfig visualization;

    /* crossing logic */
    public final CrossingLogicConfig crossingLogic;

    /* vehicles */
    public int maxVehicleCount;

    /* street type priorities */
    public StreetPriorityFunction streetPriorityLevel;

    /* multithreading */
    public final MultiThreadingConfig multiThreading;

    /**
     * Just calls {@link #setup()}.
     */
    public SimulationConfig() {

        scenario        = new ScenarioConfig();
        crossingLogic   = new CrossingLogicConfig();
        visualization   = new VisualizationConfig();
        multiThreading  = new MultiThreadingConfig();
        setup();
    }

    /**
     * Calls the {@link #SimulationConfig() default constructor} and sets all values using
     * {@link #update(SimulationConfig)}.
     */
    public SimulationConfig(SimulationConfig init) {
        this();
        update(init);
    }

    /**
     * <p>
     * Resets the parameter of this config file. This method keeps references of<br>
     * &bull {@link VisualizationConfig}<br>
     * &bull {@link CrossingLogicConfig}<br>
     * &bull {@link MultiThreadingConfig}
     */
    private void setup() {
        // 1/3,6 = 25/90 = 0,277... => 0,277 m/cell means 1 cell/s <=> 1 km/h
        metersPerCell     = 7.5f; // Nagel-Schreckenberg-Model
        globalMaxVelocity = 6; // <=> ca. 180 km/h after Nagel-Schreckenberg-Model
        speedup           = 1;
        seed              = new Random().nextLong();
        // vehicles
        maxVehicleCount = 100;
        // street type priorities
        streetPriorityLevel = new DefaultStreetPriorityFunction();
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
        /* general */
        metersPerCell     = config.metersPerCell;
        globalMaxVelocity = config.globalMaxVelocity;
        speedup           = config.speedup;
        seed              = config.seed;
        /* scenario */
        scenario.update(config.scenario);
        /* crossing logic */
        crossingLogic.update(config.crossingLogic);
        /* visualization */
        visualization.update(config.visualization);
        /* vehicles */
        maxVehicleCount = config.maxVehicleCount;
        /* street type priorities */
        streetPriorityLevel = config.streetPriorityLevel;
        /* multithreading */
        multiThreading.update(config.multiThreading);
    }



    /**
     * @author Maximilian Luz, Dominic Parga Cacheiro
     */
    public interface StreetPriorityFunction {
        byte getPriority(StreetType type);
    }

    /**
     * @author Maximilian Luz, Dominic Parga Cacheiro
     */
    public static class DefaultStreetPriorityFunction implements StreetPriorityFunction {
        @Override
        public byte getPriority(StreetType type) {
            byte priority = 0;

            if (type.isRoundabout()) {
                return Byte.MAX_VALUE;
            }

            switch (type.getType()) {
                case StreetType.MOTORWAY:       priority++;
                case StreetType.TRUNK:          priority++;
                case StreetType.PRIMARY:        priority++;
                case StreetType.SECONDARY:      priority++;
                case StreetType.TERTIARY:       priority++;
                case StreetType.UNCLASSIFIED:   priority++;
                case StreetType.RESIDENTIAL:    priority++;
                case StreetType.LIVING_STREET:  priority++;
                case StreetType.SERVICE:        priority++;
                case StreetType.TRACK:          priority++;
                case StreetType.ROAD:           priority++;
            }

            priority *= 2;

            if (type.isLink())
                priority--;

            return priority;
        }
    }



    /**
     * @author Dominic Parga Cacheiro
     */
    public enum Element {
        // General
        sliderSpeedup(true),
        maxVehicleCount(true),
        seed(true),
        metersPerCell(false),
        globalMaxVelocity(false),
        /* scenario */
        showAreasWhileSimulating(true),
        nodesAreWeightedUniformly(true),
        scenarioSelection(true),
        // crossing logic
        edgePriority(true),
        priorityToThe(true),
        onlyOneVehicle(true),
        friendlyStandingInJam(true),
        // Visualization
        style(false),
        // concurrency
        nThreads(true),
        vehiclesPerRunnable(true),
        nodesPerThread(true);

        private final boolean editable;

        Element() {
            this(false);
        }

        Element(boolean editable) {
            this.editable = editable;
        }

        public boolean isEditable() {
            return editable;
        }
    }

    public static class EnableLexicon {
        private final HashMap<Element, Boolean> lexicon = new HashMap<>();

        public boolean isEnabled(Element element) {
            Boolean enabled = lexicon.get(element);
            if (enabled == null)
                return false;
            return enabled;
        }

        public boolean setEnabledIfEditable(Element element, boolean enabled) {
            enabled &= element.editable;
            lexicon.put(element, enabled);
            return enabled;
        }
    }
}