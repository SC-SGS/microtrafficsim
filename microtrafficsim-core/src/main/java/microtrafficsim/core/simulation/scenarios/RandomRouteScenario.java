package microtrafficsim.core.simulation.scenarios;

import microtrafficsim.core.frameworks.shortestpath.ShortestPathAlgorithm;
import microtrafficsim.core.frameworks.shortestpath.astar.impl.FastestWayAStar;
import microtrafficsim.core.frameworks.shortestpath.astar.impl.LinearDistanceAStar;
import microtrafficsim.core.frameworks.vehicle.IVisualizationVehicle;
import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.map.area.RectangleArea;
import microtrafficsim.core.simulation.configs.SimulationConfig;

import java.util.Random;
import java.util.function.Supplier;

/**
 * This class defines a concrete scenarios of a simulation. This means it
 * contains an extension of {@link SimulationConfig} to define simulation
 * parameters. Furthermore, this class serves with a list of uniformly randomly
 * chosen start and end nodes for the vehicles and an age that counts the
 * finished simulation steps.
 *
 * @author Dominic Parga Cacheiro, Jan-Oliver Schmidt
 */
public class RandomRouteScenario extends AbstractStartEndScenario {

  /**
   * Standard constructor.
   *
   * @param config The used config file for this scenarios.
   * @param graph The streetgraph used for this scenarios.
   * @param vehicleFactory This creates vehicles.
   */
  public RandomRouteScenario(SimulationConfig config, StreetGraph graph,
                             Supplier<IVisualizationVehicle> vehicleFactory) {
    super(config,
            graph,
            vehicleFactory);
  }

  @Override
  protected final void createNodeFields() {
    addStartField(new RectangleArea(graph.minLat, graph.minLon, graph.maxLat, graph.maxLon), 1);
    addEndField(new RectangleArea(graph.minLat, graph.minLon, graph.maxLat, graph.maxLon), 1);
  }

  @Override
  protected final Node[] findRouteNodes() {
    return new Node[]{
            getRandomStartNode(),
            getRandomEndNode()
    };
  }

  @Override
  protected Supplier<ShortestPathAlgorithm> createScoutFactory() {
    return new Supplier<ShortestPathAlgorithm>() {
      private Random random = new Random(config.seed);

      @Override
      public ShortestPathAlgorithm get() {
        if (random.nextFloat() < 1.0f) {
          return new FastestWayAStar(config.metersPerCell);
        } else {
          return new LinearDistanceAStar(config.metersPerCell);
        }
      }
    };
  }
}