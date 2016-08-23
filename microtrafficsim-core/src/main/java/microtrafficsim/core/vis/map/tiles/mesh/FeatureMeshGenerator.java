package microtrafficsim.core.vis.map.tiles.mesh;

import microtrafficsim.core.map.tiles.TileId;
import microtrafficsim.core.map.tiles.TileRect;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.map.tiles.layers.FeatureTileLayerSource;
import microtrafficsim.core.vis.mesh.Mesh;
import microtrafficsim.math.Rect2d;


/**
 * Generator for feature-based meshes.
 *
 * @author Maximilian Luz
 */
public interface FeatureMeshGenerator {
    interface FeatureMeshKey {}

    /**
     * Creates a key used to uniquely identify a mesh constructed from the given properties.
     *
     * @param context the context for which the mesh should be created.
     * @param source  the source of this mesh.
     * @param tile    the tile for which the mesh should be created.
     * @param target  the target-space to which the mesh should be projected.
     * @return the key generated from the given properties.
     */
    FeatureMeshKey getKey(RenderContext context, FeatureTileLayerSource source, TileId tile, Rect2d target);

    /**
     * Return the bounds in tiles that the given source actually provides for the specified tile in combination with
     * this generator.
     *
     * @param src  the source for which the bounds should be returned.
     * @param tile the tile for which the bounds should be returned.
     * @return the actually provided bounds by this generator in combination with the given source for the given tile.
     */
    TileRect getFeatureBounds(FeatureTileLayerSource src, TileId tile);

    /**
     * Generates a mesh on the given context from the given source for the given tile, projecting it to the provided
     * target rectangle.
     *
     * @param context the context on which the mesh should be created.
     * @param source  the source of the mesh.
     * @param tile    the tile for which the mesh should be generated.
     * @param target  the target-space to which the mesh should be projected.
     * @return the generated mesh.
     * @throws InterruptedException if this operation has been interrupted.
     */
    Mesh generate(RenderContext context, FeatureTileLayerSource source, TileId tile, Rect2d target)
            throws InterruptedException;
}
