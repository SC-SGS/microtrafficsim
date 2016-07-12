package microtrafficsim.utils.datacollection;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * TODO
 *
 * @author Dominic Parga Cacheiro
 */
public class Bundle extends Data implements Iterable<Data> {

    private ArrayList<Data> list;

    Bundle() {
        super(new ArrayList<Data>());
        list = (ArrayList<Data>) get();
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    Data set(Object obj) {
        if (obj instanceof Data)
            list.add((Data) obj);
        else
            list.add(new Data(obj));

        return this;
    }

    @Override
    public Iterator<Data> iterator() {
        return list.iterator();
    }
}
