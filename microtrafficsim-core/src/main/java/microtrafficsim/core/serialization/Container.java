package microtrafficsim.core.serialization;

import microtrafficsim.core.map.SegmentFeatureProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class Container {
    private Meta meta = new Meta();
    private SegmentFeatureProvider segment;



    public Container setMetaInfo(Meta meta) {
        this.meta = meta;
        return this;
    }

    public Meta getMetaInfo() {
        return meta;
    }


    public Container setVersion(Version version) {
        this.meta.version = version;
        return this;
    }

    public Version getVersion() {
        return meta.version;
    }


    public Container setSegment(SegmentFeatureProvider segment) {
        this.segment = segment;
        return this;
    }

    public SegmentFeatureProvider getSegment() {
        return segment;
    }


    public void write(Serializer serializer, File file) throws IOException {
        serializer.write(file, this);
    }

    public void write(Serializer serializer, OutputStream os) throws IOException {
        serializer.write(os, this);
    }

    public static Container read(Serializer serializer, File file) throws IOException {
        return serializer.read(file);
    }

    public static Container read(Serializer serializer, InputStream is) throws IOException {
        return serializer.read(is);
    }


    public static class Meta {
        private Version version = Serializer.VERSION;
    }
}
