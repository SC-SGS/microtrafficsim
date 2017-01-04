package microtrafficsim.math.geometry.polygons;

import microtrafficsim.math.Vec2d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 * Polygon triangulator based on the Bentley-Ottmann sweep-line algorithm to generate a basic stable trapezoidation of
 * the polygon in O(n log n) time.
 *
 * @author Maximilian Luz
 */
public final class SweepLineTriangulator implements Triangulator {
    // TODO: implement epsilon-comparisons
    // TODO: look at http://ect.bell-labs.com/who/hobby/93_2-27.pdf for numerical improvements to Bentley-Ottmann alg.
    // TODO: proper triangulation of trapezoids


    /**
     * Triangulates the given polygon.
     *
     * @param polygon the polygon to triangulate.
     * @return the triangulated polygon as {@code Result}. This method will never return {@code null} as this algorithm
     *         will always produce a result. If the polygon to be triangulated takes up no space, the result will be
     *         empty in both {@code indices} and {@code vertices}) to avoid zero-area triangles.
     */
    @Override
    public Result triangulate(Polygon polygon) {
        SweepLine sweepline = new SweepLine();
        PriorityQueue<Event> queue = createEventQueue(polygon);

        while (!queue.isEmpty()) {
            Event event = queue.poll();
            sweepline.update(event.pos.x);

            if (event instanceof StartEvent) {
                StartEvent e = (StartEvent) event;
                queue.add(new StopEvent(e.edge));
                sweepline.add(e.edge);

                if (e.edge.below != null) addEventIfIntersect(queue, e.edge.below, e.edge);
                if (e.edge.above != null) addEventIfIntersect(queue, e.edge, e.edge.above);

            } else if (event instanceof StopEvent) {
                StopEvent e = (StopEvent) event;

                Edge a = e.edge.above;
                Edge b = e.edge.below;

                sweepline.remove(e.edge);

                if (a != null && b != null) addEventIfIntersect(queue, e.pos, b, a);

            } else if (event instanceof IntersectionEvent) {
                IntersectionEvent e = (IntersectionEvent) event;
                if (e.e1.above != e.e2) continue;   // if true, we already handled this intersection

                sweepline.swap(e.e1, e.e2);
                assert e.e2.above == e.e1 && e.e1.below == e.e2: "method not doing what it should";

                if (e.e2.below != null) addEventIfIntersect(queue, e.pos, e.e2.below, e.e2);
                if (e.e1.above != null) addEventIfIntersect(queue, e.pos, e.e1, e.e1.above);
            }
        }

        return triangulate(sweepline.finish());
    }


    /**
     * Comparator for a total vertex ordering, ordered first by x, then by y.
     */
    private static final Comparator<Vec2d> CMP_VEC2D = (a, b) -> {
        int cmp = Double.compare(a.x, b.x);
        if (cmp != 0) return cmp;

        return Double.compare(a.y, b.y);
    };

    /**
     * Comparator for a total ordering of Events, ordered first by the point of incident (as in {@code CMP_VERTEX}),
     * and afterwards ordered by the type of the event, according to {@code Event.typeOrderId(...)} (i.e.
     * {@code StopEvent < IntersectionEvent < StartEvent}).
     */
    private static final Comparator<Event> CMP_EVENT = (a, b) -> {
        int cmp = Double.compare(a.pos.x, b.pos.x);
        if (cmp != 0) return cmp;

        cmp = Double.compare(a.pos.y, b.pos.y);
        if (cmp != 0) return cmp;

        return Integer.compare(Event.typeOrderId(a), Event.typeOrderId(b));
    };


    /**
     * Creates a new priority queue containing all start-events of the given polygon.
     *
     * @param polygon the polygon for which the event queue should be created.
     * @return the created event queue.
     */
    private static PriorityQueue<Event> createEventQueue(Polygon polygon) {
        PriorityQueue<Event> queue = new PriorityQueue<>(CMP_EVENT);
        addToEventQueue(queue, polygon.outline);

        for (Vec2d[] island : polygon.islands)
            addToEventQueue(queue, island);

        return queue;
    }

