package microtrafficsim.core.exfmt.extractor.scenario;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.base.ScenarioMetaInfo;
import microtrafficsim.core.exfmt.exceptions.NotAvailableException;


public class ScenarioMetaInfoExtractor implements ExchangeFormat.Extractor<ScenarioMetaInfo> {

    @Override
    public ScenarioMetaInfo extract(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container src) throws Exception {
        ScenarioMetaInfo meta = src.get(ScenarioMetaInfo.class);
        if (meta == null) throw new NotAvailableException();

        return meta;
    }
}
