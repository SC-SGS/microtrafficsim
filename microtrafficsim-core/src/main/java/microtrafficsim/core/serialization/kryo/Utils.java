package microtrafficsim.core.serialization.kryo;


import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import microtrafficsim.core.map.Coordinate;

public class Utils {
    private Utils() {}

    public static void writeCoordinates(Output output, Coordinate[] coordinates) {
        output.writeInt(coordinates.length);

        for (Coordinate c : coordinates) {
            output.writeDouble(c.lat);
            output.writeDouble(c.lon);
        }
    }

    public static Coordinate[] readCoordinates(Input input) {
        int len = input.readInt();
        Coordinate[] coordinates = new Coordinate[len];

        for (int i = 0; i < len; i++) {
            coordinates[i] = new Coordinate(input.readDouble(), input.readDouble());
        }

        return coordinates;
    }
}
