package microtrafficsim.core.logic.streets.information;

import microtrafficsim.utils.Resettable;

/**
 * @author Dominic Parga Cacheiro
 */
public class FullStreetInfo implements Resettable {

    public final RawStreetInfo raw;

    public int numberOfCells;
    public int maxVelocity;
    public byte priorityLevel;


    public FullStreetInfo(RawStreetInfo rawStreetInfo) {
        this.raw = rawStreetInfo;
        reset();
    }

    @Override
    public void reset() {
        // important for shortest path: round up
        // important for multilaned crossing logic: at least 2
        numberOfCells = Math.max(2, (int) (Math.ceil(raw.lengthInMeters / raw.metersPerCell)));

        // maxVelocity in km/h, but this.maxVelocity in cells/s
        maxVelocity = Math.max(1, (int) Math.round(raw.maxVelocity / 3.6 / raw.metersPerCell));

        priorityLevel = raw.priorityFn.getPriority(raw.type);
    }
}
