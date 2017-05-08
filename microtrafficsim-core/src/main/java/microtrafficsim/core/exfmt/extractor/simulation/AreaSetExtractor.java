package microtrafficsim.core.exfmt.extractor.simulation;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.base.ScenarioAreaSet;
import microtrafficsim.core.exfmt.exceptions.NotAvailableException;

/**
 * @author Dominic Parga Cacheiro
 */
public class AreaSetExtractor implements ExchangeFormat.Extractor<ScenarioAreaSet> {

    @Override
    public ScenarioAreaSet extract(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container src) throws Exception {
        ScenarioAreaSet areas = src.get(ScenarioAreaSet.class);
        if (areas == null) throw new NotAvailableException("ScenarioAreaSet missing");

        return areas;
    }
}
