package microtrafficsim.utils.datacollection;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

/**
 * TODO
 *
 * @author Dominic Parga Cacheiro
 */
public class ConcurrentHashDataCollector implements DataCollector<Object> {

    private HashMap<Tag, Data> dataMap;
    private final ReentrantLock lock;

    public ConcurrentHashDataCollector() {
        dataMap = new HashMap<>();
        lock = new ReentrantLock(true);
    }

    @Override
    public int size(String tag) {
        Tag t = new Tag(tag);
        int size = 0;
        lock.lock();
        if (dataMap.containsKey(t))
            size = dataMap.get(t).size();
        lock.unlock();
        return size;
    }

    @Override
    public boolean addBundle(String tag) {
        lock.lock();
        if (dataMap.containsKey(tag)) {
            lock.unlock();
            return false;
        }
        dataMap.put(new Tag(tag), new Bundle());
        lock.unlock();
        return true;
    }

    @Override
    public void put(String tag, Object obj) {
        lock.lock();
        Data data = dataMap.get(new Tag(tag));
        if (data == null) {
            data = new Data(obj);
            dataMap.put(new Tag(tag), data);
        }
        data.set(obj);
        lock.unlock();
    }

    @Override
    public Iterator<Object> iterator() {
        lock.lock();
        return new Iterator<Object>() {

            private Iterator<Tag> tags = dataMap.keySet().iterator();
            private Iterator<Data> bundle = null;
            private boolean finished = false;

            @Override
            public boolean hasNext() {

                if (!finished) {
                    boolean hasNext = false;
                    if (bundle == null)
                        hasNext = tags.hasNext();
                    else
                        hasNext = tags.hasNext() || bundle.hasNext();

                    if (!hasNext) {
                        finished = true;
                        lock.unlock();
                    }

                    return hasNext;
                }
                return false;
            }

            @Override
            public Object next() {

                if (!finished) {
                    if (bundle != null)
                        if (bundle.hasNext())
                            return bundle.next();

                    Tag t = tags.next();
                    Data d = dataMap.get(t);
                    if (d instanceof Bundle) {
                        bundle = ((Bundle) d).iterator();
                        return t;
                    } else {
                        bundle = null;
                        return d;
                    }
                }

                return null;
            }
        };
    }
}
