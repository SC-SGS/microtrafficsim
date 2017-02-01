package microtrafficsim.core.logic.streets.information;

import microtrafficsim.core.simulation.configs.ScenarioConfig;

/**
 * @author Dominic Parga Cacheiro
 */
public class FullStreetInfo {

    public final RawStreetInfo raw;

    public int numberOfCells;
    public int maxVelocity;


    public FullStreetInfo(RawStreetInfo rawStreetInfo) {

        this.raw = rawStreetInfo;

        // important for shortest path: round up
        numberOfCells = Math.max(1, (int) (Math.ceil(rawStreetInfo.lengthInMeters / rawStreetInfo.metersPerCell)));

        // maxVelocity in km/h, but this.maxVelocity in cells/s
        maxVelocity = Math.max(1, (int) Math.round(rawStreetInfo.maxVelocity / 3.6 / rawStreetInfo.metersPerCell));
    }
}