    /**
     * Adds the {@code StartEvent}s of the given polygon-outline to the given queue.
     *
     * @param queue   the queue to which the start events should be added.
     * @param outline the outline from which the start events should be generated.
     */
    private static void addToEventQueue(PriorityQueue<Event> queue, Vec2d[] outline) {
        assert outline.length > 0: "Contract violation: zero-size outline.";

        Vec2d a = new Vec2d(outline[outline.length - 1]);
        for (int i = 0; i < outline.length; i++) {
            Vec2d b = new Vec2d(outline[i]);

            if (a.x != b.x || a.y != b.y) {
                queue.add(new StartEvent(new Edge(a, b)));
                a = b;
            }
        }
    }


    /**
     * Add a new {@code IntersectionEvent} to the given queue, if the given edges intersect and this intersection is
     * after the current position in the sweep line.
     *
     * @param queue   the queue to which the intersection event should be added.
     * @param current the current position in the sweep-line.
     * @param a       the first edge, has to be below the second edge ({@code b}).
     * @param b       the second edge, has to be above the first edge ({@code a}).
     */
    private static void addEventIfIntersect(PriorityQueue<Event> queue, Vec2d current, Edge a, Edge b) {
        assert a.above == b && b.below == a: "Contract violation: edges a and b are no neighbors.";

        Vec2d ip = Edge.intersect(a, b);
        if (ip != null && (ip.x > current.x || (ip.x == current.x && ip.y >= current.y)))
            queue.add(new IntersectionEvent(a, b, ip));
    }

    /**
     * Add a new {@code IntersectionEvent} to the given queue, if the given edges intersect.
     *
     * @param queue   the queue to which the intersection event should be added.
     * @param a       the first edge, has to be below the second edge ({@code b}).
     * @param b       the second edge, has to be above the first edge ({@code a}).
     */
    private static void addEventIfIntersect(PriorityQueue<Event> queue, Edge a, Edge b) {
        assert a.above == b && b.below == a: "Contract violation: edges a and b are no neighbors.";

        Vec2d ip = Edge.intersect(a, b);
        if (ip != null)
            queue.add(new IntersectionEvent(a, b, ip));
    }


    /**
     * Triangulates the given trapezoidation.
     *
     * @param traps the list of trapezoids representing the trapezoidation of a polygon.
     * @return the triangulation of the given trapezoidation.
     */
    private static Result triangulate(ArrayList<Trapezoid> traps) {
        // TODO: implement proper triangulation

        return dumpTrapsToResult(traps);
    }

    /**
     * Triangulate the given trapezoid, and add it to the vertex and index list.
     *
     * @param t        the trapezoid to triangulate.
     * @param vertices the list of vertices, to which the result will be added.
     * @param indices  the list of indices, to which the result will be appended
     * @param indexmap a map, mapping all already added vertices to their indices in the vertex list.
     */
    private static void triangulate(Trapezoid t, ArrayList<Vec2d> vertices, ArrayList<Integer> indices,
                                    HashMap<Vec2d, Integer> indexmap) {
        Vec2d tl = new Vec2d(t.left, Edge.y(t.top, t.left));
        Vec2d bl = new Vec2d(t.left, Edge.y(t.bot, t.left));
        Vec2d tr = new Vec2d(t.right, Edge.y(t.top, t.right));
        Vec2d br = new Vec2d(t.right, Edge.y(t.bot, t.right));

        if (!tl.equals(tr) && !tr.equals(br)) {
            addVertex(tl, vertices, indices, indexmap);
            addVertex(br, vertices, indices, indexmap);
            addVertex(tr, vertices, indices, indexmap);
        }

        if (!tl.equals(bl) && !bl.equals(br)) {
            addVertex(tl, vertices, indices, indexmap);
            addVertex(bl, vertices, indices, indexmap);
            addVertex(br, vertices, indices, indexmap);
        }
    }

    /**
     * Add the given vertex to the vertex and index list.
     *
     * @param v        the vertex to add.
     * @param vertices the list of vertices, to which the vertex will be added, if it is not already contained in it.
     * @param indices  the list of indices, to which the index of the given vertex will be added.
     * @param indexmap a map, mapping all already added vertices to their indices in the vertex list.
     */
    private static void addVertex(Vec2d v, ArrayList<Vec2d> vertices, ArrayList<Integer> indices,
                                  HashMap<Vec2d, Integer> indexmap) {

        Integer index = indexmap.get(v);
        if (index == null) {
            index = vertices.size();
            indexmap.put(v, index);
            vertices.add(v);
        }
        indices.add(index);
    }

