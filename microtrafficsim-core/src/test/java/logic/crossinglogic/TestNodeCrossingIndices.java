package logic.crossinglogic;

import logic.validation.Main;
import logic.validation.ScenarioType;
import microtrafficsim.build.BuildSetup;
import microtrafficsim.core.logic.DirectedEdge;
import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.map.style.impl.DarkStyleSheet;
import microtrafficsim.core.mapviewer.MapViewer;
import microtrafficsim.core.mapviewer.TileBasedMapViewer;
import microtrafficsim.core.parser.OSMParser;
import microtrafficsim.core.simulation.configs.ScenarioConfig;
import microtrafficsim.utils.Tuple;
import microtrafficsim.utils.resources.PackagedResource;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * @author Dominic Parga Cacheiro
 */
public class TestNodeCrossingIndices {

    private static MapViewer      mapviewer;
    private static ScenarioConfig config;

    @Test
    public void testPlusCrossroad() {

        /* file */
        File file = null;
        try {
            file = new PackagedResource(Main.class, ScenarioType.PLUS_CROSSROAD.getOSMFilename()).asTemporaryFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mapviewer.createParser(config);

        StreetGraph graph = null;
        try {
            /* parse file and create tiled provider */
            OSMParser.Result result = mapviewer.parse(file);
            graph = result.streetgraph;
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.err.println(graph);

        /* test */
        for (Node node : graph.getNodes()) {
            Iterator<Tuple<DirectedEdge, Byte>> crossingIndices = node.iterCrossingIndices();

            while (crossingIndices.hasNext()) {
                Tuple<DirectedEdge, Byte> crossingTuple = crossingIndices.next();
                DirectedEdge edge  = crossingTuple.obj0;
                Byte crossingIndex = crossingTuple.obj1;

            }
        }
    }

    @BeforeClass
    public static void buildSetup() {
        /* build setup: logging */
        BuildSetup.TRACE_ENABLED = false;
        BuildSetup.DEBUG_ENABLED = true;
        BuildSetup.INFO_ENABLED  = true;
        BuildSetup.WARN_ENABLED  = true;
        BuildSetup.ERROR_ENABLED = true;

        mapviewer = new TileBasedMapViewer(new DarkStyleSheet());
        config = new ScenarioConfig();
    }
}