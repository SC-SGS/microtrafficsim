package microtrafficsim.core.exfmt.ecs.components;

import microtrafficsim.core.exfmt.ecs.Component;
import microtrafficsim.core.exfmt.ecs.Entity;


public class StreetComponent extends Component {

    private double layer;
    private double length;
    private double[] distances;

    public StreetComponent(Entity entity, double layer, double length, double[] distances) {
        super(entity);
        this.layer = layer;
        this.length = length;
        this.distances = distances;
    }


    public double getLayer() {
        return layer;
    }

    public void setLayer(double layer) {
        this.layer = layer;
    }


    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }


    public double[] getDistances() {
        return distances;
    }

    public void setDistances(double[] distances) {
        this.distances = distances;
    }
}
