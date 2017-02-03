package microtrafficsim.core.vis.scenario.areas.ui;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import microtrafficsim.core.vis.glui.Component;
import microtrafficsim.core.vis.glui.events.MouseAdapter;
import microtrafficsim.core.vis.glui.events.MouseEvent;
import microtrafficsim.core.vis.glui.renderer.ComponentRenderPass;
import microtrafficsim.core.vis.opengl.utils.Color;
import microtrafficsim.core.vis.scenario.areas.ScenarioAreaOverlay;
import microtrafficsim.core.vis.view.OrthographicView;
import microtrafficsim.math.Rect2d;
import microtrafficsim.math.Vec2d;
import microtrafficsim.math.Vec2i;


public class EdgeSplit extends Component {
    private static final float SIZE_INNER = 5.f;
    private static final float SIZE_OUTER = 7.f;

    private static final float HEIGHT_ACTIVE = 12.f;

    private static final Color COLOR_INNER = Color.fromRGBA(0x000000D0);
    private static final Color COLOR_OUTER = Color.fromRGBA(0xFFAB8AD0);

    private static final ComponentRenderPass PASS_OUTER = new AreaVertex.AreaVertexPass(COLOR_OUTER, SIZE_OUTER,
            c -> c instanceof EdgeSplit && ((EdgeSplit) c).active);

    private static final ComponentRenderPass PASS_INNER = new AreaVertex.AreaVertexPass(COLOR_INNER, SIZE_INNER,
            c -> c instanceof EdgeSplit && ((EdgeSplit) c).active);


    private ScenarioAreaOverlay root;
    protected AreaVertex a;
    protected AreaVertex b;
    protected Vec2d pos;

    private boolean active;


    EdgeSplit(ScenarioAreaOverlay root, AreaVertex a, AreaVertex b) {
        super(PASS_OUTER, PASS_INNER);

        this.root = root;
        this.a = a;
        this.b = b;
        this.pos = Vec2d.add(a.getPosition(), b.getPosition()).mul(0.5);
        this.focusable = false;
        this.active = false;

        addMouseListener(new MouseListenerImpl());
        addKeyListener(new KeyListenerImpl());
    }


    @Override
    protected boolean contains(Vec2d p) {
        Vec2d h = getActiveHeightScale();
        return ActiveArea.calculate(a.getPosition(), b.getPosition(), h).contains(p);
    }

    @Override
    protected void updateBounds() {
        Vec2d h = getActiveHeightScale();
        this.aabb = ActiveArea.bounds(a.getPosition(), b.getPosition(), h);
    }

    @Override
    protected Rect2d getBounds() {
        updateBounds();
        return aabb;
    }


    private Vec2d getActiveHeightScale() {
        OrthographicView view = getUIManager().getView();

        Vec2i viewport = view.getSize();
        Rect2d bounds = view.getViewportBounds();

        return new Vec2d(
                (HEIGHT_ACTIVE / 2.0) / viewport.x * (bounds.xmax - bounds.xmin),
                (HEIGHT_ACTIVE / 2.0) / viewport.y * (bounds.ymax - bounds.ymin)
        );
    }


    @Override
    public void redraw(boolean recursive) {
        pos = Vec2d.add(a.getPosition(), b.getPosition()).mul(0.5);
        super.redraw(recursive);
    }


    private void activate(boolean shift) {
        AreaComponent inConstruction = root.getAreaInConstruction();
        boolean activate = (inConstruction == null || inConstruction.getVertices().size() == 1)
                && root.getActiveSplit() == null
                && shift;

        if (activate) {
            active = true;
            root.setActiveSplit(EdgeSplit.this);
            root.abortAreaInConstruction();

            redraw();
        }
    }

    private void deactivate(boolean shift) {
        active = false;

        if (root.getActiveSplit() == EdgeSplit.this) {
            root.setActiveSplit(null);

            if (shift) {
                root.startNewAreaInConstruction();
            }
        }

        redraw();
    }


    private class MouseListenerImpl extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (root.isAreaInConstruction()) return;
            if (root.getActiveSplit() != EdgeSplit.this) return;

            root.clearVertexSelection();
            AreaComponent area = (AreaComponent) getParent();
            area.insert(area.getVertices().indexOf(b), pos, true);

            e.setConsumed(true);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            activate(e.isShiftDown());
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            activate(e.isShiftDown());
        }

        @Override
        public void mouseExited(MouseEvent e) {
            deactivate(e.isShiftDown());
        }
    }

    private class KeyListenerImpl implements KeyListener {
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                activate(true);
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                deactivate(false);
            }
        }
    }

    private static class ActiveArea {
        Vec2d c1;
        Vec2d c2;
        double r;

        ActiveArea(Vec2d c1, Vec2d c2, double r) {
            this.c1 = c1;
            this.c2 = c2;
            this.r = r;
        }

        static ActiveArea calculate(Vec2d a, Vec2d b, Vec2d h) {
            // calculate third points of circles
            Vec2d c = Vec2d.add(a, b).mul(0.5);
            Vec2d cn = Vec2d.sub(b, a).normalize();
            cn = new Vec2d(-cn.y * h.x, cn.x * h.y);
            Vec2d c1 = Vec2d.add(cn, c);
            Vec2d c2 = Vec2d.mul(cn, -1).add(c);

            // calculate perpendiculars
            Vec2d p11o = Vec2d.add(a, c1).mul(0.5);
            Vec2d p11d = Vec2d.sub(a, c1).normalize();
            p11d = new Vec2d(-p11d.y, p11d.x);

            Vec2d p12o = Vec2d.add(c1, b).mul(0.5);
            Vec2d p12d = Vec2d.sub(c1, b).normalize();
            p12d = new Vec2d(-p12d.y, p12d.x);

            Vec2d p21o = Vec2d.add(a, c2).mul(0.5);
            Vec2d p21d = Vec2d.sub(a, c2).normalize();
            p21d = new Vec2d(-p21d.y, p21d.x);

            Vec2d p22o = Vec2d.add(b, c2).mul(0.5);
            Vec2d p22d = Vec2d.sub(c2, b).normalize();
            p22d = new Vec2d(-p22d.y, p22d.x);

            // calculate centers of circles
            Vec2d center1 = intersect(p11o, p11d, p12o, p12d);
            Vec2d center2 = intersect(p21o, p21d, p22o, p22d);

            double r = Vec2d.sub(center1, b).len();
            return new ActiveArea(center1, center2, r);
        }

        static Rect2d bounds(Vec2d a, Vec2d b, Vec2d h) {
            Vec2d n = Vec2d.sub(a, b).normalize();
            n = new Vec2d(-n.y * h.x, n.x * h.y);

            Vec2d p1 = Vec2d.add(a, n);
            Vec2d p2 = Vec2d.sub(a, n);
            Vec2d p3 = Vec2d.add(b, n);
            Vec2d p4 = Vec2d.sub(b, n);

            return Rect2d.from(p1, p2, p3, p4);
        }

        boolean contains(Vec2d p) {
            return Vec2d.sub(p, c1).len() <= r && Vec2d.sub(p, c2).len() <= r;
        }

        private static Vec2d intersect(Vec2d p1, Vec2d d1, Vec2d p2, Vec2d d2) {
            // Note: this code assumes that the lines are not parallel. This is given for any value of ACTIVE_HEIGHT
            //       other than zero.

            double a = (p2.x - p1.x) / (d1.x - d2.x);
            return new Vec2d(p1.x + a * d1.x, p1.y + a * d1.y);
        }
    }
}
