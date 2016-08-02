package preprocessing.graph.testutils;

import microtrafficsim.core.map.Bounds;
import microtrafficsim.osm.parser.base.DataSet;
import microtrafficsim.osm.parser.ecs.Component;
import microtrafficsim.osm.parser.ecs.entities.NodeEntity;
import microtrafficsim.osm.parser.ecs.entities.WayEntity;
import microtrafficsim.osm.parser.features.FeatureDefinition;
import microtrafficsim.osm.parser.features.FeatureGenerator;
import microtrafficsim.osm.parser.features.streets.StreetComponent;
import microtrafficsim.core.parser.processing.sanitizer.SanitizerWayComponent;
import microtrafficsim.osm.parser.relations.restriction.RestrictionRelation;

import java.util.HashSet;
import java.util.Set;


/**
 * A {@code FeatureGenerator} to extract a cloned {@code DataSet}.
 *
 * @author Maximilian Luz
 */
class DataSetCloneExtractor implements FeatureGenerator {

    private DataSet dataset;

    DataSetCloneExtractor() {
        dataset = null;
    }


    @Override
    public void execute(DataSet dataset, FeatureDefinition feature) {
        DataSet clone = new DataSet();

        clone.bounds = new Bounds(dataset.bounds);

        for (NodeEntity n : dataset.nodes.values())
            clone.nodes.put(n.id, n.clone());

        for (WayEntity w : dataset.ways.values())
            clone.ways.put(w.id, w.clone());

        for (RestrictionRelation r : dataset.relations.getAll(RestrictionRelation.class).values())
            clone.relations.add(r.clone());

        // NOTE: relations other than RestrictionRelations are not needed for the tests

        this.dataset = clone;
    }

    /**
     * Returns the extracted {@code DataSet} or {@code null} if it has not been
     * extracted yet.
     *
     * @return the extracted {@code DataSet}.
     */
    DataSet getDataSet() {
        return this.dataset;
    }


    @Override
    public Set<Class<? extends Component>> getRequiredWayComponents() {
        HashSet<Class<? extends Component>> required = new HashSet<>();
        required.add(StreetComponent.class);
        required.add(SanitizerWayComponent.class);
        return required;
    }
}
