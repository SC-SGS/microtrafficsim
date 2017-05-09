package microtrafficsim.core.exfmt.injector.simulation;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.base.TypedPolygonAreaSet;
import microtrafficsim.core.map.UnprojectedAreas;

/**
 * @author Dominic Parga Cacheiro
 */
public class UnprojectedAreasInjector implements ExchangeFormat.Injector<UnprojectedAreas> {

    /**
     * @param fmt
     * @param ctx
     * @param dst
     * @param src
     * @throws Exception
     */
    @Override
    public void inject(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container dst, UnprojectedAreas src)
            throws Exception {
        TypedPolygonAreaSet areas = new TypedPolygonAreaSet();
        src.forEach(areas::add);
        dst.set(areas);
    }
}