    /**
     * Trivially triangulate the given trapezoidation by simply dividing the trapezoids into (at most) two triangles.
     *
     * @param traps the list of trapezoids representing the trapezoidation to be triangulated.
     * @return the result of the triangulation.
     */
    private static Result dumpTrapsToResult(ArrayList<Trapezoid> traps) {
        Result r = new Result(new ArrayList<>(), new ArrayList<>());
        HashMap<Vec2d, Integer> indexmap = new HashMap<>();

        for (Trapezoid t : traps)
            triangulate(t, r.vertices, r.indices, indexmap);

        return r;
    }


    /**
     * Vertically aligned trapezoid, bound by left and right x-values and top and bottom edges.
     */
    private static final class Trapezoid {
        Edge top, bot;
        double left, right;
    }

    /**
     * Edge of a polygon, including state for its position in the sweep-line.
     */
    private static final class Edge {
        Vec2d a, b;                     // original vertices of the edge
        Vec2d left, right;              // ordered vertices of the edge
        int dir;                        // edge direction, 1 if right to left, -1 if left to right, 0 otherwise

        boolean isnew;                  // flag indicating that this edge was newly added to the sweep-line

        Edge trapClosing;               // edge closing the trapezoid started with this edge
        double trapStart;               // x-value where the trapezoid started with this edge ends

        Edge above, below;              // neighbor-edges above and below in the sweep-line
        Edge parent, cleft, cright;     // tree nodes for sweep-line

        /**
         * Creates a new Edge from the given vertices. Initializes all vertices and the edge direction.
         *
         * @param a the start vertex of the edge.
         * @param b the end vertex of the edge.
         */
        Edge(Vec2d a, Vec2d b) {
            this.a = a;
            this.b = b;

            if (CMP_VEC2D.compare(a, b) <= 0) {
                this.left  = a;
                this.right = b;
            } else {
                this.left  = b;
                this.right = a;
            }

            dir = Double.compare(b.x, a.x);
        }

        /**
         * Calculates the y-value of the line specified by the given edge for the given x value. Does not check if the
         * given x value is inside the edge segment.
         *
         * @param e the edge specifying the line for which the y-value should be calculated.
         * @param x the x-value for which the y-value should be calculated.
         * @return the y-value for the given x-value of the line specified by the given edge. Returns positive or
         *         negative infinity if the line is vertical, with the sign indicating the direction.
         */
        static double y(Edge e, double x) {
            if (e.left.x == x)  return e.left.y;
            if (e.right.x == x) return e.right.y;

            double d = (e.right.x - e.left.x);
            if (d == 0) {
                if (e.left.y > e.right.y)
                    return Double.NEGATIVE_INFINITY;
                else
                    return Double.POSITIVE_INFINITY;
            }

            return e.left.y + (x - e.left.x) * (e.right.y - e.left.y) / d;
        }

        /**
         * Calculates the intersection-point of the two given edges.
         *
         * @param a the first edge.
         * @param b the second edge.
         * @return the point of intersection of {@code a} and {@code b} or {@code null} if the edges are collinear or
         *         parallel, or the intersection is not inside the segments.
         */
        static Vec2d intersect(Edge a, Edge b) {
            // TODO: wikipedia suggests rotation to improve numerical stability when lines are almost parallel

            double adx = a.left.x - a.right.x;
            double ady = a.left.y - a.right.y;
            double bdx = b.left.x - b.right.x;
            double bdy = b.left.y - b.right.y;

            double d = adx * bdy - ady * bdx;
            if (d == 0.0)
                return null;

            // test if intersection is in segment
            double abdx = b.left.x - a.left.x;
            double abdy = b.left.y - a.left.y;

            double s = adx * abdy - abdx * ady;
            if (Math.signum(d) * s <= 0 || Math.signum(d) * s >= Math.abs(d))
                return null;

            double t = abdy * bdx - abdx * bdy;
            if (Math.signum(d) * t <= 0 || Math.signum(d) * t >= Math.abs(d))
                return null;

            // calculate intersection point
            double det1 = a.left.x * a.right.y - a.right.x * a.left.y;
            double det2 = b.left.x * b.right.y - b.right.x * b.left.y;

            return new Vec2d((det1 * bdx - det2 * adx) / d, (det1 * bdy - det2 * ady) / d);
        }

