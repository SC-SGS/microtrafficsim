package microtrafficsim.osm.parser.features.streets.info;

import microtrafficsim.osm.parser.features.streets.ReverseEquals;
import microtrafficsim.utils.hashing.FNVHashBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


/**
 * Information about the number of lanes on a street.
 *
 * @author Maximilian Luz
 */
public class LaneInfo implements ReverseEquals {
    private static Logger logger = LoggerFactory.getLogger(LaneInfo.class);

    public static final int UNGIVEN = -1;

    public int sum;
    public int forward;
    public int backward;

    public LaneInfo(int sum, int forward, int backward) {
        this.sum      = sum;
        this.forward  = forward;
        this.backward = backward;
    }

    public LaneInfo(LaneInfo other) {
        this.sum      = other.sum;
        this.forward  = other.forward;
        this.backward = other.backward;
    }


    @Override
    public int hashCode() {
        return new FNVHashBuilder()
                .add(sum)
                .add(forward)
                .add(backward)
                .getHash();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof LaneInfo)) return false;

        LaneInfo other = (LaneInfo) obj;
        return this.sum == other.sum
                && this.forward == other.forward
                && this.backward == other.backward;
    }

    @Override
    public boolean reverseEquals(Object obj) {
        if (!(obj instanceof LaneInfo)) return false;

        LaneInfo other = (LaneInfo) obj;
        return this.sum == other.sum
                && this.forward == other.backward
                && this.backward == other.forward;
    }


    /**
     * Create a {@code LaneInfo} object for a street out of the given
     * OpenStreetMap tags.
     *
     * @param tags the OpenStreetMap tags as Map.
     * @return the parsed {@code LaneInfo}
     */
    public static LaneInfo parse(Map<String, String> tags) {
        int lanes    = parseTagValue(tags.get("lanes"));
        int forward  = parseTagValue(tags.get("lanes:forward"));
        int backward = parseTagValue(tags.get("lanes:backward"));

        return new LaneInfo(lanes, forward, backward);
    }

    /**
     * Parses the given lane tag-value.
     *
     * @param value the lane-value-string to be parsed.
     * @return the number of lanes, or {@link LaneInfo#UNGIVEN} if the string is {@code null}.
     */
    private static int parseTagValue(String value) {
        int lanes = LaneInfo.UNGIVEN;

        if (value != null) {
            try {
                // XXX: interpreting half lanes as full ones
                lanes = (int) Math.ceil(Float.parseFloat(value));
            } catch (NumberFormatException e) {
                logger.warn("on 'lanes' tag: '" + value + "' is not a valid number!");
            }
        }

        return lanes;
    }
}
