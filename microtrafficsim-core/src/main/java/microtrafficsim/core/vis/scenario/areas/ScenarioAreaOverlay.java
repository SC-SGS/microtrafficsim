package microtrafficsim.core.vis.scenario.areas;

import com.jogamp.newt.event.KeyAdapter;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseListener;
import microtrafficsim.core.vis.Overlay;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.glui.Component;
import microtrafficsim.core.vis.glui.DirectBatchUIOverlay;
import microtrafficsim.core.vis.glui.events.MouseEvent;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.core.vis.scenario.areas.ui.*;
import microtrafficsim.core.vis.view.OrthographicView;
import microtrafficsim.math.Rect2d;
import microtrafficsim.math.Vec2d;
import microtrafficsim.math.geometry.polygons.Polygon;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;

// TODO: proper line rendering for area outline

// TODO: pre-load shaders?
// TODO: re-use batches?


/**
 * @author Maximilian Luz
 */
public class ScenarioAreaOverlay implements Overlay {
    private DirectBatchUIOverlay ui;

    private HashSet<AreaComponent> selectedAreas = new HashSet<>();
    private HashSet<AreaVertex> selectedVertices = new HashSet<>();
    private EdgeSplit activeSplit = null;

    private PropertyFrame properties = null;
    private SelectionRectangle rectangle = null;
    private AreaComponent construction = null;


    public ScenarioAreaOverlay() {
        this.ui = new DirectBatchUIOverlay();
        this.ui.getRootComponent().addMouseListener(new MouseListenerImpl());
        this.ui.getRootComponent().addKeyListener(new KeyListenerImpl());

        SwingUtilities.invokeLater(() -> {
            properties = new PropertyFrame(ui);

            /* put properties to bottom right */
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
            Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
            int x = (int) rect.getMaxX() - properties.getWidth();
            int y = 0;
            properties.setLocation(x - 100, y + 100);

            properties.setVisible(false);
            properties.setResizable(false);
            properties.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            properties.setAlwaysOnTop(true);
        });

        this.rectangle = new SelectionRectangle();
    }


    public void add(Area area) {
        ui.addComponent(new AreaComponent(this, area));
    }

    public void remove(Area area) {
        for (Component c : ui.getComponents()) {
            if (c instanceof AreaComponent) {
                if (((AreaComponent) c).getArea() == area) {
                    ui.removeComponent(c);
                }
            }
        }
    }

    public void removeAllAreas() {
        ui.getComponents().stream()
                .filter(c -> c instanceof AreaComponent)
                .forEach(c -> ui.removeComponent(c));
    }

    public ArrayList<Area> getAreas() {
        ArrayList<Area> areas = new ArrayList<>();

        for (Component c : ui.getComponents()) {
            if (c instanceof AreaComponent) {
                areas.add(((AreaComponent) c).getArea());
            }
        }

        return areas;
    }


    @Override
    public void setView(OrthographicView view) {
        ui.setView(view);
    }

    @Override
    public void initialize(RenderContext context) throws Exception {
        ui.initialize(context);
        rectangle.initialize(context);
    }

    @Override
    public void dispose(RenderContext context) throws Exception {
        ui.dispose(context);
        rectangle.dispose(context);

        SwingUtilities.invokeLater(() -> {
            properties.dispose();
            properties = null;
        });
    }

    @Override
    public void resize(RenderContext context) throws Exception {
        ui.resize(context);
    }

    @Override
    public void display(RenderContext context, MapBuffer map) throws Exception {
        ui.display(context, map);
        rectangle.display(context);
    }

    @Override
    public void setEnabled(boolean enabled) {
        ui.setEnabled(enabled);
        properties.setVisible(enabled);
    }

    @Override
    public boolean isEnabled() {
        return ui.isEnabled();
    }

    @Override
    public KeyListener getKeyListener() {
        return ui.getKeyListener();
    }

    @Override
    public MouseListener getMouseListener() {
        return ui.getMouseListener();
    }


    public void select(AreaComponent component) {
        if (component.isSelected()) return;

        component.setSelected(true);
        selectedAreas.add(component);

        if (properties != null) {
            if (!properties.isVisible()) {
                properties.setFocusableWindowState(false);
                properties.setVisible(true);
                properties.setFocusableWindowState(true);
            }

            properties.setAreas(selectedAreas);
        }
    }

    public void deselect(AreaComponent component) {
        if (!component.isSelected()) return;

        component.setSelected(false);
        selectedAreas.remove(component);

        if (properties != null)
            properties.setAreas(selectedAreas);
    }


    public void select(AreaVertex vertex) {
        if (vertex.isSelected()) return;

        vertex.setSelected(true);
        selectedVertices.add(vertex);
    }

    public void deselect(AreaVertex vertex) {
        if (!vertex.isSelected()) return;

        vertex.setSelected(false);
        selectedVertices.remove(vertex);
    }


    public void clearAreaSelection() {
        for (AreaComponent c : selectedAreas) {
            c.setSelected(false);
        }
        selectedAreas.clear();

        if (properties != null)
            properties.setAreas(selectedAreas);
    }

    public void clearVertexSelection() {
        for (AreaVertex v : selectedVertices) {
            v.setSelected(false);
        }
        selectedVertices.clear();
    }


    public void moveSelectedAreas(Vec2d delta) {
        for (AreaComponent c : selectedAreas) {
            c.move(delta);
        }
    }

