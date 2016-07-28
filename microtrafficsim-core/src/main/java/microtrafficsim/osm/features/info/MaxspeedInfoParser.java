package microtrafficsim.osm.features.info;

import microtrafficsim.core.map.features.info.MaxspeedInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Provides functionality for extracting a {@code MaxspeedInfo} object out of
 * OpenStreetMap tags.
 *
 * @author Maximilian Luz
 */
public class MaxspeedInfoParser {
    private MaxspeedInfoParser() {}
    private static Logger logger = LoggerFactory.getLogger(MaxspeedInfoParser.class);

    private static Pattern valuetype = Pattern.compile("^([0-9]+(?:\\.(?:[0-9])*)?)(?:\\s*(km/h|kmh|kph|mph|knots))?$");
    private static Pattern zonetype  = Pattern.compile("^(.*):(.*)$");

    private static Map<String, Map<String, Float>> zoneinfo;

    static {    // values from http://wiki.openstreetmap.org/wiki/Speed_limits#Country_code.2Fcategory_conversion_table
        HashMap<String, Float> at = new HashMap<>(4);
        at.put("urban",     50.f);
        at.put("rural",    100.f);
        at.put("trunk",    100.f);
        at.put("motorway", 130.f);

        HashMap<String, Float> ch = new HashMap<>(4);
        ch.put("urban",     50.f);
        ch.put("rural",     80.f);
        ch.put("trunk",    100.f);
        ch.put("motorway", 120.f);

        HashMap<String, Float> cz = new HashMap<>(4);
        cz.put("urban",     50.f);
        cz.put("rural",     90.f);
        cz.put("trunk",    130.f);
        cz.put("motorway", 130.f);

        HashMap<String, Float> dk = new HashMap<>(3);
        dk.put("urban",     50.f);
        dk.put("rural",     80.f);
        dk.put("motorway", 130.f);

        HashMap<String, Float> de = new HashMap<>(4);
        de.put("living_street", 7.f);
        de.put("urban",        50.f);
        de.put("rural",       100.f);
        de.put("motorway", MaxspeedInfo.NONE);

        HashMap<String, Float> fi = new HashMap<>(4);
        fi.put("urban",     50.f);
        fi.put("rural",     80.f);
        fi.put("trunk",    100.f);
        fi.put("motorway", 120.f);

        HashMap<String, Float> fr = new HashMap<>(4);
        fr.put("urban",     50.f);
        fr.put("rural",     90.f);
        fr.put("trunk",    110.f);
        fr.put("motorway", 130.f);

        HashMap<String, Float> hu = new HashMap<>(4);
        hu.put("urban",     50.f);
        hu.put("rural",     90.f);
        hu.put("trunk",    110.f);
        hu.put("motorway", 130.f);

        HashMap<String, Float> it = new HashMap<>(4);
        it.put("urban",     50.f);
        it.put("rural",     90.f);
        it.put("trunk",    110.f);
        it.put("motorway", 130.f);

        HashMap<String, Float> jp = new HashMap<>(2);
        jp.put("national",  60.f);
        jp.put("motorway", 100.f);

        HashMap<String, Float> ro = new HashMap<>(4);
        ro.put("urban",     50.f);
        ro.put("rural",     90.f);
        ro.put("trunk",    100.f);
        ro.put("motorway", 130.f);

        HashMap<String, Float> ru = new HashMap<>(4);
        ru.put("living_street", 20.f);
        ru.put("rural",         90.f);
        ru.put("urban",         60.f);
        ru.put("motorway",     110.f);

        HashMap<String, Float> sk = new HashMap<>(4);
        sk.put("urban",     50.f);
        sk.put("rural",     90.f);
        sk.put("trunk",    130.f);
        sk.put("motorway", 130.f);

        HashMap<String, Float> sl = new HashMap<>(4);
        sl.put("urban",     50.f);
        sl.put("rural",     90.f);
        sl.put("trunk",    110.f);
        sl.put("motorway", 130.f);

        HashMap<String, Float> se = new HashMap<>(4);
        se.put("urban",     50.f);
        se.put("rural",     70.f);
        se.put("trunk",     90.f);
        se.put("motorway", 110.f);

        HashMap<String, Float> gb = new HashMap<>(3);
        gb.put("nsl_single", 96.5606f);
        gb.put("nsl_dual",  112.6540f);
        gb.put("motorway",  112.6540f);

        HashMap<String, Float> ua = new HashMap<>(4);
        ua.put("urban",     60.f);
        ua.put("rural",     90.f);
        ua.put("trunk",    110.f);
        ua.put("motorway", 130.f);

        zoneinfo = new HashMap<>();
        zoneinfo.put("at", at);
        zoneinfo.put("ch", ch);
        zoneinfo.put("cz", cz);
        zoneinfo.put("dk", dk);
        zoneinfo.put("de", de);
        zoneinfo.put("fi", fi);
        zoneinfo.put("fr", fr);
        zoneinfo.put("hu", hu);
        zoneinfo.put("it", it);
        zoneinfo.put("jp", jp);
        zoneinfo.put("ro", ro);
        zoneinfo.put("ru", ru);
        zoneinfo.put("sk", sk);
        zoneinfo.put("sl", sl);
        zoneinfo.put("se", se);
        zoneinfo.put("gb", gb);
        zoneinfo.put("ua", ua);
    }


