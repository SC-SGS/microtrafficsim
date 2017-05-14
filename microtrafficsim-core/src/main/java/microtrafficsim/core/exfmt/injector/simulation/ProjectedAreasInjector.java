package microtrafficsim.core.exfmt.injector.simulation;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.base.TypedPolygonAreaSet;
import microtrafficsim.core.exfmt.exceptions.ExchangeFormatException;
import microtrafficsim.core.map.ProjectedAreas;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.core.vis.scenario.areas.Area;

/**
 * @author Dominic Parga Cacheiro
 */
public class ProjectedAreasInjector implements ExchangeFormat.Injector<ProjectedAreas> {

    /**
     * @param fmt
     * @param ctx
     * @param dst
     * @param src
     * @throws Exception
     */
    @Override
    public void inject(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container dst, ProjectedAreas src)
            throws Exception {
        Config cfg = fmt.getConfig().get(Config.class);
        if (cfg == null) throw new ExchangeFormatException(
                "Config for " + ProjectedAreasInjector.class.getSimpleName() + " missing");

        TypedPolygonAreaSet areas = new TypedPolygonAreaSet();
        for (Area area : src) {
            areas.add(area.getUnprojectedArea(cfg.projection));
        }

        dst.set(areas);
    }


    public static class Config extends microtrafficsim.core.exfmt.Config.Entry {
        public Projection projection;
    }
}
