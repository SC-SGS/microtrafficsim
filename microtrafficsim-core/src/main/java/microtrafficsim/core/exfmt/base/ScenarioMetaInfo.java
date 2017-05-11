package microtrafficsim.core.exfmt.base;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.logic.streetgraph.GraphGUID;
import microtrafficsim.core.simulation.scenarios.Scenario;


/**
 * @deprecated
 */
public class ScenarioMetaInfo extends Container.Entry {
    private GraphGUID guid;
    private Class<? extends Scenario> type;


    public void setGraphGUID(GraphGUID guid) {
        this.guid = guid;
    }

    public GraphGUID getGraphGUID() {
        return this.guid;
    }


    public void setScenarioType(Class<? extends Scenario> type) {
        this.type = type;
    }

    public Class<? extends Scenario> getScenarioType() {
        return this.type;
    }
}
