package microtrafficsim.ui.preferences.model;


/**
 * @author Dominic Parga Cacheiro
 */
public enum PrefElement {
    // General
    sliderSpeedup(true),
    maxVehicleCount(true),
    seed(true),
    metersPerCell(false),
    /* scenario */
    showAreasWhileSimulating(true),
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

    private boolean enabled;

    PrefElement() {
        this(false);
    }

    PrefElement(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Enum<PrefElement> setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }
}