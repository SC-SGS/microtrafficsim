package microtrafficsim.core.exfmt.base;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.map.MapProperties;

import java.util.Map;


public class MapInfo extends Container.Entry {
    private MapProperties properties;


    public static MapInfo getDefault() {
        MapInfo info = new MapInfo();
        info.properties = new MapProperties(true);

        return info;
    }


    public void setProperties(MapProperties properties) {
        this.properties = properties;
    }

    public MapProperties getProperties() {
        return this.properties;
    }
}
