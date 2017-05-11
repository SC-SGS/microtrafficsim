package microtrafficsim.core.serialization.kryo.impl;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.map.area.polygons.TypedPolygonArea;
import microtrafficsim.core.serialization.kryo.Utils;
import microtrafficsim.core.vis.scenario.areas.Area;


public class TypedPolygonAreaSerializer extends Serializer<TypedPolygonArea> {

    @Override
    public void write(Kryo kryo, Output output, TypedPolygonArea object) {
        kryo.writeObject(output, object.getType());
        Utils.writeCoordinates(output, object.getCoordinates());
    }

    @Override
    public TypedPolygonArea read(Kryo kryo, Input input, Class<TypedPolygonArea> t) {
        Area.Type type = kryo.readObject(input, Area.Type.class);
        Coordinate[] coords = Utils.readCoordinates(input);

        return new TypedPolygonArea(coords, type);
    }
}
