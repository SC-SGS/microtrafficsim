package logic.validation.scenarios;

import microtrafficsim.core.simulation.scenarios.Scenario;
import microtrafficsim.core.simulation.scenarios.containers.VehicleContainer;
import microtrafficsim.core.simulation.utils.ODMatrix;

/**
 * @author Dominic Parga Cacheiro
 */
public abstract class ValidationScenario implements Scenario {

    public final String OSM_FILENAME;

    public ValidationScenario(String OSM_FILENAME) {
        this.OSM_FILENAME = OSM_FILENAME;
    }

    /*
    |==============|
    | (i) Scenario |
    |==============|
    */
    @Override
    public VehicleContainer getVehicleContainer() {
        return null;
    }

    @Override
    public void setPrepared(boolean isPrepared) {

    }

    @Override
    public boolean isPrepared() {
        return false;
    }

    @Override
    public void setODMatrix(ODMatrix odMatrix) {

    }

    @Override
    public ODMatrix getODMatrix() {
        return null;
    }
}
