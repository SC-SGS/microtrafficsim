package preprocessing.graph.testutils;

import microtrafficsim.osm.parser.ecs.Component;
import microtrafficsim.osm.parser.ecs.Entity;
import microtrafficsim.osm.parser.ecs.entities.WayEntity;
import microtrafficsim.osm.parser.relations.restriction.RestrictionRelation;
import microtrafficsim.utils.collections.HashMultiSet;

import java.util.HashSet;


/**
 * A Component for {@code NodeEntities} to store all referenced street-graph
 * ways and restrictions, used by the {@code GraphConsistencyTest}.
 *
 * @author Maximilian Luz
 */
public class TestNodeComponent extends Component {

    public HashMultiSet<WayEntity>      ways;
    public HashSet<RestrictionRelation> restrictions;

    public TestNodeComponent(Entity entity) {
        super(entity);

        this.ways         = new HashMultiSet<>();
        this.restrictions = new HashSet<>();
    }

    @Override
    public Class<? extends Component> getType() {
        return TestNodeComponent.class;
    }

    @Override
    public TestNodeComponent clone(Entity e) {
        TestNodeComponent tnc = new TestNodeComponent(e);
        tnc.ways              = new HashMultiSet<>(this.ways);
        return tnc;
    }
}
