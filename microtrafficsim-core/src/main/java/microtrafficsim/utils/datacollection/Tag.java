package microtrafficsim.utils.datacollection;

/**
 * TODO
 *
 * @author Dominic Parga Cacheiro
 */
public class Tag {

    private String label;

    Tag(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Tag || obj instanceof String))
            return false;

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
