package microtrafficsim.core.parser.processing;

import microtrafficsim.osm.parser.ecs.entities.NodeEntity;
import microtrafficsim.osm.parser.ecs.entities.WayEntity;
import microtrafficsim.osm.parser.features.streets.StreetComponent;
import microtrafficsim.core.parser.processing.Ways.WayLayout;


/**
 * Utility class for {@code Connector}s.
 *
 * @author Maximilian Luz
 */
public class Connectors {
    private Connectors() {}


    /**
     * Creates a new, valid Connector from {@code from} via {@code via} to {@code
     * to} and returns it. If no valid Connector could be created (i.e. {@code
     * from} and {@code to} are two one-way streets going in different directions)
     * this method returns {@code null}. Streets with one-way-type {@code
     * REVERSIBLE} are handled as if the one-way-type would be {@code NO}. If
     * {@code from == to} no valid Connector can be created (it would have no
     * unique meaning) and thus {@code null} is returned.
     *
     * @param via  the NodeEntity via which the created Connector goes.
     * @param from the WayEntity from which the created Connector goes.
     * @param to   the WayEntity to which the created Connector goes.
     * @return a new, valid Connector as described above or {@code null} if no
     * valid Connector could be created.
     */
    public static Connector create(NodeEntity via, WayEntity from, WayEntity to) {
        if (from == to) return null;

        StreetComponent scf = from.get(StreetComponent.class);
        StreetComponent sct = to.get(StreetComponent.class);

        if ((scf == null) != (sct == null)) return null;

        if (scf != null) {
            WayLayout layout = Ways.getLayout(via.id, from, to);
            if (!Ways.isConnectionPossible(layout, scf.oneway, sct.oneway)) return null;
        }

        return new Connector(via, from, to);
    }

    /**
     * Adds the given {@code Connector} to the specific sets in the {@code
     * GraphWayComponent} of the referenced Ways. These Components must be present,
     * otherwise a {@code NullPointerException} will be thrown.
     * If {@code c.from == c.to} the Connector is assumed to be a u-turn.
     *
     * @param c the {@code Connector} to add.
     */
    public static void add(Connector c) {
        if (c == null) return;

        if (c.from == c.to) {       // u-turn connector
            c.to.get(GraphWayComponent.class).uturn.add(c);
        } else {                    // from-to connector
            c.from.get(GraphWayComponent.class).from.add(c);
            c.to.get(GraphWayComponent.class).to.add(c);
        }
    }

    /**
     * Removes the given {@code Connector} from the specific sets in the {@code
     * GraphWayComponent} of the referenced Ways. These Components must be present,
     * otherwise a {@code NullPointerException} will be thrown.
     *
     * @param c the {@code Connector} to remove.
     */
    public static void remove(Connector c) {
        if (c.to == c.from) {       // u-turn connector
            c.to.get(GraphWayComponent.class).uturn.remove(c);
        } else {                    // from-to connector
            c.from.get(GraphWayComponent.class).from.remove(c);
            c.to.get(GraphWayComponent.class).to.remove(c);
        }
    }
}
