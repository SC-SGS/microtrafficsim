package microtrafficsim.osmcreator.user.gestures.selection;

import java.util.Set;

/**
 * @author Dominic Parga Cacheiro
 */
public interface SelectionModel {
  Set<Selectable> getSelectables();
  void add(Selectable selectable);

  /**
   * @param selectable
   *
   * @return {@code true} if this model contained the specified element
   */
  boolean remove(Selectable selectable);
}
