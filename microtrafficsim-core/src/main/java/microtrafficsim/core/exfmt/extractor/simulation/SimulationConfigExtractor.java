package microtrafficsim.core.exfmt.extractor.simulation;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.base.SimulationConfigInfo;
import microtrafficsim.core.exfmt.exceptions.ExchangeFormatException;
import microtrafficsim.core.exfmt.exceptions.NotAvailableException;
import microtrafficsim.core.simulation.configs.SimulationConfig;

/**
 * @author Dominic Parga Cacheiro
 */
public class SimulationConfigExtractor implements ExchangeFormat.Extractor<SimulationConfig> {

    /**
     * @param fmt
     * @param ctx unused
     * @param src
     * @return The given config via this {@link Config#config extractor config}
     * @throws Exception
     */
    @Override
    public SimulationConfig extract(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container src) throws Exception {

        Config fmtcfg = fmt.getConfig().get(Config.class);
        if (fmtcfg == null) throw new ExchangeFormatException("Config for " + getClass().getSimpleName() + " missing");

        SimulationConfig config = fmtcfg.config;


        SimulationConfigInfo info = src.get(SimulationConfigInfo.class);
        if (info == null) throw new NotAvailableException(SimulationConfigInfo.class.getSimpleName() + " missing");
        info.update(config);

        return config;
    }


    public static class Config extends microtrafficsim.core.exfmt.Config.Entry {
        public SimulationConfig config;
    }
}