    public void moveSelectedVertices(Vec2d delta) {
        for (AreaVertex v : selectedVertices) {
            v.move(delta);
        }
    }


    public HashSet<AreaVertex> getSelectedVertices() {
        return selectedVertices;
    }

    public HashSet<AreaComponent> getSelectedAreas() {
        return selectedAreas;
    }


    public void startNewAreaInConstruction() {
        if (construction != null) return;

        Area.Type type = properties != null ? properties.getLastSelectedType() : Area.Type.ORIGIN;
        construction = new AreaComponent(ScenarioAreaOverlay.this, type);
        construction.add(lastmove, true);
        ui.addComponent(construction);
    }

    public AreaComponent getAreaInConstruction() {
        return construction;
    }

    public void completeAreaInConstruction() {
        if (construction == null) return;

        ArrayList<AreaVertex> vertices = construction.getVertices();
        if (vertices.size() >= 4) {
            construction.remove(vertices.get(vertices.size() - 1));
            construction.setComplete(true);

            clearAreaSelection();
            clearVertexSelection();
            select(construction);
        } else {
            deselect(vertices.get(vertices.size() - 1));
            ui.removeComponent(construction);
        }

        construction = null;
    }

    public void abortAreaInConstruction() {
        if (construction == null) return;

        ui.removeComponent(construction);
        construction = null;
    }

    public boolean isAreaInConstruction() {
        return construction != null;
    }


    public EdgeSplit getActiveSplit() {
        return activeSplit;
    }

    public void setActiveSplit(EdgeSplit split) {
        this.activeSplit = split;
    }


    private Vec2d lastmove = null;

    private class MouseListenerImpl extends microtrafficsim.core.vis.glui.events.MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() != MouseEvent.BUTTON1) return;
            if (e.isControlDown()) return;

            boolean shift = e.isShiftDown();
            ui.getContext().addTask(c -> {
                clearAreaSelection();
                clearVertexSelection();

                if (shift) {
                    select(construction);
                    construction.add(e.getPointer(), true);
                }

                return null;
            });
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getButton() != MouseEvent.BUTTON3) return;

            ui.getContext().addTask(c -> {
                rectangle.begin(e.getPointer());
                return null;
            });

            e.setConsumed(true);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.getButton() != MouseEvent.BUTTON3) return;
            if (!rectangle.isSelectionValid()) return;

            ui.getContext().addTask(ctx -> {
                Rect2d rect = rectangle.end(e.getPointer());

                clearVertexSelection();
                if (!e.isControlDown()) {
                    clearAreaSelection();
                }

                for (Component c : ui.getComponents()) {
                    if (!(c instanceof AreaComponent)) continue;
                    AreaComponent area = ((AreaComponent) c);

                    if (area.getBounds().intersects(rect) && Polygon.intersects(area.getOutline(), rect)) {
                        select(area);
                    }
                }

                return null;
            });

            e.setConsumed(true);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            lastmove = e.getPointer();

            Vec2d pos = e.getPointer();
            if (e.isShiftDown()) {
                ui.getContext().addTask(c -> {
                    if (construction == null) return null;

                    ArrayList<AreaVertex> vertices = construction.getVertices();
                    vertices.get(vertices.size() - 1).setPosition(pos);

                    return null;
                });
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (e.getButton() != MouseEvent.BUTTON3) return;

            if (rectangle.isEnabled()) {
                Vec2d pos = e.getPointer();

                ui.getContext().addTask(c -> {
                    rectangle.update(pos);
                    return null;
                });

                e.setConsumed(true);
            }
        }
    }

    private class KeyListenerImpl extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                if (e.isAutoRepeat()) return;

                ui.getContext().addTask(c -> {
                    startNewAreaInConstruction();
                    return null;
                });

            } else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                if (e.isShiftDown()) return;

                ui.getContext().addTask(c -> {
                    if (!selectedVertices.isEmpty()) {      // remove vertices (or area, if not enough vertices are left)
                        assert selectedAreas.size() == 1;

                        AreaComponent area = selectedAreas.iterator().next();

                        if (area == construction && area.getOutline().length >= 2) {
                            ArrayList<AreaVertex> vertices = area.getVertices();
                            area.remove(vertices.get(vertices.size() - 1));
                            vertices.get(vertices.size() - 2).setPosition(lastmove);

                        } else if (area.getOutline().length - selectedVertices.size() >= 3) {
                            area.removeAll(selectedVertices);

                        } else {
                            ui.removeComponent(area);
                            construction = null;
                        }

                        clearVertexSelection();

                        if (construction != null) {
                            ArrayList<AreaVertex> vertices = construction.getVertices();
                            select(vertices.get(vertices.size() - 1));
                        }

                    } else {                                // remove areas
                        for (AreaComponent area : selectedAreas)
                            ui.removeComponent(area);

                        selectedAreas.clear();
                        properties.setAreas(selectedAreas);
                    }

                    return null;
                });
                e.setConsumed(true);

            } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                if (!selectedVertices.isEmpty()) {
                    ui.getContext().addTask(c -> {
                        clearVertexSelection();
                        return null;
                    });
                    e.setConsumed(true);
                } else if (!selectedAreas.isEmpty()) {
                    ui.getContext().addTask(c -> {
                        clearAreaSelection();
                        return null;
                    });
                    e.setConsumed(true);
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                ui.getContext().addTask(c -> {
                    completeAreaInConstruction();
                    return null;
                });
            }
        }
    }
}
