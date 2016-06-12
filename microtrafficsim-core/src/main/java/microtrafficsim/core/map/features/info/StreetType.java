package microtrafficsim.core.map.features.info;


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
	ROAD
}
