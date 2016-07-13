package microtrafficsim.core.vis.map.segments;


import microtrafficsim.core.map.layers.LayerSource;

public class Layer {

    private final String name;
    private int          index;
    private LayerSource  source;
    private boolean      enabled;


    public Layer(String name, int index, LayerSource source) {
        this.name    = name;
        this.index   = index;
        this.source  = source;
        this.enabled = true;
    }


    public String getName() {
        return this.name;
    }


    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }


    public LayerSource getSource() {
        return source;
    }


    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
