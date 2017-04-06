package microtrafficsim.core.serialization.kryo.impl;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import microtrafficsim.core.serialization.Version;


public class VersionSerializer extends Serializer<Version> {

    @Override
    public void write(Kryo kryo, Output output, Version object) {
        output.writeInt(object.major);
        output.writeInt(object.minor);
        output.writeInt(object.patch);
    }

    @Override
    public Version read(Kryo kryo, Input input, Class<Version> type) {
        return new Version(
                input.readInt(),
                input.readInt(),
                input.readInt()
        );
    }
}

