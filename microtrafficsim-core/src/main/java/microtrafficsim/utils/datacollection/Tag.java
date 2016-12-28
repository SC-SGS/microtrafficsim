package microtrafficsim.utils.datacollection;


/**
 * <p>
 * This class is just a wrapper for a string to ensure that you can distinguish between Tags and Strings while iterating
 * over a {@link Bundle}.
 *
 * <p>
 * BUT: This tag can be compared to another Tag or another String using {@link #equals(Object)}
 * and its {@link #hashCode()} returns the saved string's hashcode. So you have to use "instanceof" for differentiating
 * between Tags and Strings.
 *
 * @author Dominic Parga Cacheiro
 */
public class Tag {

    private String label;

    /**
     * Default constructor.
     *
     * @param label This label is used in {@link #equals(Object)} and the Tag's hashcode method returns label.hashCode()
     */
    Tag(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Tag || obj instanceof String)) return false;

        String other;
        if (obj instanceof Tag)
            other = ((Tag) obj).label;
        else
            other = (String) obj;

        return label.equals(other);
    }

    @Override
    public int hashCode() {
        return label.hashCode();
    }
}
