package microtrafficsim.osm.parser.features.streets.info;

import microtrafficsim.osm.parser.ecs.components.traits.Reversible;
import microtrafficsim.osm.parser.features.streets.ReverseEquals;

import java.util.Map;


public class TurnInfo implements ReverseEquals, Reversible {

    public TurnInfo(TurnInfo turns) {
        // TODO
    }


    @Override
    public int hashCode() {
        return 0;       // TODO
    }

    @Override
    public boolean equals(Object obj) {
        return true;   // TODO
    }

    @Override
    public boolean reverseEquals(Object obj) {
        return false;   // TODO
    }

    @Override
    public void reverse() {
        // TODO
    }


    public static TurnInfo parse(Map<String, String> tags) {
        return new TurnInfo(null);  // TODO
    }
}
