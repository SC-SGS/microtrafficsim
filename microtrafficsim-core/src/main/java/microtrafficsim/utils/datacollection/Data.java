package microtrafficsim.utils.datacollection;


/**
 * TODO
 *
 * @author Dominic Parga Cacheiro
 */
public class Data {

    private Object obj;

    Data(Object obj) {
        if (obj instanceof Data)
            this.obj = ((Data) obj).obj;
        else
            this.obj = obj;
    }

    public int size() {
        return 1;
    }

    Object get() {
        return obj;
    }

    Data set(Object obj) {
        if (obj instanceof Data)
            this.obj = ((Data) obj).obj;
        else
            this.obj = obj;
        return this;
    }

    @Override
    public String toString() {
        return obj.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Data)) return this.obj.equals(obj);

        Data other = (Data) obj;
        return this.obj.equals(other.obj);
    }

    @Override
    public int hashCode() {
        return obj.hashCode();
    }
}
