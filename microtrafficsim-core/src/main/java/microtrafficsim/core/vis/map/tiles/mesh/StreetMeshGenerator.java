package microtrafficsim.core.vis.map.tiles.mesh;

import microtrafficsim.core.map.tiles.TileId;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.map.tiles.layers.FeatureTileLayerSource;
import microtrafficsim.core.vis.mesh.Mesh;
import microtrafficsim.core.vis.mesh.MeshBucket;
import microtrafficsim.core.vis.mesh.style.Style;
import microtrafficsim.core.vis.opengl.shader.ShaderProgram;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexArrayObject;
import microtrafficsim.core.vis.opengl.utils.LifeTimeObserver;
import microtrafficsim.math.Rect2d;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class StreetMeshGenerator implements FeatureMeshGenerator {

    @Override
    public FeatureMeshKey getKey(RenderContext context, FeatureTileLayerSource source, TileId tile, Rect2d target) {
        return new StreetMeshKey(
                context,
                tile,
                target,
                source.getFeatureProvider(),
                source.getFeatureName(),
                source.getTilingScheme(),
                source.getRevision(),
                getPropAdjacency(source.getStyle()),
                getPropJoinsWhenPossible(source.getStyle())
        );
    }

    @Override
    public Mesh generate(RenderContext context, FeatureTileLayerSource source, TileId tile, Rect2d target) {
        // NOTE: use tile-relative positions
        // TODO

        return new Mesh() {
            @Override public State getState() { return State.INITIALIZED; }
            @Override public boolean initialize(RenderContext context, boolean force) { return true; }
            @Override public boolean dispose(RenderContext context) { return true; }
            @Override public boolean load(RenderContext context, boolean force) { return true; }
            @Override public void display(RenderContext context, ShaderProgram shader) {}
            @Override public void display(RenderContext context, VertexArrayObject vao) {}
            @Override public List<? extends MeshBucket> getBuckets() { return new ArrayList<>(); }
            @Override public void addLifeTimeObserver(LifeTimeObserver<Mesh> lto) {}
            @Override public void removeLifeTimeObserver(LifeTimeObserver<Mesh> lto) {}
            @Override public Set<LifeTimeObserver<Mesh>> getLifeTimeObservers() { return new HashSet<>(); }

            @Override
            public VertexArrayObject createVAO(RenderContext context, ShaderProgram program) {
                VertexArrayObject vao = VertexArrayObject.create(context.getDrawable().getGL().getGL2ES3());
                return vao;
            }
        };
    }


    private static boolean getPropAdjacency(Style style) {
        return style.getProperty("adjacency_primitives", false);
    }

    private static boolean getPropJoinsWhenPossible(Style style) {
        return style.getProperty("use_joins_when_possible", false);
    }
}
