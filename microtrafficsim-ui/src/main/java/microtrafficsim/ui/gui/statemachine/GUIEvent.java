package microtrafficsim.ui.gui.statemachine;


/**
 * These events can be used as trigger for user input events.
 *
 * @author Dominic Parga Cacheiro
 */
public enum GUIEvent {
    LOAD_MAP,
    SAVE_MAP,
    CHANGE_AREA_SELECTION,
    NEW_SCENARIO,
    ACCEPT_PREFS,
    CANCEL_PREFS,
    EDIT_SCENARIO,
    RUN_SIM,
    RUN_SIM_ONE_STEP,
    PAUSE_SIM,
    EXIT
}
