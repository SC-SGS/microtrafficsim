package microtrafficsim.core.exfmt.extractor.simulation;

import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.base.TypedPolygonAreaSet;
import microtrafficsim.core.exfmt.exceptions.NotAvailableException;
import microtrafficsim.core.map.UnprojectedAreas;

/**
 * @author Dominic Parga Cacheiro
 */
public class UnprojectedAreasExtractor implements ExchangeFormat.Extractor<UnprojectedAreas> {

    @Override
    public UnprojectedAreas extract(ExchangeFormat fmt, ExchangeFormat.Context ctx, Container src) throws Exception {
        TypedPolygonAreaSet areaSet = src.get(TypedPolygonAreaSet.class);
        if (areaSet == null) throw new NotAvailableException(TypedPolygonAreaSet.class.getSimpleName() + " missing");

        UnprojectedAreas areas = new UnprojectedAreas();
        areas.addAll(areaSet.getAll());

        return areas;
    }
}
