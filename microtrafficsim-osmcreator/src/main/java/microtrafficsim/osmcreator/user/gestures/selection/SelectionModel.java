package microtrafficsim.osmcreator.user.gestures.selection;

import java.util.Set;

/**
 * @author Dominic Parga Cacheiro
 */
public interface SelectionModel<T extends Selectable> {
  Set<T> getSelectables();
  void add(T selectable);

  /**
   * @param selectable
   *
   * @return {@code true} if this model contained the specified element
   */
  boolean remove(T selectable);
}