package microtrafficsim.core.serialization;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.base.TileGridInfo;
import microtrafficsim.core.exfmt.ecs.components.FeatureComponent;
import microtrafficsim.core.exfmt.ecs.components.StreetComponent;
import microtrafficsim.core.exfmt.ecs.components.TileGridComponent;
import microtrafficsim.core.exfmt.ecs.entities.LineEntity;
import microtrafficsim.core.exfmt.ecs.entities.PointEntity;
import microtrafficsim.core.exfmt.ecs.entities.PolygonEntity;
import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.map.FeatureDescriptor;
import microtrafficsim.core.map.tiles.QuadTreeTilingScheme;
import microtrafficsim.core.map.tiles.TileRect;
import microtrafficsim.core.serialization.kryo.impl.*;
import microtrafficsim.core.serialization.kryo.impl.exfmt.FeatureDescriptorSerializer;
import microtrafficsim.core.serialization.kryo.impl.exfmt.TileGridSetSerializer;
import microtrafficsim.core.serialization.kryo.impl.exfmt.components.FeatureComponentSerializer;
import microtrafficsim.core.serialization.kryo.impl.exfmt.components.StreetComponentSerializer;
import microtrafficsim.core.serialization.kryo.impl.exfmt.components.TileGridComponentSerializer;
import microtrafficsim.core.serialization.kryo.impl.exfmt.entities.LineEntitySerializer;
import microtrafficsim.core.serialization.kryo.impl.exfmt.entities.PointEntitySerializer;
import microtrafficsim.core.serialization.kryo.impl.exfmt.entities.PolygonEntitySerializer;
import microtrafficsim.core.vis.map.projections.MercatorProjection;
import microtrafficsim.core.vis.map.projections.PlateCarreeProjection;
import microtrafficsim.math.Rect2d;
import microtrafficsim.utils.Version;
import microtrafficsim.utils.collections.Grid;

import java.io.*;


public class ExchangeFormatSerializer {
    public static final Version VERSION = new Version(0, 1, 0);

    private final Kryo kryo;


    public static ExchangeFormatSerializer create() {
        return new ExchangeFormatSerializer(createKryo());
    }

    public ExchangeFormatSerializer(Kryo kryo) {
        this.kryo = kryo;
    }


    public void write(File file, Container container) throws IOException {
        try (OutputStream os = new FileOutputStream(file)) {
            write(os, container);
        }
    }

    public void write(OutputStream os, Container container) throws IOException {
        Output out = new Output(os);
        kryo.writeClassAndObject(out, container);
        out.flush();
    }


    public Container read(File file) throws IOException {
        try (InputStream is = new FileInputStream(file)) {
            return read(is);
        }
    }

    public Container read(InputStream is) throws IOException {
        return (Container) kryo.readClassAndObject(new Input(is));
    }


    private static Kryo createKryo() {
        Kryo kryo = new Kryo();

        // NOTE: any change of the following statements breaks file-compatibility
        //       with the exception of appending new serializers

        // misc
        kryo.register(Grid.class, new GridSerializer());

        kryo.register(Version.class, new VersionSerializer());
        kryo.register(Container.class);

        // exchange-format
        kryo.register(TileGridInfo.Grid.class, new TileGridSetSerializer.Grid());

        kryo.register(PointEntity.class, new PointEntitySerializer());
        kryo.register(LineEntity.class, new LineEntitySerializer());
        kryo.register(PolygonEntity.class, new PolygonEntitySerializer());

        kryo.register(StreetComponent.class, new StreetComponentSerializer());
        kryo.register(FeatureComponent.class, new FeatureComponentSerializer());
        kryo.register(TileGridComponent.class, new TileGridComponentSerializer());
        kryo.register(TileGridComponent.Entry.class, new TileGridComponentSerializer.Entry());

        // map stuff
        kryo.register(FeatureDescriptor.class, new FeatureDescriptorSerializer());

        kryo.register(Coordinate.class, new CoordinateSerializer());
        kryo.register(Bounds.class, new BoundsSerializer());
        kryo.register(Rect2d.class, new Rect2dSerializer());
        kryo.register(TileRect.class, new TileRectSerializer());

        kryo.register(PlateCarreeProjection.class);
        kryo.register(MercatorProjection.class);

        kryo.register(QuadTreeTilingScheme.class, new QuadTreeTilingSchemeSerializer());

        return kryo;
    }
}
