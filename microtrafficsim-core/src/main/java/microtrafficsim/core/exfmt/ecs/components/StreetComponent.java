package microtrafficsim.core.exfmt.ecs.components;

import microtrafficsim.core.exfmt.ecs.Component;
import microtrafficsim.core.exfmt.ecs.Entity;


public class StreetComponent extends Component {

    private double layer;
    private double length;
    private double[] distances;
    private int numLanesFwd;
    private int numLanesBwd;

    public StreetComponent(Entity entity, double layer, double length, double[] distances, int numLanesFwd,
                           int numLanesBwd)
    {
        super(entity);
        this.layer = layer;
        this.length = length;
        this.distances = distances;
        this.numLanesFwd = numLanesFwd;
        this.numLanesBwd = numLanesBwd;
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


    public int getLanesFwd() {
        return numLanesFwd;
    }

    public void setLanesFwd(int numLanesFwd) {
        this.numLanesFwd = numLanesFwd;
    }

    public int getLanesBwd() {
        return numLanesBwd;
    }

    public void setLanesBwd(int numLanesBwd) {
        this.numLanesBwd = numLanesBwd;
    }
}