        /**
         * Checks if the lines represented by the given edges are collinear.
         *
         * @param a the first edge.
         * @param b the second edge.
         * @return {@code true} if the lines represented by the given edges are collinear.
         */
        static boolean lcollinear(Edge a, Edge b) {
            double adx = a.left.x - a.right.x;
            double ady = a.left.y - a.right.y;
            double bdx = b.left.x - b.right.x;
            double bdy = b.left.y - b.right.y;

            double d = adx * bdy - ady * bdx;
            return !(d != 0.0) && (b.left.x - a.right.x) * ady == (b.left.y - a.right.y) * adx;
        }
    }


    /**
     * Base-class for sweep-line events.
     */
    private static abstract class Event {
        Vec2d pos;      // position of the event

        /**
         * Returns an id of the given event, based on its actual type, which can be used for ordering.
         *
         * @param e the event for which the id should be returned.
         * @return the type-/order-id of the given event.
         */
        static int typeOrderId(Event e) {
            if (e instanceof StopEvent)
                return 1;
            if (e instanceof IntersectionEvent)
                return 2;
            if (e instanceof StartEvent)
                return 3;

            return 0;
        }
    }

    /**
     * Start-event, indicating the start of a new edge.
     */
    private static final class StartEvent extends Event {
        Edge edge;

        StartEvent(Edge edge) {
            this.edge = edge;
            this.pos = edge.left;
        }
    }

    /**
     * Stop event, indicating the end of an edge.
     */
    private static final class StopEvent extends Event {
        Edge edge;

        StopEvent(Edge edge) {
            this.edge = edge;
            this.pos = edge.right;
        }
    }

    /**
     * Intersection event, indicating an intersection between two edges.
     */
    private static final class IntersectionEvent extends Event {
        Edge e1, e2;        // NOTE: expect e1.y < e2.y before intersection

        IntersectionEvent(Edge e1, Edge e2, Vec2d ip) {
            this.e1 = e1;
            this.e2 = e2;
            this.pos = ip;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof IntersectionEvent))
                return false;

            IntersectionEvent other = (IntersectionEvent) obj;

