package microtrafficsim.core.map.style.predicates;

import microtrafficsim.osm.primitives.Way;

import java.util.function.Predicate;


public interface StreetBasePredicate extends Predicate<Way> {
    String getType();
}
