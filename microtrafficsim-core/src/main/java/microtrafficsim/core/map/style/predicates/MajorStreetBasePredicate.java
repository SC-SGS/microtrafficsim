package microtrafficsim.core.map.style.predicates;

import microtrafficsim.osm.primitives.Way;

import java.util.function.Predicate;

/**
 * A predicate usable to select major streets (i.e. streets and their associated link-type).
 */
public class MajorStreetBasePredicate implements Predicate<Way> {

    private final String type;
    private final String link;

    /**
     * Create a new predicate based on the given type-name.
     * @param type the name of the street-type to select.
     */
    public MajorStreetBasePredicate(String type) {
        this.type = type;
        this.link = type + "_link";
    }

    @Override
    public boolean test(Way w) {
        return w.visible && (type.equals(w.tags.get("highway")) || link.equals(w.tags.get("highway")))
                && (w.tags.get("area") == null || w.tags.get("area").equals("no"));
    }
}