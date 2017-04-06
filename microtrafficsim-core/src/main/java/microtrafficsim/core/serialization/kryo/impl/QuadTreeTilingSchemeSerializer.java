package microtrafficsim.core.serialization.kryo.impl;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import microtrafficsim.core.map.tiles.QuadTreeTilingScheme;
import microtrafficsim.core.vis.map.projections.Projection;


public class QuadTreeTilingSchemeSerializer extends Serializer<QuadTreeTilingScheme> {

    @Override
    public void write(Kryo kryo, Output output, QuadTreeTilingScheme object) {
        kryo.writeClassAndObject(output, object.getProjection());
        output.writeInt(object.getMinimumZoomLevel());
        output.writeInt(object.getMaximumZoomLevel());
    }

    @Override
    public QuadTreeTilingScheme read(Kryo kryo, Input input, Class<QuadTreeTilingScheme> type) {
        return new QuadTreeTilingScheme(
                (Projection) kryo.readClassAndObject(input),
                input.readInt(),
                input.readInt()
        );
    }
}
