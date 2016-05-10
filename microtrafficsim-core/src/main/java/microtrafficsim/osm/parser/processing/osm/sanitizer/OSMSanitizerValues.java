package microtrafficsim.osm.parser.processing.osm.sanitizer;


/**
 * Provides all required values for the {@code OSMDataSetSanitizer}.
 * 
 * @author Maximilian Luz
 */
public interface OSMSanitizerValues {
	
	/**
	 * Returns the maximum speed-limit value which is used to replace {@code
	 * MaxspeedInfo.NONE}.
	 * 
	 * @return the maximum speed-limit in km/h.
	 */
	float getMaximumSpeed();
	
	/**
	 * Returns the speed-limit value for {@code MaxspeedInfo.WALKING}.
	 * 
	 * @return the walking-speed in km/h.
	 */
	float getWalkingSpeed();
	
	/**
	 * Return the speed-limit value deduced from the given highway-type.
	 * 
	 * @param highway	the type of the street as {@code String}.
	 * @return the speed-limit value deduced from {@code highway}.
	 */
	float getMaxspeedFromStreetType(String highway);
	
	/**
	 * Return the number of lanes per direction deduced from the given
	 * highway-type.
	 * 
	 * @param highway	the type of the street as {@code String}.
	 * @return the number of lanes per direction deduced from {@code highway}.
	 */
	int getLanesPerDirectionFromHighwayType(String highway);
}
