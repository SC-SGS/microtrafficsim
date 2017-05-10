package testhelper;

import logic.determinism.BacknangGraphLink;
import microtrafficsim.utils.collections.Tuple;

/**
 * Just a superclass for all link classes. Those link classes do only exist to find resources more easily.
 *
 * @author Dominic Parga Cacheiro
 */
public abstract class ResourceClassLinks {
    public static final Tuple<Class<?>, String> BACKNANG = new Tuple<>(BacknangGraphLink.class, "Backnang.osm");
}
