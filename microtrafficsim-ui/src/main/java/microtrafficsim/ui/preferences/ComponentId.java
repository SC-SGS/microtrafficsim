package microtrafficsim.ui.preferences;

import microtrafficsim.core.simulation.configs.SimulationConfig;

/**
 * @author Dominic Parga Cacheiro
 */
public enum ComponentId {
  // General
  sliderSpeedup,
  ageForPause,
  maxVehicleCount,
  seed,
  metersPerCell,
  // Visualization
  projection,
  // concurrency
  nThreads,
  vehiclesPerRunnable,
  nodesPerThread,
  // crossing logic
  edgePriorityEnabled,
  priorityToThe,
  onlyOneVehicleEnabled,
  friendlyStandingInJam
}