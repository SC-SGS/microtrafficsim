package microtrafficsim.core.simulation.builder.impl;

import microtrafficsim.core.logic.vehicles.AbstractVehicle;
import microtrafficsim.core.map.area.Area;
import microtrafficsim.core.simulation.builder.Builder;
import microtrafficsim.core.simulation.scenarios.Scenario;
import microtrafficsim.utils.StringUtils;

import java.util.HashSet;

/**
 * This class is an implementation of {@link Builder} for {@link AbstractVehicle}s.
 *
 * @author Dominic Parga Cacheiro
 */
public class VehicleSimulationBuilder implements Builder {

    @Override
    public Scenario prepare(Scenario scenario) {

        scenario.setPrepared(false);
        scenario.getVehicleContainer().clearAll();
        scenario.getGraph().reset();

        if (!scenario.isODMatrixBuilt()) {

            

            HashSet<Area> emptyStartFields = new HashSet<>(startFields.keySet());
            HashSet<Area> emptyEndFields   = new HashSet<>(endFields.keySet());

            scenario.getGraph().getNodeIterator().forEachRemaining(node -> {
                startFields.keySet().stream().filter(polygon -> polygon.contains(node)).forEach(polygon -> {
                    emptyStartFields.remove(polygon);
                    startFields.get(polygon).add(node);
                });

                endFields.keySet().stream().filter(polygon -> polygon.contains(node)).forEach(polygon -> {
                    emptyEndFields.remove(polygon);
                    endFields.get(polygon).add(node);
                });
            });

            if (emptyStartFields.size() > 0) {
                try {
                    throw new Exception("At least one selected start field is empty!");
                } catch (Exception e) { e.printStackTrace(); }
            }
            if (emptyEndFields.size() > 0) {
                try {
                    throw new Exception("At least one selected end field is empty!");
                } catch (Exception e) { e.printStackTrace(); }
            }
            //        emptyStartFields.forEach(area -> {
            //            startFields.remove(area);
            //            startWheel.remove(area);
            //        });
            //        emptyEndFields.forEach(area -> {
            //            endFields.remove(area);
            //            endWheel.remove(area);
            //        });
            if (true) {
                // just to prevent comment collapsing
            }



            scenario.setODMatrix(null);
            scenario.setODMatrixBuilt(true);
        }












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
}
