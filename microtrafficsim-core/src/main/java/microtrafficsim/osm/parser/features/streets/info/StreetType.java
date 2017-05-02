package microtrafficsim.osm.parser.features.streets.info;


import java.util.Map;

/**
 * StreetType corresponding to the OpenStreetMap 'highway' tag.
 * See http://wiki.openstreetmap.org/wiki/Key:highway for extensive detail on
 * the meaning of these names.
 *
 * @author Maximilian Luz
 */
public enum StreetType {

    // main types
    MOTORWAY,
    TRUNK,
    PRIMARY,
    SECONDARY,
    TERTIARY,
    UNCLASSIFIED,
    RESIDENTIAL,
    SERVICE,
    ROUNDABOUT,

    // link types
    MOTORWAY_LINK,
    TRUNK_LINK,
    PRIMARY_LINK,
    SECONDARY_LINK,
    TERTIARY_LINK,

    // special types
    LIVING_STREET,
    TRACK,
    ROAD;

    /**
     * Create a {@code LaneInfo} object for a street out of the given
     * OpenStreetMap tags.
     *
     * @param tags the OpenStreetMap tags as Map.
     * @return the parsed {@code LaneInfo}
     */
    public static StreetType parse(Map<String, String> tags) {
        String highway = tags.get("highway");
        if (highway == null) return null;

        switch (highway) {
            case "motorway":      return StreetType.MOTORWAY;
            case "trunk":         return StreetType.TRUNK;
            case "primary":       return StreetType.PRIMARY;
            case "secondary":     return StreetType.SECONDARY;
            case "tertiary":      return StreetType.TERTIARY;
            case "unclassified":  return StreetType.UNCLASSIFIED;
            case "residential":   return StreetType.RESIDENTIAL;
            case "service":       return StreetType.SERVICE;

            case "motorway_link": return StreetType.MOTORWAY_LINK;
            case "trunk_link":    return StreetType.TRUNK_LINK;
            case "primary_link":  return StreetType.PRIMARY_LINK;
            case "tertiary_link": return StreetType.TERTIARY_LINK;

            case "living_street": return StreetType.LIVING_STREET;
            case "track":         return StreetType.TRACK;
            case "road":          return StreetType.ROAD;

            default: return null;
        }
    }


    public microtrafficsim.core.map.StreetType toCoreStreetType() {
        return toCoreStreetType(this);
    }

    public static microtrafficsim.core.map.StreetType toCoreStreetType(StreetType type) {
        switch (type) {
            case MOTORWAY:       return new microtrafficsim.core.map.StreetType(microtrafficsim.core.map.StreetType.MOTORWAY,      false, false);
            case TRUNK:          return new microtrafficsim.core.map.StreetType(microtrafficsim.core.map.StreetType.TRUNK,         false, false);
            case PRIMARY:        return new microtrafficsim.core.map.StreetType(microtrafficsim.core.map.StreetType.PRIMARY,       false, false);
            case SECONDARY:      return new microtrafficsim.core.map.StreetType(microtrafficsim.core.map.StreetType.SECONDARY,     false, false);
            case TERTIARY:       return new microtrafficsim.core.map.StreetType(microtrafficsim.core.map.StreetType.TERTIARY,      false, false);
            case UNCLASSIFIED:   return new microtrafficsim.core.map.StreetType(microtrafficsim.core.map.StreetType.UNCLASSIFIED,  false, false);
            case RESIDENTIAL:    return new microtrafficsim.core.map.StreetType(microtrafficsim.core.map.StreetType.RESIDENTIAL,   false, false);
            case SERVICE:        return new microtrafficsim.core.map.StreetType(microtrafficsim.core.map.StreetType.SERVICE,       false, false);

            case ROUNDABOUT:     return new microtrafficsim.core.map.StreetType((short) 0x0000,                                    true,  false);

            case MOTORWAY_LINK:  return new microtrafficsim.core.map.StreetType(microtrafficsim.core.map.StreetType.MOTORWAY,      false, true);
            case TRUNK_LINK:     return new microtrafficsim.core.map.StreetType(microtrafficsim.core.map.StreetType.TRUNK,         false, true);
            case PRIMARY_LINK:   return new microtrafficsim.core.map.StreetType(microtrafficsim.core.map.StreetType.PRIMARY,       false, true);
            case SECONDARY_LINK: return new microtrafficsim.core.map.StreetType(microtrafficsim.core.map.StreetType.SECONDARY,     false, true);
            case TERTIARY_LINK:  return new microtrafficsim.core.map.StreetType(microtrafficsim.core.map.StreetType.TERTIARY,      false, true);

            case LIVING_STREET:  return new microtrafficsim.core.map.StreetType(microtrafficsim.core.map.StreetType.LIVING_STREET, false, false);
            case TRACK:          return new microtrafficsim.core.map.StreetType(microtrafficsim.core.map.StreetType.TRACK,         false, false);
            case ROAD:           return new microtrafficsim.core.map.StreetType(microtrafficsim.core.map.StreetType.ROAD,          false, false);

            default: return null;
        }
    }
}
