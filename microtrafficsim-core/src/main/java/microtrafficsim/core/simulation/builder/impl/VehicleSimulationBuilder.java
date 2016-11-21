package microtrafficsim.core.simulation.builder.impl;

import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.vehicles.AbstractVehicle;
import microtrafficsim.core.logic.vehicles.VehicleStateListener;
import microtrafficsim.core.map.area.Area;
import microtrafficsim.core.simulation.builder.Builder;
import microtrafficsim.core.simulation.scenarios.Scenario;
import microtrafficsim.core.simulation.utils.ODMatrix;
import microtrafficsim.core.simulation.utils.SparseODMatrix;
import microtrafficsim.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * This class is an implementation of {@link Builder} for {@link AbstractVehicle}s.
 *
 * @author Dominic Parga Cacheiro
 */
public class VehicleSimulationBuilder implements Builder {

    private Logger logger = LoggerFactory.getLogger(VehicleSimulationBuilder.class);

    /**
     * Addition to superclass-doc: If the ODMatrix should be created using origin/destination fields, all nodes are
     * collected in one set, so there are no duplicates and the nodes are chosen distributed uniformly at random.
     */
    @Override
    public Scenario prepare(Scenario scenario) {

        // ---------- ---------- ---------- ---------- --
        // reset all
        // ---------- ---------- ---------- ---------- --
        scenario.setPrepared(false);
        scenario.getVehicleContainer().clearAll();
        scenario.getGraph().reset();

        // ---------- ---------- ---------- ---------- --
        // check if odmatrix has to be built
        // ---------- ---------- ---------- ---------- --
        if (!scenario.isODMatrixBuilt()) {

            ArrayList<Node>
                    origins = new ArrayList<>(),
                    destinations = new ArrayList<>();

            // ---------- ---------- ---------- ---------- --
            // for each graph node, check its location relative to the origin/destination fields
            // ---------- ---------- ---------- ---------- --
            Iterator<Node> nodes = scenario.getGraph().getNodeIterator();
            while (nodes.hasNext()) {
                Node node = nodes.next();

                // for each node being in an origin field => add it
                for (Area area : scenario.getOriginFields())
                    if (area.contains(node)) {
                        origins.add(node);
                        break;
                    }

                // for each node being in a destination field => add it
                for (Area area : scenario.getDestinationFields())
                    if (area.contains(node)) {
                        destinations.add(node);
                        break;
                    }
            }

            // ---------- ---------- ---------- ---------- --
            // build matrix
            // ---------- ---------- ---------- ---------- --
            ODMatrix odmatrix = new SparseODMatrix();
            Random random = scenario.getConfig().rndGenGenerator.next();
            for (int i = 0; i < scenario.getConfig().maxVehicleCount; i++) {
                int rdmOrig = random.nextInt(origins.size());
                int rdmDest = random.nextInt(destinations.size());
                odmatrix.inc(origins.get(rdmOrig), destinations.get(rdmDest));
            }

            // ---------- ---------- ---------- ---------- --
            // finish
            // ---------- ---------- ---------- ---------- --
            scenario.setODMatrix(odmatrix);
            scenario.setODMatrixBuilt(true);
        }

        ODMatrix odmatrix = scenario.getODMatrix();










        // createAndAddVehicles(listener);

        if (startFields.size() <= 0 || endFields.size() <= 0) {
            if (startFields.size() <= 0) logger.info("You are using no or only empty start fields!");
            if (endFields.size() <= 0) logger.info("You are using no or only empty end fields!");
        } else {
            logger.info("CREATING VEHICLES started");
            long time = System.nanoTime();

            if (config.multiThreading.nThreads > 1)
                multiThreadedVehicleCreation(listener);
            else
                singleThreadedVehicleCreation(listener);

            logger.info(StringUtils.buildTimeString(
                    "CREATING VEHICLES finished after ",
                    System.nanoTime() - time,
                    "ns"
            ).toString());
        }


        scenario.setPrepared(true);
        return scenario;
    }

    private void multiThreadedVehicleCreation(VehicleStateListener listener) {

    }
}
