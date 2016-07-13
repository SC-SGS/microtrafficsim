package microtrafficsim.core.map.layers;


public class LayerDefinition {
    private final String name;
    private int          index;
    private LayerSource  source;

    public LayerDefinition(String name, int index, LayerSource source) {
        this.name   = name;
        this.index  = index;
        this.source = source;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public LayerSource getSource() {
        return source;
    }
}
