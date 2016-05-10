package microtrafficsim.osm.parser.processing.osm.sanitizer;


/**
 * The default implementation for {@code OSMSanitizerValues}.
 * 
 * @author Maximilian Luz
 */
public class DefaultOSMSanitizerValues implements OSMSanitizerValues {
	
	public static final float SPEED_MAX = 200.0f;
	public static final float SPEED_WALKING = 7.0f;


	@Override
	public float getMaximumSpeed() {
		return SPEED_MAX;
	}

	@Override
	public float getWalkingSpeed() {
		return SPEED_WALKING;
	}

	@Override
	public float getMaxspeedFromStreetType(String highway) {
		if (highway == null) return 50.0f;
		
		switch (highway) {
		case "motorway":
		case "motorway_link":
		case "trunk":
		case "trunk_link":
			return 120;
			
		case "primary":
		case "primary_link":
		case "secondary":
		case "secondary_link":
		case "tertiary":
		case "tertiary_link":
		case "unclassified":
			return 50;
			
		case "residential":
		case "service":
		case "track":
		case "path":
			return 30;
			
		case "living_street":
		case "pedestrian":
			return SPEED_WALKING;
			
		default:
			return 50;
		}
	}

	
	@Override
	public int getLanesPerDirectionFromHighwayType(String highway) {
		if (highway == null) return 1;
		
		switch (highway) {
		case "motorway":
		case "trunk":
			return 2;
				
		default:
			return 1;
		}
	}
}
