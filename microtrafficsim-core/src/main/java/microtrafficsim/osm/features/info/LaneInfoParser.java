package microtrafficsim.osm.features.info;

import microtrafficsim.core.map.features.info.LaneInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


/**
 * Provides functionality for extracting a {@code LaneInfo} object out of
 * OpenStreetMap tags.
 *
 * @author Maximilian Luz
 */
public class LaneInfoParser {
    private static Logger logger = LoggerFactory.getLogger(LaneInfoParser.class);

    private LaneInfoParser() {}


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
