package microtrafficsim.core.simulation.scenarios;

import microtrafficsim.core.entities.vehicle.VisualizationVehicleEntity;
import microtrafficsim.core.logic.vehicles.AbstractVehicle;
import microtrafficsim.core.logic.vehicles.VehicleState;
import microtrafficsim.core.simulation.Simulation;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.interesting.emotions.Hulk;
import microtrafficsim.interesting.progressable.ProgressListener;
import microtrafficsim.math.HaversineDistanceCalculator;
import microtrafficsim.utils.datacollection.ConcurrentHashDataCollector;
import microtrafficsim.utils.datacollection.Data;
import microtrafficsim.utils.datacollection.DataCollector;
import microtrafficsim.utils.datacollection.Tag;
import microtrafficsim.utils.io.FileManager;

import java.util.*;


/**
 * @author Dominic Parga Cacheiro
 */
public class MeasuringScenario implements Simulation {

    private SimulationConfig config;
    private Simulation       simulation;
    private Timer            timer;
    private boolean          paused;
    private DataCollector    dataCollector;
    private FileManager      fileManager;
    private HashMap<MeasuredType, Boolean> enabledMeasurements;
    /**
     * Default constructor.
     *
     * @param simulation This simulation should be measured.
     */
    public MeasuringScenario(SimulationConfig config, Simulation simulation) {
        this(config, simulation, "data");
    }

    /**
     * Default constructor.
     *
     * @param simulation         This simulation should be measured.
     * @param PATH_TO_DATA_FILES Path to the folder where the measured data should be saved
     */
    public MeasuringScenario(SimulationConfig config, Simulation simulation, final String PATH_TO_DATA_FILES) {

        this.config     = config;
        this.simulation = simulation;
        paused          = true;
        timer           = new Timer();

        dataCollector = new ConcurrentHashDataCollector();
        fileManager   = new FileManager(PATH_TO_DATA_FILES);

        enabledMeasurements = new HashMap<>();
        enableAllMeasurements();
        for (MeasuredType key : MeasuredType.values())
            dataCollector.addBundle(key.filename);

        // TODO config.seed.addObserver(...)
    }

    /**
     * TODO
     */
    public static void printUsage() {
        System.out.println("MeasuringScenario - parameters");
        System.out.println("");
        System.out.println("Parameters:");
        System.out.println(
                "  -o <folder>               The path to the folder where measured data is saved. Default: data");
        System.out.println("");
    }

    @Override
    public SimulationConfig getConfig() {
        return config;
    }

    /*
    |======================|
    | writing data to file |
    |======================|
    */
    public synchronized void writeDataToFile() {
        System.out.println("");
        System.out.println("WRITING FILES STARTED");

        // writing
        Iterator<Object> data     = dataCollector.iterator();
        String           filename = "";
        Object           obj      = null;
        if (data.hasNext()) obj   = data.next();    // has to be instance of Tag
        while (data.hasNext()) {
            // set filename
            while (obj instanceof Tag) {
                filename = obj.toString();
                fileManager.writeDataToFile(filename, "", true);

                if (data.hasNext())
                    obj = data.next();
                else
                    break;
            }

            // write data
            System.out.println("Writing " + filename + " started.");
            StringBuilder builder = new StringBuilder();
            boolean       first   = true;
            while (obj instanceof Data) {
                if (!first) builder.append(";");

                builder.append(obj.toString());
                if (data.hasNext())
                    obj = data.next();
                else
                    break;
                first = false;
            }
            fileManager.writeDataToFile(filename, builder.toString(), false);
            System.out.println("Writing " + filename + " finished.");
        }

        System.out.println("WRITING FILES FINISHED");
        System.out.println("");
    }

    /*
    |=====================================|
    | measuring inside simulation methods |
    |=====================================|
    */
    @Override
    public final void willRunOneStep() {}

    @Override
    public final void didRunOneStep() {

        // init a few variables to save execution time using the hasmap and for better overview
        ArrayList<? extends AbstractVehicle> spawnedVehicles = null;
        boolean angerPerTimestep                             = enabledMeasurements.get(MeasuredType.ANGER_PER_TIMESTEP);
        boolean spawnedCountPerTimestep = enabledMeasurements.get(MeasuredType.SPAWNED_COUNT_PER_TIMESTEP);
        if (angerPerTimestep || spawnedCountPerTimestep) spawnedVehicles = getSpawnedVehicles();

        if (angerPerTimestep) {
            dataCollector.put(MeasuredType.ANGER_PER_TIMESTEP.filename, spawnedVehicles.size());
            dataCollector.put(MeasuredType.ANGER_PER_TIMESTEP.filename, getAge());
            spawnedVehicles.forEach(
                    (Hulk hulk) -> dataCollector.put(MeasuredType.ANGER_PER_TIMESTEP.filename, hulk.getAnger()));
        }

        if (spawnedCountPerTimestep) {
            dataCollector.put(MeasuredType.SPAWNED_COUNT_PER_TIMESTEP.filename, spawnedVehicles.size());
        }
    }