            return this.e1 == other.e1 && this.e2 == other.e2;
        }
    }


    /**
     * Vertical sweep-line based on a tree, used in the triangulation algorithm.
     */
    private static final class SweepLine {
        // TODO: implement rb-tree

        private final Random rnd = new Random();

        private Edge head = null;
        private double x = Double.NEGATIVE_INFINITY;

        ArrayList<Edge> stopped = new ArrayList<>();
        ArrayList<Trapezoid> result = new ArrayList<>();


        /**
         * Insert the given edge to this sweep-line.
         *
         * @param edge the edge to add.
         */
        void add(Edge edge) {
            assert edge != null: "Contract violation: this set may not contain null-objects.";

            edge.isnew = true;

            // add to tree
            if (head != null) {
                Edge node = head;
                boolean left;

                // find insertion point
                while (true) {
                    left = selectnext(node, edge, x);

                    Edge child;
                    if (left) {
                        child = node.cleft;
                        edge.above = node;
                    } else {
                        child = node.cright;
                        edge.below = node;
                    }

                    if (child != null)
                        node = child;
                    else
                        break;
                }

                // insert node
                edge.parent = node;

                if (left)
                    node.cleft = edge;
                else
                    node.cright = edge;

                if (edge.above != null)
                    edge.above.below = edge;

                if (edge.below != null)
                    edge.below.above = edge;

                // TODO: restructure tree if necessary

            } else {
                this.head = edge;
                edge.parent = null;
                edge.cleft  = null;
                edge.cright = null;
                edge.above  = null;
                edge.below  = null;
            }
        }

        /**
         * Removes the given edge from this sweep-line. The edge must be present in the sweep-line.
         *
         * @param edge the edge to remove.
         */
        void remove(Edge edge) {
            assert edge != null:   "Contract violation: this set may not contain null-objects.";
            assert contains(edge): "Contract violation: the given edge must be contained in this set.";

            // add to stopped list
            stopped.add(edge);

            // remove node from tree
            if (edge.cleft == null && edge.cright == null) {    // no child nodes: simply remove node
                replace(edge, null);

            } else if (edge.cleft == null) {                    // only right child: simple replace
                replace(edge, edge.cright);
                edge.cright.parent = edge.parent;

            } else if (edge.cright == null) {                   // only left child: simple replace
                replace(edge, edge.cleft);
                edge.cleft.parent = edge.parent;

            } else if (edge.cleft.cright == null) {             // left child has only one child: move it up
                replace(edge, edge.cleft);
                edge.cleft.parent = edge.parent;
                edge.cleft.cright = edge.cright;
                edge.cright.parent = edge.cleft;

            } else if (edge.cright.cleft == null) {             // right child has only one child: move it up
                replace(edge, edge.cright);
                edge.cright.parent = edge.parent;
                edge.cright.cleft = edge.cleft;
                edge.cleft.parent = edge.cright;

            } else {                                            // two children: search for replacement
                Edge node;
                if (rnd.nextBoolean()) {                        // replacement is leftmost child in right subtree
                    node = edge.cright.cleft;
                    while (node.cleft != null)
                        node = node.cleft;

                    replace(edge, node);
                    node.parent.cleft = node.cright;
                    node.parent = edge.parent;

                    if (node.cright != null)
                        node.cright.parent = node.parent;

                } else {                                        // replacement is rightmost child in left subtree
                    node = edge.cleft.cright;
                    while (node.cright != null)
                        node = node.cright;

                    replace(edge, node);
                    node.parent.cright = node.cleft;
                    node.parent = edge.parent;

                    if (node.cleft != null)
                        node.cleft.parent = node.parent;
                }

                node.cright = edge.cright;
                node.cleft = edge.cleft;

                if (node.cright != null)
                    node.cright.parent = node;

                if (node.cleft != null)
                    node.cleft.parent = node;

            }

            // update neighbor references
            if (edge.above != null)
                edge.above.below = edge.below;

            if (edge.below != null)
                edge.below.above = edge.above;

            // TODO: restructure tree if necessary

            edge.parent = null;
            edge.cleft  = null;
            edge.cright = null;
            edge.above  = null;
            edge.below  = null;
        }

        /**
         * Swaps the given edges. Both edges mus tbe present in the sweep-line and ordered accordingly.
         *
         * @param a the lower edge of both edges to swap.
         * @param b the upper edge of both edges to swap.
         */
        void swap(Edge a, Edge b) {
            assert a != null && b != null:       "Contract violation: this set may not contain null-objects.";
            assert a != b:                       "Contract violation: cannot swap with itself.";
            assert contains(a) && contains(b):   "Contract violation: the given edges must be contained in this set.";
            assert a.above == b && b.below == a: "Contract violation: the given edges must be ordered and neighbors.";

            // end trapezoids on edges
            trapezoidEnd(a);
            trapezoidEnd(b);

            // swap nodes in tree
            if (a.parent != null) {
                if (a.parent.cleft == a)
                    a.parent.cleft = b;
                else
                    a.parent.cright = b;
            } else {
                head = b;
            }

            if (b.parent != null) {
                if (b.parent.cleft == b)
                    b.parent.cleft = a;
                else
                    b.parent.cright = a;
            } else {
                head = a;
            }

            Edge tmp0 = a.parent;
            a.parent = b.parent;
            b.parent = tmp0;

            Edge tmp1 = a.cleft;
            a.cleft = b.cleft;
            b.cleft = tmp1;

            Edge tmp2 = a.cright;
            a.cright = b.cright;
            b.cright = tmp2;

            if (a.cleft != null) a.cleft.parent = a;
            if (a.cright != null) a.cright.parent = a;
            if (b.cleft != null) b.cleft.parent = b;
            if (b.cright != null) b.cright.parent = b;

            // swap neighbor links
            a.above = b.above;
            b.below = a.below;

            a.below = b;
            b.above = a;

            if (a.above != null)
                a.above.below = a;

            if (b.below != null)
                b.below.above = b;
        }


        /**
         * Perform an update-step to advance this sweep-line, if the given x-value is larger than the current one.
         *
         * @param x the new x-value.
         */
        void update(double x) {
            if (this.x >= x) return;

            Edge bot = bottom();
            while (bot != null) {
                Edge top = closing(bot);
                if (top == null) return;

                // extend old trapezoid to new edge, if possible
                if (bot.isnew) {
                    for (Edge s : stopped) {
                        if (s.right.y > bot.left.y)
                            break;

                        if (s.trapClosing != null && Edge.lcollinear(s, bot)) {
                            bot.trapClosing = s.trapClosing;
                            bot.trapStart = s.trapStart;

                            s.trapClosing = null;
                            break;
                        }
                    }

                    bot.isnew = false;
                }

                // try to extend trapezoid via closing edge or end and create new
                if (bot.trapClosing != top)
                    trapezoidContinue(bot, top);

                bot = top.above;
            }

            for (Edge e : stopped)
                trapezoidEnd(e);

            stopped.clear();

            this.x = x;
        }

        /**
         * Finishes this sweep-line, returning the resulting trapezoidation.
         *
         * @return the trapezoidation generated with this sweep-line.
         */
        ArrayList<Trapezoid> finish() {
            for (Edge e : stopped)
                trapezoidEnd(e);

            return result;
        }


        /**
         * Ends the trapezoid-in-progress associated with the given edge.
         *
         * @param e the edge for which the trapezoid should be ended.
         */
        private void trapezoidEnd(Edge e) {
            if (e.trapClosing != null) {
                if (e.trapStart < x && !Edge.lcollinear(e, e.trapClosing)) {
                    Trapezoid trap = new Trapezoid();
                    trap.bot = e;
                    trap.top = e.trapClosing;
                    trap.left = e.trapStart;
                    trap.right = x;
                    result.add(trap);
                }

                e.trapClosing = null;
            }
        }

        /**
         * Tries to continue the trapezoid-in-progress associated with the given edge for the new top edge. If a
         * continuation is not possible, the old trapezoid will be ended and a new trapezoid will be started.
         *
         * @param bot the bottom edge, with which the trapezoid is associated.
         * @param top the new top-edge of the trapezoid-in-progress associated with the bottom edge.
         */
        private void trapezoidContinue(Edge bot, Edge top) {
            if (bot.trapClosing != null) {
                if (Edge.lcollinear(bot.trapClosing, top)) {
                    bot.trapClosing = top;
                    return;
                }

                if (bot.trapStart < x && !Edge.lcollinear(bot, bot.trapClosing)) {
                    Trapezoid trap = new Trapezoid();
                    trap.bot = bot;
                    trap.top = bot.trapClosing;
                    trap.left = bot.trapStart;
                    trap.right = x;
                    result.add(trap);
                }
            }

            bot.trapClosing = top;
            bot.trapStart = x;
        }


        /**
         * Find the closing (top) edge for the given opening (bottom) edge.
         *
         * @param bot the edge for which the closing (top) edge should be retrieved.
         * @return the topmost closing top-edge for the given opening bottom edge.
         */
        private static Edge closing(Edge bot) {
            Edge top = bot;
            int i = bot.dir;

            while (top.above != null) {
                top = top.above;
                i += top.dir;

                if (i == 0) {
                    // extend trapezoid above collinear edges
                    if (top.above != null && Edge.lcollinear(top, top.above)) {
                        top = top.above;
                        i += top.dir;
                    } else {
                        break;
                    }
                }
            }

            return top != bot ? top : null;
        }

        /**
         * Returns the bottom-most edge in this sweep-line.
         *
         * @return the bottom-most edge in this sweep-line.
         */
        private Edge bottom() {
            if (head == null) return null;

            Edge node = head;
            while (node.cleft != null)
                node = node.cleft;

            return node;
        }

        /**
         * Replaces the given child-node in the sweep-line-tree with the given replacement node.
         *
         * @param child       the node to replace.
         * @param replacement the replacement replacing the node to replace.
         */
        private void replace(Edge child, Edge replacement) {
            assert child != replacement: "Contract violation: cannot replace node with itself.";

            if (child.parent != null) {
                if (child.parent.cleft == child)
                    child.parent.cleft = replacement;
                else
                    child.parent.cright = replacement;
            } else {
                head = replacement;
            }
        }

        /**
         * Checks if the given node is contained in this sweep-line-tree.
         *
         * @param node the edge/node to check for.
         * @return {@code true} if the given edge is contained in this sweep-line-tree.
         */
        private boolean contains(Edge node) {
            while (node.parent != null)
                node = node.parent;

            return node == head;
        }

        /**
         * Select the next node in a down-traversal step for the given parameters.
         *
         * @param current the current tree-node.
         * @param target  the target tree-node (for which the insertion-point should be found).
         * @param x       the current x-position of the sweep-line.
         * @return the next node as child of {@code current} or {@code null} if no such node exists.
         */
        private static boolean selectnext(Edge current, Edge target, double x) {
            double cy = Edge.y(current, x);
            double ty = Edge.y(target, x);

            if (cy != ty)
                return ty <= cy;
            else
                return Edge.y(target, current.right.x) <= Edge.y(current, current.right.x);
        }
    }
}