    /**
     * Create a {@code MaxspeedInfo} object for a street out of the given
     * OpenStreetMap tags.
     *
     * @param tags the OpenStreetMap tags as Map.
     * @return the parsed {@code MaxspeedInfo}.
     */
    public static MaxspeedInfo parse(Map<String, String> tags) {
        float both     = parseTagValue(tags.get("maxspeed"));
        float forward  = parseTagValue(tags.get("maxspeed:forward"));
        float backward = parseTagValue(tags.get("maxspeed:backward"));

        if (forward == MaxspeedInfo.UNGIVEN)  forward  = both;
        if (backward == MaxspeedInfo.UNGIVEN) backward = both;

        return new MaxspeedInfo(forward, backward);
    }

    /**
     * Parse a maxspeed-value from the given {@code String}.
     *
     * @param value the {@code String} to parse.
     * @return the maxspeed-value as float.
     */
    private static float parseTagValue(String value) {
        if (value != null) {
            switch (value) {
            case "walk": return MaxspeedInfo.WALKING;
            case "signals": return MaxspeedInfo.SIGNALS;
            case "none": return MaxspeedInfo.NONE;

            default:
                Matcher m;

                if ((m = valuetype.matcher(value)).matches())
                    return speedFromStringValue(m.group(1), m.group(2));
                else if ((m = zonetype.matcher(value)).matches())
                    return speedFromZoneInfo(m.group(1), m.group(2));
                else
                    logger.debug("could not parse maxspeed-value '" + value + "', falling back to default value");
            }
        }

        return MaxspeedInfo.UNGIVEN;
    }

    /**
     * Parse a speed from the given value and unit Strings.
     *
     * @param value the speed value.
     * @param unit  the speed unit. If {@code unit} is empty or {@code null}
     *              the unit is assumed to be km/h. Supported units are km/h
     *              (kmh, kph), knots and mph.
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

            case "":    // assume km/h
            case "km/h":
            case "kmh":
            case "kph":
                unitFactor = 1.0f;
                break;

            default:
                throw new IllegalArgumentException("unknown unit '" + unit + "' for speed value");
            }
        }

        return Float.parseFloat(value) * unitFactor;
    }

    /**
     * Parse a speed from the given zone info.
     *
     * @param country the country code of the zone.
     * @param zone    the zone code of the zone.
     * @return the speed associated with the given zone info, or
     * {@code MaxspeedInfo.UNGIVEN} if the zone is unknown.
     */
    private static float speedFromZoneInfo(String country, String zone) {
        Map<String, Float> local = zoneinfo.get(country.toLowerCase());
        if (local == null) {
            logger.debug("could not parse maxspeed-value, country-code '" + country
                         + "' unknown, falling back to default value");
            return MaxspeedInfo.UNGIVEN;
        }

        Float speed = local.get(zone);
        if (speed == null) {
            logger.debug("could not parse maxspeed-value, zone-info '" + country + ":" + zone
                         + "' unknown, falling back to default value");
            return MaxspeedInfo.UNGIVEN;
        }

        return speed;
    }
}
