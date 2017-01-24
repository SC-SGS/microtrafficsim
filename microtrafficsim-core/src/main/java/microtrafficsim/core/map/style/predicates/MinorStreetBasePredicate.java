package microtrafficsim.core.map.style.predicates;

import microtrafficsim.osm.primitives.Way;

import java.util.function.Predicate;

/**
 * A predicate usable to select minor streets.
 */
public class MinorStreetBasePredicate implements Predicate<Way> {

    private final String type;

    /**
     * Create a new predicate based on the given type-name.
     * @param type the name of the street-type to select.
     */
    public MinorStreetBasePredicate(String type) {
        this.type = type;
    }

    @Override
    public boolean test(Way w) {
        return w.visible && type.equals(w.tags.get("highway"))
                && (w.tags.get("area") == null || w.tags.get("area").equals("no"));
    }
}