package microtrafficsim.ui.preferences;

/**
 * @author Dominic Parga Cacheiro
 */
public enum PrefElement {
  // General
  sliderSpeedup(true),
  ageForPause(true),
  maxVehicleCount(true),
  seed(true),
  metersPerCell(false),
  // Visualization
  projection(false),
  // crossing logic
  edgePriority(true),
  priorityToThe(true),
  onlyOneVehicle(true),
  friendlyStandingInJam(true),
  // concurrency
  nThreads(true),
  vehiclesPerRunnable(true),
  nodesPerThread(true);

  PrefElement() {
    this(false);
  }

  PrefElement(boolean enabled) {
    this.enabled = enabled;
  }

  private boolean enabled;

  public boolean isEnabled() {
    return enabled;
  }

  public Enum<PrefElement> setEnabled(boolean enabled) {
    this.enabled = enabled;
    return this;
  }
}