package microtrafficsim.core.serialization.kryo.impl;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import microtrafficsim.core.logic.streetgraph.GraphGUID;
import microtrafficsim.core.map.Bounds;


public class GraphGUIDSerializer extends Serializer<GraphGUID> {

    @Override
    public void write(Kryo kryo, Output output, GraphGUID object) {
        kryo.writeObject(output, object.getBounds());
        kryo.writeObject(output, object.getNodeHash());
        kryo.writeObject(output, object.getEdgeHash());
    }

    @Override
    public GraphGUID read(Kryo kryo, Input input, Class<GraphGUID> type) {
        Bounds bounds = kryo.readObject(input, Bounds.class);
        int nodes = kryo.readObject(input, Integer.class);
        int edges = kryo.readObject(input, Integer.class);

        return new GraphGUID(bounds, nodes, edges);
    }
}
