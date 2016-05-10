package microtrafficsim.osm.features.info;

import java.util.Map;

import microtrafficsim.core.map.features.info.StreetType;


/**
 * Provides functionality for extracting a {@code StreetType} object out of
 * OpenStreetMap tags.
 * 
 * @author Maximilian Luz
 */
public class StreetTypeParser {
	
	/**
	 * Create a {@code LaneInfo} object for a street out of the given
	 * OpenStreetMap tags.
	 * 
	 * @param tags	the OpenStreetMap tags as Map.
	 * @return the parsed {@code LaneInfo}
	 */
	public static StreetType parse(Map<String, String> tags) {
		String highway = tags.get("highway");
		if (highway == null) return null;
		
		switch (highway) {
		case "motorway":		return StreetType.MOTORWAY;
		case "trunk":			return StreetType.TRUNK;
		case "primary":			return StreetType.PRIMARY;
		case "secondary":		return StreetType.SECONDARY;
		case "tertiary":		return StreetType.TERTIARY;
		case "unclassified":	return StreetType.UNCLASSIFIED;
		case "residential":		return StreetType.RESIDENTIAL;
		case "service":			return StreetType.SERVICE;
		
		case "motorway_link":	return StreetType.MOTORWAY_LINK;
		case "trunk_link":		return StreetType.TRUNK_LINK;
		case "primary_link":	return StreetType.PRIMARY_LINK;
		case "tertiary_link":	return StreetType.TERTIARY_LINK;
		
		case "living_street":	return StreetType.LIVING_STREET;
		case "track":			return StreetType.TRACK;
		case "road":			return StreetType.ROAD;
		
		default:				return null;
		}
	}
}
