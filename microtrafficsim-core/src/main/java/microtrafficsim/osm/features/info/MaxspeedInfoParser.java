package microtrafficsim.osm.features.info;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import microtrafficsim.core.map.features.info.MaxspeedInfo;


/**
 * Provides functionality for extracting a {@code MaxspeedInfo} object out of
 * OpenStreetMap tags.
 * 
 * @author Maximilian Luz
 */
public class MaxspeedInfoParser {
	private static Logger logger = LoggerFactory.getLogger(MaxspeedInfoParser.class);
	
	private static Pattern valuetype = Pattern.compile("^([0-9]+(?:\\.(?:[0-9])*)?)(?:\\s*(km/h|kmh|kph|mph|knots))?$");
	private static Pattern zonetype = Pattern.compile("^(.*):(.*)$");
	
	
	/**
	 * Create a {@code MaxspeedInfo} object for a street out of the given
	 * OpenStreetMap tags.
	 * 
	 * @param tags	the OpenStreetMap tags as Map.
	 * @return the parsed {@code MaxspeedInfo}.
	 */
	public static MaxspeedInfo parse(Map<String, String> tags) {
		float both = parseTagValue(tags.get("maxspeed"));
		float forward = parseTagValue(tags.get("maxspeed:forward"));
		float backward = parseTagValue(tags.get("maxspeed:backward"));
		
		if (forward == MaxspeedInfo.UNGIVEN)
			forward = both;
		
		if (backward == MaxspeedInfo.UNGIVEN)
			backward = both;
		
		return new MaxspeedInfo(forward, backward);
	}
	
	/**
	 * Parse a maxspeed-value from the given {@code String}.
	 * 
	 * @param value	the {@code String} to parse.
	 * @return the maxspeed-value as float.
	 */
	private static float parseTagValue(String value) {
		if (value != null) {
			switch (value) {
			case "walk":	return MaxspeedInfo.WALKING;
			case "signals":	return MaxspeedInfo.SIGNALS;
			case "none":	return MaxspeedInfo.NONE;
			
			default:
				Matcher m;
				
				if ((m = valuetype.matcher(value)).matches()) {
					return speedFromStringValue(m.group(1), m.group(2));
				} else if ((m = zonetype.matcher(value)).matches()) {
					logger.debug("zone-info not implemented yet, falling back to default value");
				} else {
					logger.debug("could not parse maxspeed-value '"
							+ value + "', falling back to default value");
				}
			}
		}
			
		return MaxspeedInfo.UNGIVEN;
	}
	
	/**
	 * Parse a speed from the given value and unit Strings.
	 * 
	 * @param value	the speed value.
	 * @param unit	the speed unit. If {@code unit} is empty or {@code null}
	 * 				the unit is assumed to be km/h. Supported units are km/h
	 * 				(kmh, kph), knots and mph.
	 * @return the parsed speed in km/h
	 */
	private static float speedFromStringValue(String value, String unit) {
		float unitFactor = 1.0f;
		
		if (unit != null) {
			switch (unit) {
			case "mph":
				unitFactor = 1.609f;
				break;
				
			case "knots":
				unitFactor = 1.852f;
				break;
				
			case "":		// empty: assume km/h
			case "km/h":
			case "kmh":
			case "kph":
				unitFactor = 1.0f;
				
			default:
				throw new IllegalArgumentException("unknown unit '" + unit + "' for speed value");
			}
		}
		
		return Float.parseFloat(value) * unitFactor;
	}
}
