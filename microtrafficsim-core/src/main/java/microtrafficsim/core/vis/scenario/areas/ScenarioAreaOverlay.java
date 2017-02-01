package microtrafficsim.core.vis.scenario.areas;


import com.jogamp.newt.event.KeyAdapter;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseListener;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.vis.Overlay;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.glui.DirectBatchUIOverlay;
import microtrafficsim.core.vis.glui.events.MouseEvent;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.core.vis.scenario.areas.ui.AreaComponent;
import microtrafficsim.core.vis.scenario.areas.ui.AreaVertex;
import microtrafficsim.core.vis.scenario.areas.ui.EdgeSplit;
import microtrafficsim.core.vis.scenario.areas.ui.PropertyFrame;
import microtrafficsim.core.vis.view.OrthographicView;
import microtrafficsim.math.Vec2d;
import microtrafficsim.math.geometry.polygons.Polygon;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashSet;

// TODO: edge-split-component
// TODO: selection by rectangle
// TODO: proper line rendering for area outline

// TODO: pre-load shaders?
// TODO: re-use batches?


public class ScenarioAreaOverlay implements Overlay {

    private DirectBatchUIOverlay ui;
    private Projection projection;

    private HashSet<AreaComponent> selectedAreas = new HashSet<>();
    private HashSet<AreaVertex> selectedVertices = new HashSet<>();
    private EdgeSplit activeSplit = null;

    private AreaComponent construction = null;
    private PropertyFrame properties = null;


    public ScenarioAreaOverlay(Projection projection) {
        this.ui = new DirectBatchUIOverlay();
        this.ui.getRootComponent().addMouseListener(new MouseListenerImpl());
        this.ui.getRootComponent().addKeyListener(new KeyListenerImpl());
        this.projection = projection;

        SwingUtilities.invokeLater(() -> {
                properties = new PropertyFrame();
                properties.setVisible(false);
                properties.setResizable(false);
                properties.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                properties.setAlwaysOnTop(true);
        });

        loadTokyoTestingPolygons();
    }


