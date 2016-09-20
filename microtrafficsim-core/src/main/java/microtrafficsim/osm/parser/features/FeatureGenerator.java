package microtrafficsim.osm.parser.features;

import microtrafficsim.osm.parser.base.DataSet;
import microtrafficsim.osm.parser.ecs.Component;

import java.util.HashSet;
import java.util.Set;


/**
 * Basic interface for a Feature-Generator. Feature-Generators are the last step
 * in the basic processing pipeline.
 *
 * @author Maximilian Luz
 */
public interface FeatureGenerator {

    class Properties {
        public enum BoundaryManagement { NONE, RECALCULATE, CLIP }

        public BoundaryManagement bounds = BoundaryManagement.NONE;
    }

    /**
     * Execute this generator on the specified DataSet to generate Features for the
     * given FeatureDefinition.
     *
     * @param dataset    the DataSet on which to execute this generator.
     * @param feature    the FeautureDefinition describing the Features which should
     *                   be generated.
     * @param properties the properties to be used when generating this feature.
     */
    void execute(DataSet dataset, FeatureDefinition feature, Properties properties) throws Exception;

    /**
     * Returns the type of {@code Component}s which need to be initialized on a
     * newly created {@code NodeEntity} in the data-abstraction phase for this
     * generator.
     *
     * @return the Components which this generator needs to be initialized on a
     * NodeEntity.
     */
    default Set<Class<? extends Component>> getRequiredNodeComponents() {
        return new HashSet<>();
    }

    /**
     * Returns the type of {@code Component}s which need to be initialized on a
     * newly created {@code WayEntity} in the data-abstraction phase for this
     * generator.
     *
     * @return the Components which this generator needs to be initialized on a
     * WayEntity.
     */
    default Set<Class<? extends Component>> getRequiredWayComponents() {
        return new HashSet<>();
    }
}
