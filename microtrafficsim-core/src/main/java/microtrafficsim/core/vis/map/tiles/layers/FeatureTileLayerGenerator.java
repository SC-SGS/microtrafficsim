package microtrafficsim.core.vis.map.tiles.layers;

import microtrafficsim.core.map.FeaturePrimitive;
import microtrafficsim.core.map.features.Street;
import microtrafficsim.core.map.tiles.TileId;
import microtrafficsim.core.map.tiles.TileRect;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.map.tiles.mesh.FeatureMeshGenerator;
import microtrafficsim.core.vis.map.tiles.mesh.StreetMeshGenerator;
import microtrafficsim.core.vis.mesh.ManagedMesh;
import microtrafficsim.core.vis.mesh.Mesh;
import microtrafficsim.core.vis.mesh.MeshPool;
import microtrafficsim.core.vis.mesh.style.FeatureStyle;
import microtrafficsim.math.Mat4f;
import microtrafficsim.math.Rect2d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;


/**
 * Generator for feature tile layers.
 *
 * @author Maximilian Luz
 */
public class FeatureTileLayerGenerator implements TileLayerGenerator {
    private static final Logger logger = LoggerFactory.getLogger(FeatureTileLayerGenerator.class);

    private static final Rect2d MESH_TARGET = new Rect2d(-1.0, -1.0, 1.0, 1.0);

    private MeshPool<FeatureMeshGenerator.FeatureMeshKey> pool;
    private HashSet<FeatureMeshGenerator.FeatureMeshKey>  loading;
    private HashMap<Class<? extends FeaturePrimitive>, FeatureMeshGenerator> generators;


    /**
     * Constructs a new {@code FeatureTileLayerGenerator} and default-initializes it.
     * This call is equivalent to
     * {@link FeatureTileLayerGenerator#FeatureTileLayerGenerator(boolean)
     * FeatureTileLayerGenerator(true)}
     */
    public FeatureTileLayerGenerator() {
        this(true);
    }

    /**
     * Constructs a new {@code FeatureTileLayerGenerator} and default-initializes it, if specified.
     * Default-initialization adds default generators.
     *
     * @param defaultInit set to {@code true} if the generator should be default initialized.
     */
    public FeatureTileLayerGenerator(boolean defaultInit) {
        this.pool       = new MeshPool<>();
        this.loading    = new HashSet<>();
        this.generators = new HashMap<>();

        if (defaultInit) { generators.put(Street.class, new StreetMeshGenerator()); }
    }


    @Override
    public FeatureTileLayer generate(RenderContext context, Layer layer, TileId tile, Rect2d target)
            throws InterruptedException {
        if (!(layer.getSource() instanceof FeatureTileLayerSource)) return null;

        FeatureTileLayerSource src = (FeatureTileLayerSource) layer.getSource();
        if (!src.isAvailable()) return null;

        FeatureMeshGenerator generator = generators.get(src.getFeatureType());
        if (generator == null) return null;

        FeatureMeshGenerator.FeatureMeshKey key  = generator.getKey(context, src, tile, MESH_TARGET);
        ManagedMesh                         mesh = null;

        synchronized (this) {
            while (mesh == null) {
                mesh = pool.get(key);

                if (mesh != null) {
                    mesh = mesh.require();
                } else {
                    // if mesh is already being loaded, wait
                    if (loading.contains(key)) {
                        try {
                            while (loading.contains(key))
                                this.wait();
                        } catch (InterruptedException e) { return null; }
                    } else {
                        loading.add(key);
                        break;
                    }
                }
            }
        }

        TileRect actual = generator.getFeatureBounds(src, tile);

        if (mesh == null) {
            logger.debug("generating mesh for tile {" + actual.xmin + "-" + actual.xmax + "/" + actual.ymin + "-"
                         + actual.ymax + "/" + actual.zoom + "}, feature '" + src.getFeatureName() + "'");

            Mesh m;
            try {
                m = generator.generate(context, src, tile, MESH_TARGET);
            } catch (InterruptedException e) {
                synchronized (this) {
                    loading.remove(key);
                    this.notifyAll();
                    throw new InterruptedException();
                }
            }

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

        // translate from MESH_TARGET via meshbounds to tilebounds to targe
        Rect2d meshbounds = src.getTilingScheme().getBounds(actual);
        Rect2d tilebounds = src.getTilingScheme().getBounds(tile);

        double sxMeshToBounds = (meshbounds.xmax - meshbounds.xmin) / (MESH_TARGET.xmax - MESH_TARGET.xmin);
        double syMeshToBounds = (meshbounds.ymax - meshbounds.ymin) / (MESH_TARGET.ymax - MESH_TARGET.ymin);
        double sxBoundsToTile = (target.xmax - target.xmin) / (tilebounds.xmax - tilebounds.xmin);
        double syBoundsToTile = (target.ymax - target.ymin) / (tilebounds.ymax - tilebounds.ymin);

        double sx = sxMeshToBounds * sxBoundsToTile;
        double sy = syMeshToBounds * syBoundsToTile;
        float  sz = 0.1f;    // handle layer valuse from -10 to 10

        double tx = meshbounds.xmin - tilebounds.xmin;
        double ty = meshbounds.ymin - tilebounds.ymin;

        Mat4f transform = new Mat4f(
                (float) (sx), 0.f, 0.f, (float) (target.xmin + sxBoundsToTile * tx - MESH_TARGET.xmin * sx),
                0.f, (float) (sy), 0.f, (float) (target.ymin + syBoundsToTile * ty - MESH_TARGET.ymin * sy),
                0.f, 0.f, sz, 0.f, 0.f, 0.f, 0.f, 1.f);

        FeatureStyle style = new FeatureStyle(src.getStyle());
        return new FeatureTileLayer(tile, layer, transform, mesh, style);
    }
}
