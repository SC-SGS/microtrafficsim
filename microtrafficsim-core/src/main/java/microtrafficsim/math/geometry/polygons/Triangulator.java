package microtrafficsim.math.geometry.polygons;

import microtrafficsim.math.Vec2d;

import java.util.ArrayList;


/**
 * Stateless facade for polygon triangulation algorithms.
 *
 * @author Maximilian Luz
 */
public interface Triangulator {

    /**
     * Triangulates the given polygon.
     *
     * @param polygon the polygon to triangulate.
     * @return the triangulated polygon as {@code Result}, may return {@code null} if the triangulation algorithm
     *         failed. If the polygon to be triangulated takes up no space, the result may be empty (in either
     *         {@code indices} or both {@code indices} and {@code vertices}) to avoid zero-area triangles.
     */
    Result triangulate(Polygon polygon);

    /**
     * Class to store the result of a (successful) polygon triangulation. The triangles of a triangulated polygon are
     * stored as a list of vertices and sequence of index triples (in {@code indices}, thus
     * {@code indices.size() % 3 == 0}) pointing into said list of vertices. Each stored index-triple represents one
     * generated triangle.
     */
    class Result {
        public ArrayList<Vec2d> vertices;
        public ArrayList<Integer> indices;

        /**
         * Creates a new result, storing the triangles of the triangulated polygon. For the details on how the
         * triangles are stored, see the documentation of this class.
         *
         * @param vertices the list of vertices of the triangulated polygon.
         * @param indices  the list of indices, representing the triangles as triples.
         */
        public Result(ArrayList<Vec2d> vertices, ArrayList<Integer> indices) {
            this.vertices = vertices;
            this.indices  = indices;
        }
    }
}

