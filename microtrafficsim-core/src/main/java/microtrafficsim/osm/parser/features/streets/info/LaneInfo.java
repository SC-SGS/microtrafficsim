package microtrafficsim.osm.parser.features.streets.info;

import microtrafficsim.osm.parser.ecs.components.traits.Reversible;
import microtrafficsim.osm.parser.features.streets.ReverseEquals;
import microtrafficsim.utils.hashing.FNVHashBuilder;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import org.slf4j.Logger;

import java.util.Map;


/**
 * Information about the number of lanes on a street.
 *
 * @author Maximilian Luz
 */
public class LaneInfo implements ReverseEquals, Reversible {
    private static Logger logger = new EasyMarkableLogger(LaneInfo.class);

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

    @Override
    public void reverse() {
        int tmp  = forward;
        forward  = backward;
        backward = tmp;
    }


    /**
     * Create a {@code LaneInfo} object for a street out of the given
     * OpenStreetMap tags.
     *
     * @param tags the OpenStreetMap tags as Map.
     * @return the parsed {@code LaneInfo}
     */
    public static LaneInfo parse(Map<String, String> tags) {
        int lanes    = UNGIVEN;
        int forward  = UNGIVEN;
        int backward = UNGIVEN;

        // parse 'lanes' tag
        String laneTag = tags.get("lanes");
        if (laneTag != null) {
            String[] splits = laneTag.split("[;:|,]");

            int[] parsed = new int[splits.length];
            for (int i = 0; i < splits.length; i++)
                parsed[i] = parseSimpleTagValue(splits[i].trim());

            if (parsed.length >= 2 && parsed[0] != UNGIVEN && parsed[1] != UNGIVEN) {
                forward = parsed[0];
                backward = parsed[1];
                lanes = forward + backward;
            } else {
                lanes = parsed[0];
            }
        }

        if (forward == UNGIVEN)
            forward  = parseSimpleTagValue(tags.get("lanes:forward"));

        if (backward == UNGIVEN)
            backward = parseSimpleTagValue(tags.get("lanes:backward"));

        return new LaneInfo(lanes, forward, backward);
    }

    /**
     * Parses the given lane tag-value.
     *
     * @param value the lane-value-string to be parsed.
     * @return the number of lanes, or {@link LaneInfo#UNGIVEN} if the string is {@code null}.
     */
    private static int parseSimpleTagValue(String value) {
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