    private void loadTokyoTestingPolygons() {
        Polygon[] spawn = {
                new Polygon(new Vec2d[]{
                        projection.project(new Coordinate(35.613840948933316, 139.7211548375855)),
                        projection.project(new Coordinate(35.613462626375025, 139.75994834446493)),
                        projection.project(new Coordinate(35.622352428408696, 139.76000635856752)),
                        projection.project(new Coordinate(35.62237600747141, 139.75629345600166)),
                        projection.project(new Coordinate(35.62185726648556, 139.75577132907836)),
                        projection.project(new Coordinate(35.621621474015924, 139.75530721625762)),
                        projection.project(new Coordinate(35.62145641887346, 139.75423395535967)),
                        projection.project(new Coordinate(35.621621474015924, 139.75144927843527)),
                        projection.project(new Coordinate(35.621621474015924, 139.7507241021529)),
                        projection.project(new Coordinate(35.620395341965995, 139.74651807971503)),
                        projection.project(new Coordinate(35.62025386398122, 139.74622800920204)),
                        projection.project(new Coordinate(35.61808450353957, 139.74419751561135)),
                        projection.project(new Coordinate(35.61690547861829, 139.74222503612324)),
                        projection.project(new Coordinate(35.61681115587373, 139.74077468355847)),
                        projection.project(new Coordinate(35.61685831725991, 139.7377289431724)),
                        projection.project(new Coordinate(35.616575348525686, 139.73726483035168)),
                        projection.project(new Coordinate(35.61600940805381, 139.73685873163356)),
                        projection.project(new Coordinate(35.61626879793382, 139.73546639317135)),
                        projection.project(new Coordinate(35.617848699945796, 139.73439313227342)),
                        projection.project(new Coordinate(35.617943021466715, 139.73401604060655)),
                        projection.project(new Coordinate(35.617848699945796, 139.73334887842677)),
                        projection.project(new Coordinate(35.61775437831362, 139.73282675150344)),
                        projection.project(new Coordinate(35.61791944109692, 139.73192753291326)),
                        projection.project(new Coordinate(35.61836746693439, 139.73018710983553)),
                        projection.project(new Coordinate(35.61869758962963, 139.72949094060442)),
                        projection.project(new Coordinate(35.620772615368324, 139.72798257393706)),
                        projection.project(new Coordinate(35.62081977441848, 139.72740243291113)),
                        projection.project(new Coordinate(35.620796194896876, 139.72603910150025)),
                        projection.project(new Coordinate(35.621810108047264, 139.72426967137122)),
                        projection.project(new Coordinate(35.62294190289077, 139.7224132200883)),
                        projection.project(new Coordinate(35.623696423886855, 139.72081783226702))
                }),
                new Polygon(new Vec2d[]{
                        projection.project(new Coordinate (35.63937247821449, 139.73230627569114)),
                        projection.project(new Coordinate (35.63870966518994, 139.73208384882096)),
                        projection.project(new Coordinate (35.63590771303082, 139.7315648527906)),
                        projection.project(new Coordinate (35.635726938550484, 139.73301062744665)),
                        projection.project(new Coordinate (35.63532019447478, 139.73464175782783)),
                        projection.project(new Coordinate (35.63482306001563, 139.73577242775113)),
                        projection.project(new Coordinate (35.634536829681956, 139.73640263721657)),
                        projection.project(new Coordinate (35.63431085764183, 139.7368660265294)),
                        projection.project(new Coordinate (35.63417527411111, 139.7375518427124)),
                        projection.project(new Coordinate (35.63408488496287, 139.7379596253077)),
                        projection.project(new Coordinate (35.63547084067009, 139.73846008576558)),
                        projection.project(new Coordinate (35.636434969596365, 139.73883079721583)),
                        projection.project(new Coordinate (35.637188187227544, 139.73914590194855)),
                        projection.project(new Coordinate (35.63795646189825, 139.73946100668127)),
                        projection.project(new Coordinate (35.63889043292365, 139.73998000271166)),
                        projection.project(new Coordinate (35.63935741434317, 139.74022096515432))
                }),

        };

        Polygon[] dest = {
                new Polygon(new Vec2d[]{
                        projection.project(new Coordinate(35.63945929635736, 139.72115638347853)),
                        projection.project(new Coordinate(35.62429182065404, 139.72080285401069)),
                        projection.project(new Coordinate(35.62437392569791, 139.72110587926883)),
                        projection.project(new Coordinate(35.62540023163242, 139.72163617347059)),
                        projection.project(new Coordinate(35.626672832695796, 139.72330281239036)),
                        projection.project(new Coordinate(35.62792488816708, 139.72509571183437)),
                        projection.project(new Coordinate(35.628848523077934, 139.72615630023785)),
                        projection.project(new Coordinate(35.629505323633495, 139.7268633591735)),
                        projection.project(new Coordinate(35.63012106925354, 139.727923947577)),
                        projection.project(new Coordinate(35.63071628551135, 139.72890877966597)),
                        projection.project(new Coordinate(35.631516741766916, 139.72989361175493)),
                        projection.project(new Coordinate(35.63207089909461, 139.7306006706906)),
                        projection.project(new Coordinate(35.63241981099668, 139.7307269312148)),
                        projection.project(new Coordinate(35.63346653756495, 139.73092894805356)),
                        projection.project(new Coordinate(35.63441063212425, 139.7310552085778)),
                        projection.project(new Coordinate(35.63531366865945, 139.73115621699716)),
                        projection.project(new Coordinate(35.63601146263041, 139.73125722541656)),
                        projection.project(new Coordinate(35.63709918814083, 139.73148449436016)),
                        projection.project(new Coordinate(35.638104807989826, 139.73163600698922)),
                        projection.project(new Coordinate(35.63882310013475, 139.73176226751346)),
                        projection.project(new Coordinate(35.639356684405975, 139.7320147885619))
                }),
                new Polygon(new Vec2d[]{
                        projection.project(new Coordinate(35.639347754350815, 139.7405500000003)),
                        projection.project(new Coordinate(35.63755913068822, 139.73963139919263)),
                        projection.project(new Coordinate(35.63583268205591, 139.73894244858687)),
                        projection.project(new Coordinate(35.6340128715037, 139.73833004804843)),
                        projection.project(new Coordinate(35.63189748408799, 139.73779419757727)),
                        projection.project(new Coordinate(35.62920650596128, 139.73769850999315)),
                        projection.project(new Coordinate(35.62597099036785, 139.7381003978465)),
                        projection.project(new Coordinate(35.625162091010125, 139.73848314818304)),
                        projection.project(new Coordinate(35.62541098399183, 139.73938261147387)),
                        projection.project(new Coordinate(35.62768209665832, 139.73999501201234)),
                        projection.project(new Coordinate(35.6317886023385, 139.74116240053874)),
                        projection.project(new Coordinate(35.63197525667544, 139.741353775707)),
                        projection.project(new Coordinate(35.63209969265792, 139.74160256342574)),
                        projection.project(new Coordinate(35.63211524714212, 139.7419279012118)),
                        projection.project(new Coordinate(35.63188192956144, 139.74508559148816)),
                        projection.project(new Coordinate(35.63166416587174, 139.74864516961787)),
                        projection.project(new Coordinate(35.63155528380442, 139.75004220834623)),
                        projection.project(new Coordinate(35.6314619562001, 139.7520707851298)),
                        projection.project(new Coordinate(35.63132196458931, 139.75419504949755)),
                        projection.project(new Coordinate(35.63223968290666, 139.7546543499014)),
                        projection.project(new Coordinate(35.63230190071633, 139.75758238997582)),
                        projection.project(new Coordinate(35.63208413817069, 139.75779290266092)),
                        projection.project(new Coordinate(35.63213080162328, 139.76012767971375)),
                        projection.project(new Coordinate(35.639425519688935, 139.7601659547474))
                })
        };

        for (Polygon area : spawn)
            ui.addComponent(new AreaComponent(this, area, AreaComponent.Type.ORIGIN));

        for (Polygon area : dest)
            ui.addComponent(new AreaComponent(this, area, AreaComponent.Type.DESTINATION));
    }


