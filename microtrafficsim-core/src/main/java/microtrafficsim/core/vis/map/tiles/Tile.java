package microtrafficsim.core.vis.map.tiles;

import microtrafficsim.core.map.tiles.TileId;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.math.Mat4f;


public interface Tile {
    TileId getId();

    void display(RenderContext context);

    Mat4f getTransformation();
    void setTransformation(Mat4f m);
}
