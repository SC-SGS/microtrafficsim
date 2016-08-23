package microtrafficsim.core.vis.map.tiles;

import microtrafficsim.core.map.tiles.TileId;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.math.Mat4f;


/**
 * Renderable tile.
 *
 * @author Maximilian Luz
 */
public interface Tile {

    /**
     * Returns the id of the tile.
     *
     * @return the id of the tile.
     */
    TileId getId();

    /**
     * Renders the tile.
     *
     * @param context the context on which the tile should be displayed.
     */
    void display(RenderContext context);

    /**
     * Returns the transformation-matrix for this tile.
     *
     * @return the transformation-matrix for this tile.
     */
    Mat4f getTransformation();

    /**
     * Sets the transformation-matrix for this tile.
     *
     * @param m the new transformation-matrix.
     */
    void setTransformation(Mat4f m);
}
