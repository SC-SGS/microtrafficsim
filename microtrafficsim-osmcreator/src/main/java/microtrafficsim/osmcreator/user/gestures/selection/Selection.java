package microtrafficsim.osmcreator.user.gestures.selection;

import javafx.scene.layout.Pane;

import java.util.Set;

/**
 * @author Dominic Parga Cacheiro
 */
public interface Selection<T extends Selectable> {
  boolean isActive();
  Set<T> getSelectedItems();
  void select(T selectable);
  void unselect(T selectable);
  void unselectAll();
  /* marking */
  void activate(Pane parent, SelectionModel<T> selectionModel);
  void startSelection(Pane parent, double x, double y);
  void holdSelection(double x, double y);
  void stopSelection(Pane parent, SelectionModel<T> selectionModel);
}
