package microtrafficsim.osmcreator.user.gestures.selection;

import javafx.geometry.Bounds;

/**
 * @author Dominic Parga Cacheiro
 */
public interface Selectable {
  void setSelected(boolean selected);
  boolean isSelected();
  Bounds getBoundsInParent();
}
