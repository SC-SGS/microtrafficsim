package microtrafficsim.core.vis.glui;

import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.glui.renderer.Batch;
import microtrafficsim.core.vis.glui.renderer.BatchBuilder;
import microtrafficsim.core.vis.glui.renderer.ComponentRenderPass;
import microtrafficsim.math.Mat3d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public class BatchUIRenderer {

    private Component root;
    private ArrayList<Batch> batches = new ArrayList<>();
    private Set<Component> dirty = Collections.synchronizedSet(new HashSet<>());


    public BatchUIRenderer(Component root) {
        this.root = root;
    }


    public void initialize(RenderContext context) throws Exception {
        update(context);
    }

    public void dispose(RenderContext context) {
        for (Batch batch : batches)
            batch.dispose(context);

        batches.clear();
    }

    public void redraw(Component component) {
        dirty.add(component);
    }


    public void update(RenderContext context) throws Exception {
        if (dirty.isEmpty()) return;
        // TODO: refactor
        // TODO: update only batches that contain dirty components
        // TODO: consider overlapping components?

        // collect all batch-builders
        ArrayList<BatchBuilder> all = new ArrayList<>();

        Mat3d[] transforms = {root.getTransform()};

        ArrayList<ArrayList<Component>> level = new ArrayList<>(1);
        level.add(root.getComponents());

        BatchBuilder[] parentBuilders = new BatchBuilder[]{ null };

        while (!level.isEmpty()) {
            int maxrp = 0;
            int levelsize = 0;

            for (ArrayList<Component> group : level) {
                for (Component component : group) {
                    if (maxrp < component.getRenderPasses().length)
                        maxrp = component.getRenderPasses().length;

                    if (component.isVisible())
                        levelsize++;
                }
            }

            BatchBuilder[] nextParentBuilders = new BatchBuilder[levelsize];
            Mat3d[] nextTransforms = new Mat3d[levelsize];
            ArrayList<ArrayList<Component>> nextLevel = new ArrayList<>(levelsize);

            // try to batch by parents and same-level render-passes
            for (int rp = 0; rp < maxrp; rp++) {
                ArrayList<BatchBuilder> levelBuilders = new ArrayList<>();

                int levelid = 0;        // number of component in current level
                for (int group = 0; group < level.size(); group++) {
                    BatchBuilder parentBuilder = parentBuilders[group];
                    Mat3d parentTransform = transforms[group];

                    for (Component c : level.get(group)) {
                        if (!c.isVisible())
                            continue;

                        Mat3d transform = nextTransforms[levelid];
                        if (transform == null) {
                            transform = Mat3d.mul(parentTransform, c.getTransform());
                            nextTransforms[levelid] = transform;
                        }

                        if (rp < c.getRenderPasses().length && c.getRenderPasses()[rp].isActiveFor(c)) {
                            ComponentRenderPass pass = c.getRenderPasses()[rp];
                            BatchBuilder builder;

                            if (rp == 0 && parentBuilder != null && parentBuilder.isApplicableFor(c, pass)) {
                                builder = parentBuilder;
                            } else {
                                builder = findApplicableBuilder(c, pass, levelBuilders);

                                if (builder == null) {
                                    builder = pass.createBuilder();
                                    levelBuilders.add(builder);
                                }
                            }

                            builder.add(c, c.getRenderPasses()[rp], transform);
                            nextParentBuilders[levelid] = builder;
                        }

                        if (rp == 0)
                            nextLevel.add(c.getComponents());

                        levelid++;
                    }
                }

                all.addAll(levelBuilders);
            }

            transforms = nextTransforms;
            level = nextLevel;
            parentBuilders = nextParentBuilders;
        }

        ArrayList<Batch> batches = new ArrayList<>(all.size());
        for (BatchBuilder builder : all) {
            Batch batch = builder.build(context);
            batch.initialize(context);
            batches.add(batch);
        }

        for (Batch batch : this.batches)
            batch.dispose(context);

        this.batches = batches;
        dirty.clear();
    }

    private static BatchBuilder findApplicableBuilder(Component c, ComponentRenderPass rp, ArrayList<BatchBuilder> in) {
        for (BatchBuilder builder : in)
            if (builder.isApplicableFor(c, rp))
                return builder;

        return null;
    }


    public void display(RenderContext context) throws Exception {
        for (Batch batch : batches)
            batch.display(context);

        context.ShaderState.unbind(context.getDrawable().getGL().getGL2ES2());
    }
}
