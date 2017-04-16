package microtrafficsim.core.exfmt.extractor.streetgraph;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.logic.streetgraph.StreetGraph;


public class StreetGraphExtractor implements ExchangeFormat.Extractor<StreetGraph> {

    // TODO (GraphEdgeComponent extractor): get meters per cell and priorityFn from config

    @Override
    public StreetGraph extract(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container src) throws Exception {
        return null;
    }
}
