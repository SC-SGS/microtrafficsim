package microtrafficsim.core.vis.mesh.utils;

import java.util.ArrayList;
import java.util.HashMap;


public class VertexSet<Vertex> {
    private HashMap<Vertex, Integer> indexmap;
    private ArrayList<Vertex> vertices;
    private int counter = 0;


    public VertexSet() {
        indexmap = new HashMap<>();
        vertices = new ArrayList<>();
    }

    public VertexSet(int capacity) {
        indexmap = new HashMap<>(capacity);
        vertices = new ArrayList<>(capacity);
    }


    public int add(Vertex vertex) {
        return indexmap.computeIfAbsent(vertex, (k) -> {
            vertices.add(vertex);
            return counter++;
        });
    }

    public ArrayList<Vertex> getVertices() {
        return vertices;
    }

    public int size() {
        return vertices.size();
    }
}
