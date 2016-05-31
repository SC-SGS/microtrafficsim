package microtrafficsim.ui.preferences;

import microtrafficsim.osm.parser.ecs.Component;

/**
 * @author Dominic Parga Cacheiro
 */
public enum ComponentId {

    // General
    sliderSpeedup(true),
    ageForPause(true),
    maxVehicleCount(false),
    seed(false),
    metersPerCell(false),
    // Visualization
    projection(false, true),
    // concurrency
    nThreads(false),
    vehiclesPerRunnable(false),
    nodesPerThread(false),
    // crossing logic
    drivingOnTheRight(false),
    edgePriorityEnabled(false),
    priorityToTheRightEnabled(false),
    onlyOneVehicleEnabled(false),
    goWithoutPriorityEnabled(false);

    private boolean isEnabledDuringSimulating, isAlwaysDisabled;

    ComponentId(boolean isEnabledDuringSimulating) {
        this(isEnabledDuringSimulating, false);
    }

    ComponentId(boolean isEnabledDuringSimulating, boolean isAlwaysDisabled) {
        this.isEnabledDuringSimulating = isEnabledDuringSimulating;
        this.isAlwaysDisabled = isAlwaysDisabled;
    }

    public boolean isAlwaysDisabled() {
        return isAlwaysDisabled;
    }

    public synchronized boolean isEnabledDuringSimulating() {
        return isEnabledDuringSimulating;
    }

    public synchronized void setEnabledDuringSimulating(boolean isEnabledDuringSimulating) {
        this.isEnabledDuringSimulating = isEnabledDuringSimulating;
    }
}