package microtrafficsim.core.vis.map.tiles.layers;

import microtrafficsim.core.map.FeaturePrimitive;
import microtrafficsim.core.map.features.Street;
import microtrafficsim.core.map.tiles.TileId;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.map.tiles.mesh.FeatureMeshGenerator;
import microtrafficsim.core.vis.map.tiles.mesh.StreetMeshGenerator;
import microtrafficsim.core.vis.mesh.ManagedMesh;
import microtrafficsim.core.vis.mesh.Mesh;
import microtrafficsim.core.vis.mesh.MeshPool;
import microtrafficsim.core.vis.mesh.style.FeatureStyle;
import microtrafficsim.math.Rect2d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;


public class FeatureTileLayerGenerator implements TileLayerGenerator {
    private static final Logger logger = LoggerFactory.getLogger(FeatureTileLayerGenerator.class);

    private MeshPool<FeatureMeshGenerator.FeatureMeshKey> pool;
    private HashSet<FeatureMeshGenerator.FeatureMeshKey> loading;
    private HashMap<Class<? extends FeaturePrimitive>, FeatureMeshGenerator> generators;


    public FeatureTileLayerGenerator() {
        this(true);
    }

    public FeatureTileLayerGenerator(boolean defaultInit) {
        this.pool = new MeshPool<>();
        this.loading = new HashSet<>();
        this.generators = new HashMap<>();

        if (defaultInit) {
            generators.put(Street.class, new StreetMeshGenerator());
        }
    }


    @Override
    public FeatureTileLayer generate(RenderContext context, Layer layer, TileId tile, Rect2d target) {
        if (!(layer.getSource() instanceof FeatureTileLayerSource)) return null;

        FeatureTileLayerSource src = (FeatureTileLayerSource) layer.getSource();
        if (!src.isAvailable()) return null;

        FeatureMeshGenerator generator = generators.get(src.getFeatureType());
        if (generator == null) return null;

        FeatureMeshGenerator.FeatureMeshKey key = generator.getKey(context, src, tile, target);
        ManagedMesh mesh;

        synchronized (this) {
            mesh = pool.get(key);
			if (mesh != null)
				mesh = mesh.require();

            if (mesh == null) {
                // if mesh is already being loaded, wait
                if (loading.contains(key)) {
                    try {
                        while (loading.contains(key))
                            this.wait();
                    } catch (InterruptedException e) {
                        return null;
                    }

                    mesh = pool.get(key);
                    mesh = mesh.require();

                } else {
                    loading.add(key);
                }
            }
        }

        if (mesh == null) {
            logger.debug("generating mesh for tile {"
                    + tile.x + "/" + tile.y + "/" + tile.z
                    + "}, feature '" + src.getFeatureName() + "'");

            Mesh m = generator.generate(context, src, tile, target);
            if (m == null) {
                synchronized (this) {
                    loading.remove(key);
                    this.notifyAll();
                }
                return null;
            }

            mesh = new ManagedMesh(m);
            synchronized (this) {
                pool.put(key, mesh);
                loading.remove(key);
                this.notifyAll();
            }
        }

        FeatureStyle style = new FeatureStyle(src.getStyle());
        return new FeatureTileLayer(tile, layer, src, mesh, style);
    }
}