    @Override
    public void setView(OrthographicView view) {
        ui.setView(view);
    }

    @Override
    public void initialize(RenderContext context) throws Exception {
        ui.initialize(context);
    }

    @Override
    public void dispose(RenderContext context) throws Exception {
        ui.dispose(context);
    }

    @Override
    public void resize(RenderContext context) throws Exception {
        ui.resize(context);
    }

    @Override
    public void display(RenderContext context, MapBuffer map) throws Exception {
        ui.display(context, map);
    }

    @Override
    public void setEnabled(boolean enabled) {
        ui.setEnabled(enabled);
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

        AreaComponent.Type type = properties != null ? properties.getLastSelectedType() : AreaComponent.Type.ORIGIN;
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


    private Vec2d lastmove = null;

    public boolean isAreaInConstruction() {
        return construction != null;
    }


    public EdgeSplit getActiveSplit() {
        return activeSplit;
    }

    public void setActiveSplit(EdgeSplit split) {
        this.activeSplit = split;
    }


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
            // TODO: selection-rectangle
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            // TODO: selection-rectangle
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            lastmove = e.getPointer();
            if (!e.isShiftDown()) return;

            Vec2d pos = e.getPointer();
            ui.getContext().addTask(c -> {
                if (construction == null) return null;

                ArrayList<AreaVertex> vertices = construction.getVertices();
                vertices.get(vertices.size() - 1).setPosition(pos);

                return null;
            });

            // TODO: selection-rectangle
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
                    System.out.println(selectedVertices.size());
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
