package microtrafficsim.core.serialization.kryo.impl;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import microtrafficsim.utils.collections.Grid;


public class GridSerializer extends Serializer<Grid> {

    @Override
    public void write(Kryo kryo, Output output, Grid object) {
        kryo.writeObject(output, object.getSizeX());
        kryo.writeObject(output, object.getSizeY());

        for (int y = 0; y < object.getSizeY(); y++) {
            for (int x = 0; x < object.getSizeX(); x++) {
                kryo.writeClassAndObject(output, object.get(x, y));
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Grid read(Kryo kryo, Input input, Class<Grid> type) {
        int nx = kryo.readObject(input, Integer.class);
        int ny = kryo.readObject(input, Integer.class);

        Grid grid = new Grid(nx, ny);
        for (int y = 0; y < ny; y++) {
            for (int x = 0; x < nx; x++) {
                grid.set(x, y, kryo.readClassAndObject(input));
            }
        }

        return grid;
    }
}
