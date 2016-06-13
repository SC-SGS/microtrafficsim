package microtrafficsim.osmcreator.graph;

/**
 * @author Dominic Parga Cacheiro
 */
public enum StreetDirection {
  INCOMING,
  LEAVING,
  BIDIRECTIONAL;

  public StreetDirection merge(StreetDirection other) {
    if (this == other || other == null)
      return this;
    return BIDIRECTIONAL;
  }
}
