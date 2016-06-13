package microtrafficsim.osmcreator.user.gestures.selection.impl;

import javafx.scene.Group;
import microtrafficsim.osmcreator.user.gestures.selection.Selectable;
import microtrafficsim.osmcreator.user.gestures.selection.SelectionModel;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Dominic Parga Cacheiro
 */
public class SelectionModelGroup extends Group implements SelectionModel {
  private Set<Selectable> selectables;

  public SelectionModelGroup() {
    super();
    selectables = new HashSet<>();
  }

  /*
  |====================|
  | (i) SelectionModel |
  |====================|
  */
  @Override
  public Set<Selectable> getSelectables() {
    return selectables;
  }

  @Override
  public void add(Selectable selectable) {
    selectables.add(selectable);
  }

  @Override
  public boolean remove(Selectable selectable) {
    return selectables.remove(selectable);
  }
}
