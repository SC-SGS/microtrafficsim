package microtrafficsim.osm.features.info;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import microtrafficsim.core.map.features.info.OnewayInfo;


/**
 * Provides functionality for extracting a {@code Onewayinfo} object out of
 * OpenStreetMap tags.
 * 
 * @author Maximilian Luz
 */
public class OnewayInfoParser {
	private static Logger logger = LoggerFactory.getLogger(OnewayInfoParser.class);
	
	/**
	 * Create a {@code OnewayInfo} object for a street out of the given
	 * OpenStreetMap tags.
	 * 
	 * @param tags	the OpenStreetMap tags as Map.
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
		if ("motorway".equals(tags.get("highway"))) {
			return OnewayInfo.FORWARD;
		}
		
		// 'junction=roundabout' implies 'oneway=yes'
		if ("roundabout".equals(tags.get("junction"))) {
			return OnewayInfo.FORWARD;
		}
		
		// the default is 'oneway=no'
		return OnewayInfo.NO;
	}
}
