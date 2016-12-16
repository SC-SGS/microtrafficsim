package microtrafficsim.osm.parser.features.streets.info;


import microtrafficsim.osm.parser.features.streets.ReverseEquals;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import org.slf4j.Logger;

import java.util.Map;


/**
 * Information for one-way-streets.
 *
 * @author Maximilian Luz
 */
public enum OnewayInfo implements ReverseEquals {
    NO,
    FORWARD,
    BACKWARD,
    REVERSIBLE;

    private static Logger logger = new EasyMarkableLogger(OnewayInfo.class);

    @Override
    public boolean reverseEquals(Object obj) {
        if (!(obj instanceof OnewayInfo)) return false;

        OnewayInfo other = (OnewayInfo) obj;
        return (this == OnewayInfo.FORWARD && other == OnewayInfo.BACKWARD)
                || (this == OnewayInfo.BACKWARD && other == OnewayInfo.FORWARD)
                || (this == OnewayInfo.NO && other == OnewayInfo.NO)
                || (this == OnewayInfo.REVERSIBLE && other == OnewayInfo.REVERSIBLE);
    }


    /**
     * Create a {@code OnewayInfo} object for a street out of the given
     * OpenStreetMap tags.
     *
     * @param tags the OpenStreetMap tags as Map.
     * @return the parsed {@code OnewayInfo}
     */
    public static OnewayInfo parse(Map<String, String> tags) {
        String oneway = tags.get("oneway");

        // try parsing oneway-tag
        if (oneway != null) {
            switch (oneway) {
                case "no":
                case "false":
                case "0":
                    return OnewayInfo.NO;

                case "yes":
                case "true":
                case "1":
                    return OnewayInfo.FORWARD;

                case "-1":
                case "reverse":
                    return OnewayInfo.BACKWARD;

                case "reversible":
                    return OnewayInfo.REVERSIBLE;

                default:
                    logger.warn("unknown 'oneway' value '" + oneway + "' found, using default value");
            }
        }

        // 'highway=motorway' implies 'oneway=yes'
        if ("motorway".equals(tags.get("highway")))
            return OnewayInfo.FORWARD;

        // 'junction=roundabout' implies 'oneway=yes'
        if ("roundabout".equals(tags.get("junction")))
            return OnewayInfo.FORWARD;

        // the default is 'oneway=no'
        return OnewayInfo.NO;
    }
}
