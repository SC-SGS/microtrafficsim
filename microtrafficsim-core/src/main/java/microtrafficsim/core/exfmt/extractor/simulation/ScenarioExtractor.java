package microtrafficsim.core.exfmt.extractor.simulation;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.base.ScenarioMetaInfo;
import microtrafficsim.core.exfmt.exceptions.NotAvailableException;
import microtrafficsim.core.simulation.scenarios.Scenario;


/**
 * @deprecated
 */
public class ScenarioExtractor implements ExchangeFormat.Extractor<Scenario> {

    @Override
    public Scenario extract(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container src) throws Exception {
        ScenarioMetaInfo meta = src.get(ScenarioMetaInfo.class);
        if (meta == null) throw new NotAvailableException("ScenarioMetaInfo missing");

        return fmt.extract(ctx, src, meta.getScenarioType());
    }
}
