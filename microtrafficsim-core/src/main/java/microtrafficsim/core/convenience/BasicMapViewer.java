package microtrafficsim.core.convenience;

import microtrafficsim.core.map.style.MapStyleSheet;
import microtrafficsim.core.parser.OSMParser;
import microtrafficsim.core.simulation.configs.ScenarioConfig;
import microtrafficsim.core.vis.*;
import microtrafficsim.core.vis.input.KeyCommand;
import microtrafficsim.core.vis.map.projections.Projection;

import java.io.File;

/**
 * @author Maximilian Luz, Dominic Parga Cacheiro
 */
public abstract class BasicMapViewer implements MapViewer {

    private final int initialWindowWidth;
    private final int initialWindowHeight;

    protected MapStyleSheet style;
    private final Projection projection;

    private final boolean printFrameStats;

    private VisualizationPanel vpanel;
    private OSMParser          parser;


    public BasicMapViewer(int width, int height, MapStyleSheet style, Projection projection, boolean printFrameStats) {

        /* window parameters */
        this.initialWindowWidth = width;
        this.initialWindowHeight  = height;

        /* style parameters */
        this.style = style;
        this.projection = projection;

        /* internal settings */
        this.printFrameStats = printFrameStats;
    }

    protected abstract AbstractVisualization getVisualization();

    /*
    |===============|
    | (i) MapViewer |
    |===============|
    */
    @Override
    public VisualizationPanel getVisualizationPanel() {
        return vpanel;
    }

    @Override
    public Projection getProjection() {
        return projection;
    }

    @Override
    public int getInitialWindowWidth() {
        return initialWindowWidth;
    }

    @Override
    public int getInitialWindowHeight() {
        return initialWindowHeight;
    }

    @Override
    public void addOverlay(int index, Overlay overlay) {
        getVisualization().putOverlay(index, overlay);
    }

    @Override
    public void addKeyCommand(short event, short vk, KeyCommand command) {
        getVisualization().getKeyController().addKeyCommand(event, vk, command);
    }

    @Override
    public void show() {
        vpanel.start();

        /* if specified, print frame statistics */
        if (printFrameStats)
            getVisualization().getRenderContext().getAnimator().setUpdateFPSFrames(60, System.out);
    }

    @Override
    public void create(ScenarioConfig config) throws UnsupportedFeatureException {
        /* create the visualizer */
        createVisualization();

        /* create and initialize the VisualizationPanel */
        createVisualizationPanel();
    }

    @Override
    public void createVisualizationPanel() throws UnsupportedFeatureException {

        Visualization vis = getVisualization();

        /* get the default configuration for the visualization */
        VisualizerConfig config = vis.getDefaultConfig();

        /* create and return a new visualization panel */
        vpanel = new VisualizationPanel(vis, config);
    }
}