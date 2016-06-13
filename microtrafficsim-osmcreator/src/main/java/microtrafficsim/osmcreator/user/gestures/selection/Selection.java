package microtrafficsim.osmcreator.user.gestures.selection;

import javafx.scene.layout.Pane;

import java.util.Set;

/**
 * @author Dominic Parga Cacheiro
 */
public interface Selection {
  boolean isEnabled();
  Set<Selectable> getSelectedItems();
  void select(Selectable selectable);
  void unselect(Selectable selectable);
  void unselectAll();
  /* marking */
  void activate(Pane parent, SelectionModel selectionModel);
  void startSelection(Pane parent, double x, double y);
  void holdSelection(double x, double y);
  void stopSeleciton(Pane parent, SelectionModel selectionModel);
}
