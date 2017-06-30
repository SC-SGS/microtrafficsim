package microtrafficsim.core.map;

/**
 * Just a super-interface for {@link SegmentFeatureProvider} and {@link TileFeatureProvider} for easier handling.
 *
 * @author Dominic Parga Cacheiro
 */
public interface MapProvider {
    public MapProperties getProperties();
}
