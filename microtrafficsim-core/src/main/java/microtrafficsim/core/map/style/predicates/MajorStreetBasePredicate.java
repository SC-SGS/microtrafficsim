package microtrafficsim.core.map.style.predicates;

import microtrafficsim.osm.primitives.Way;

/**
 * A predicate usable to select major streets (i.e. streets and their associated link-type).
 *
 * @author Maximilian Luz
 */
public class MajorStreetBasePredicate extends BasicStreetBasePredicate {

    private final String link;

    /**
     * Create a new predicate based on the given type-name.
     * @param type the name of the street-type to select.
     */
    public MajorStreetBasePredicate(String type) {
        super(type);
        this.link = type + "_link";
    }

    public String getLinkType() {
        return link;
    }

    @Override
    public boolean test(Way w) {
        return w.visible && (type.equals(w.tags.get("highway")) || link.equals(w.tags.get("highway")))
                && (w.tags.get("area") == null || w.tags.get("area").equals("no"));
    }
}