    @Override
    public final void stateChanged(AbstractVehicle vehicle) {
        simulation.stateChanged(vehicle);
        if (vehicle.getState() == VehicleState.DESPAWNED) {

            if (enabledMeasurements.get(MeasuredType.TOTAL_ANGER_WHEN_DESPAWNING))
                dataCollector.put(MeasuredType.TOTAL_ANGER_WHEN_DESPAWNING.filename, vehicle.getTotalAnger());

            if (enabledMeasurements.get(MeasuredType.AGE_WHEN_DESPAWNING))
                dataCollector.put(MeasuredType.AGE_WHEN_DESPAWNING.filename, vehicle.getAge());

            if (enabledMeasurements.get(MeasuredType.LINEAR_DISTANCE_PER_DESPAWN_AGE)) {
                double xD = HaversineDistanceCalculator.getDistance(vehicle.getRoute().getStart().getCoordinate(),
                                                                    vehicle.getRoute().getEnd().getCoordinate())
                            / vehicle.getAge();
                dataCollector.put(MeasuredType.LINEAR_DISTANCE_PER_DESPAWN_AGE.filename, xD);
            }
        }
    }

    /*
    |============================|
    | enable/disable measurement |
    |============================|
    */
    public final void enableAllMeasurements() {
        for (MeasuredType key : MeasuredType.values())
            enabledMeasurements.put(key, true);
    }

    public final void disableAllMeasurements() {
        for (MeasuredType key : MeasuredType.values())
            enabledMeasurements.put(key, false);
    }

    public final void enable(MeasuredType type) {
        enabledMeasurements.put(type, true);
    }

    public final void disable(MeasuredType type) {
        enabledMeasurements.put(type, false);
    }

    public final boolean isEnabled(MeasuredType type) {
        return enabledMeasurements.get(type);
    }

    /*
    |==================|
    | copied literally |
    |==================|
    */
    @Override
    public final void run() {

        if (isPrepared() && paused) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    doRunOneStep();
                }
            }, 0, 1000 / config.speedup);
            paused = false;
        }
    }

    @Override
    public final void runOneStep() {
        if (isPrepared() && paused) doRunOneStep();
    }

    @Override
    public void doRunOneStep() {
        willRunOneStep();
        simulation.doRunOneStep();
        didRunOneStep();
    }

    @Override
    public final void cancel() {
        timer.cancel();
        paused = true;
    }

    @Override
    public final boolean isPaused() {
        return paused;
    }

    /*
    |=================================|
    | just copy of simulation methods |
    |=================================|
    */
    @Override
    public final boolean isPrepared() {
        return simulation.isPrepared();
    }

    @Override
    public final void prepare() {
        simulation.prepare();

        getVehicles().forEach(v -> v.setStateListener(this));
    }

    @Override
    public final void prepare(ProgressListener listener) {
        simulation.prepare(listener);

        getVehicles().forEach(v -> v.setStateListener(this));
    }

    @Override
    public final int getAge() {
        return simulation.getAge();
    }

    @Override
    public final ArrayList<? extends AbstractVehicle> getSpawnedVehicles() {
        return simulation.getSpawnedVehicles();
    }

    @Override
    public final ArrayList<? extends AbstractVehicle> getVehicles() {
        return simulation.getVehicles();
    }

    @Override
    public final int getSpawnedVehiclesCount() {
        return simulation.getSpawnedVehiclesCount();
    }

    @Override
    public final int getVehiclesCount() {
        return simulation.getVehiclesCount();
    }

    @Override
    public final VisualizationVehicleEntity createVisVehicle() {
        return simulation.createVisVehicle();
    }

    @Override
    public final boolean addVehicle(AbstractVehicle vehicle) {
        return simulation.addVehicle(vehicle);
    }

    public enum MeasuredType {

        ANGER_PER_TIMESTEP("anger_per_timestep.csv"),
        TOTAL_ANGER_WHEN_DESPAWNING("total_anger_when_despawning.csv"),
        SPAWNED_COUNT_PER_TIMESTEP("spawned_count_per_timestep.csv"),
        AGE_WHEN_DESPAWNING("age_when_despawning.csv"),
        LINEAR_DISTANCE_PER_DESPAWN_AGE("linear_distance_per_despawn_age.csv");

        private final String filename;

        private MeasuredType(String filename) {
            this.filename = filename;
        }
    }
}
