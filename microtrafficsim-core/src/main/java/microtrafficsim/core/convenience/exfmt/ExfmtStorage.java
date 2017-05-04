package microtrafficsim.core.convenience.exfmt;

import microtrafficsim.core.convenience.mapviewer.TileBasedMapViewer;
import microtrafficsim.core.convenience.parser.DefaultParserConfig;
import microtrafficsim.core.convenience.utils.FileFilters;
import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.base.ScenarioRouteSet;
import microtrafficsim.core.exfmt.exceptions.NotAvailableException;
import microtrafficsim.core.exfmt.extractor.map.QuadTreeTiledMapSegmentExtractor;
import microtrafficsim.core.exfmt.extractor.simulation.SimulationConfigExtractor;
import microtrafficsim.core.exfmt.extractor.streetgraph.StreetGraphExtractor;
import microtrafficsim.core.exfmt.injector.simulation.SimulationConfigInjector;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.logic.streetgraph.StreetGraph;
import microtrafficsim.core.map.MapProvider;
import microtrafficsim.core.map.MapSegment;
import microtrafficsim.core.map.tiles.QuadTreeTiledMapSegment;
import microtrafficsim.core.parser.OSMParser;
import microtrafficsim.core.serialization.ExchangeFormatSerializer;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.utils.RouteMatrix;
import microtrafficsim.utils.collections.Tuple;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * Should simplify loading/saving simulation files
 *
 * @author Dominic Parga Cacheiro
 */
public class ExfmtStorage {

    private static final Logger logger = new EasyMarkableLogger(ExfmtStorage.class);


    private OSMParser parser;
    private ExchangeFormat exfmt;
    private ExchangeFormatSerializer serializer;


    public ExfmtStorage(SimulationConfig config, TileBasedMapViewer mapviewer) {
        parser = DefaultParserConfig.get(config).build();


        /* create exchange format and serializer */
        serializer = ExchangeFormatSerializer.create();
        exfmt = ExchangeFormat.getDefault();

        exfmt.getConfig().set(QuadTreeTiledMapSegmentExtractor.Config.getDefault(
                mapviewer.getPreferredTilingScheme(), mapviewer.getPreferredTileGridLevel()));

        exfmt.getConfig().set(new StreetGraphExtractor.Config(config));
    }


    /*
    |=====|
    | map |
    |=====|
    */
    /**
     * Loads the given file depending on its map type (OSM or MTSM)
     */
    public Tuple<Graph, MapProvider> loadMap(File file) throws InterruptedException {

        try {
            if (FileFilters.MAP_OSM_XML.accept(file)) {
                OSMParser.Result result = parser.parse(file);
                return new Tuple<>(result.streetgraph, result.segment);
            } else if (FileFilters.MAP_EXFMT.accept(file)) {
                ExchangeFormat.Manipulator manipulator = exfmt.manipulator(serializer.read(file));

                MapProvider provider;
                try {
                    provider = manipulator.extract(QuadTreeTiledMapSegment.class);
                } catch (NotAvailableException e) { // thrown when no TileGrid available
                    provider = manipulator.extract(MapSegment.class);
                }

                return new Tuple<>(manipulator.extract(StreetGraph.class), provider);
            }
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean saveMap(File file, Tuple<Graph, MapProvider> tuple) {
        return saveMap(file, tuple.obj0, tuple.obj1);
    }

    public boolean saveMap(File file, Graph graph, MapProvider provider) {
        try {
            serializer.write(file, exfmt.manipulator()
                    .inject(provider)
                    .inject(graph)
                    .getContainer());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /*
    |========|
    | config |
    |========|
    */
    /**
     * @param file
     * @param draft see {@link SimulationConfigExtractor.Config#setConfig(SimulationConfig) Exfmt.Config.setConfig(...)}
     * @return
     */
    public SimulationConfig loadConfig(File file, SimulationConfig draft) {
        /* prepare exfmt config */
        SimulationConfigExtractor.Config cfg = new SimulationConfigExtractor.Config();
        cfg.setConfig(draft);
        exfmt.getConfig().set(cfg);


        /* prepare extractor */
        ExchangeFormat.Manipulator manipulator = null;
        try {
            manipulator = exfmt.manipulator(serializer.read(file));
        } catch (Exception e) {
            e.printStackTrace();
        }


        /* load config */
        SimulationConfig newConfig = null;
        if (manipulator != null) {
            try {
                newConfig = manipulator.extract(SimulationConfig.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return newConfig;
    }

    /**
     * @param file
     * @param config see
     * {@link SimulationConfigInjector#inject(ExchangeFormat, ExchangeFormat.Context, Container, SimulationConfig)}
     *               for more detailed information
     * @return
     */
    public boolean saveConfig(File file, SimulationConfig config) {
        try {
            serializer.write(file, exfmt.manipulator()
                    .inject(config)
                    .getContainer());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /*
    |========|
    | routes |
    |========|
    */
    public RouteMatrix loadRoutes(File file) {
        return null;
    }

    public boolean saveRoutes(File file, RouteMatrix routeMatrix) {
        try {
            serializer.write(file, exfmt.manipulator()
                    .inject(routeMatrix)
                    .getContainer());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
