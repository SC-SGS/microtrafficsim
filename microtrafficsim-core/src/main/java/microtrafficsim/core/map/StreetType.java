package microtrafficsim.core.map;


/**
 * @author Maximilian Luz
 */
public class StreetType {
    public static final int BIT_LINK        = 0x0000_0001;
    public static final int BIT_ROUNDABOUT  = 0x0000_0002;
    public static final int MASK_TYPE       = 0xFFFF_0000;

    public static final short MOTORWAY      = 0x1;
    public static final short TRUNK         = 0x2;
    public static final short PRIMARY       = 0x3;
    public static final short SECONDARY     = 0x4;
    public static final short TERTIARY      = 0x5;
    public static final short UNCLASSIFIED  = 0x6;
    public static final short RESIDENTIAL   = 0x7;
    public static final short SERVICE       = 0x8;
    public static final short LIVING_STREET = 0x9;
    public static final short TRACK         = 0xA;
    public static final short ROAD          = 0xB;


    private final int bits;

    public StreetType(int bits) {
        this.bits = bits;
    }

    public StreetType(short type, boolean roundabout, boolean link) {
        this(((type & 0xFFFF) << 16) | (roundabout ? BIT_ROUNDABOUT : 0) | (link ? BIT_LINK : 0));
    }


    public int getBits() {
        return bits;
    }


    public short getType() {
        return (short) ((bits & MASK_TYPE) >> 16);
    }

    public boolean isRoundabout() {
        return (bits & BIT_ROUNDABOUT) != 0;
    }

    public boolean isLink() {
        return (bits & BIT_LINK) != 0;
    }


    @Override
    public int hashCode() {
        return bits;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof StreetType && this.bits == ((StreetType) obj).bits;
    }
}
