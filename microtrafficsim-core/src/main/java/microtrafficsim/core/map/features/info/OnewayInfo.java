package microtrafficsim.core.map.features.info;


/**
 * Information for oneway-streets.
 *
 * @author Maximilian Luz
 */
public enum OnewayInfo implements ReverseEquals {
    NO,
    FORWARD,
    BACKWARD,
    REVERSIBLE;

    @Override
    public boolean reverseEquals(Object obj) {
        if (!(obj instanceof OnewayInfo)) return false;

        OnewayInfo other = (OnewayInfo) obj;
        return (this == OnewayInfo.FORWARD && other == OnewayInfo.BACKWARD)
                || (this == OnewayInfo.BACKWARD && other == OnewayInfo.FORWARD)
                || (this == OnewayInfo.NO && other == OnewayInfo.NO)
                || (this == OnewayInfo.REVERSIBLE && other == OnewayInfo.REVERSIBLE);
    }
}
