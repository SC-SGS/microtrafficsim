package microtrafficsim.osm.parser.ecs.entities;

import microtrafficsim.osm.parser.ecs.Component;
import microtrafficsim.osm.parser.ecs.Entity;
import microtrafficsim.osm.parser.features.FeatureDefinition;
import microtrafficsim.utils.hashing.FNVHashBuilder;

import java.util.HashSet;
import java.util.Set;


/**
 * Node-Entity containing all node-relevant information, including id,
 * latitude, longitude and a set of {@code FeatureDefinition}s which the
 * source-element of this entity matches.
 *
 * @author Maximilian Luz
 */
public class NodeEntity extends Entity {
    public long                   id;
    public double                 lat;
    public double                 lon;
    public Set<FeatureDefinition> features;


    /**
     * Constructs a new {@code NodeEntity} with the given properties.
     *
     * @param id       the id of the node which should be represented.
     * @param lat      the latitude of the node.
     * @param lon      the longitude of the node.
     * @param features the set of {@code FeatureDefinition}s to which this node belongs.
     */
    public NodeEntity(long id, double lat, double lon, Set<FeatureDefinition> features) {
        this.id  = id;
        this.lat = lat;
        this.lon = lon;

        this.features = features;
    }

    @Override
    public NodeEntity clone() {
        Set<FeatureDefinition> features = new HashSet<>(this.features);
        NodeEntity             ce       = new NodeEntity(this.id, this.lat, this.lon, features);

        for (Component component : this.getAll().values()) {
            Component cc = component.clone(ce);
            ce.getAll().put(cc.getType(), cc);
        }

        return ce;
    }

    @Override
    public int hashCode() {
        return new FNVHashBuilder().add(id).getHash();
    }
}
