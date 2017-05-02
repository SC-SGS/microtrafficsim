package microtrafficsim.core.map.style.predicates;

import microtrafficsim.osm.primitives.Way;


/**
 * A predicate usable to select streets.
 *
 * @author Maximilian Luz
 */
public class BasicStreetBasePredicate implements StreetBasePredicate {
    protected final String type;

    /**
     * Create a new predicate based on the given type-name.
     * @param type the name of the street-type to select.
     */
    public BasicStreetBasePredicate(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public boolean test(Way w) {
        return w.visible && type.equals(w.tags.get("highway"))
                && (w.tags.get("area") == null || w.tags.get("area").equals("no"));
    }
}