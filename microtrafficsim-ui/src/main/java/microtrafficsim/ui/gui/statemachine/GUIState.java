package microtrafficsim.ui.gui.statemachine;


/**
 * @author Dominic Parga Cacheiro
 */
public enum GUIState {
    // lim <=> limited
    //                    vis | user | map | sim | pref
    RAW,               //  0  |  0   |  0  |  0  |  0
    INIT,              //  1  |  1   |  0  |  0  |  0
    PARSING,           //  0  |  0   |  0  |  0  |  0
    PARSING_SIM_RUN,   //  1  | lim  |  1  |  1  |  0
    PARSING_SIM_PAUSE, //  1  | lim  |  1  |  1  |  0
    MAP,               //  1  |  1   |  1  |  0  |  0
    SIM_NEW,           //  1  |  1   |  1  |  -  |  1
    SIM_PAUSE,         //  1  |  1   |  1  |  1  |  0
    SIM_RUN,           //  1  |  1   |  1  |  1  |  0
    SIM_EDIT           //  1  |  1   |  1  |  1  |  1

    /* transitions:
     *               RAW ---create vis-----> INIT
     *               RAW ---load map-------> PARSING
     *
     *              INIT ---load map-------> PARSING
     *
     *           PARSING ---did parse------> MAP
     *
     * PARSING_SIM_PAUSE ---did parse------> MAP
     * PARSING_SIM_PAUSE ---run sim--------> PARSING_SIM_RUN
     * PARSING_SIM_PAUSE ---run one step---> PARSING_SIM_PAUSE
     *
     *   PARSING_SIM_RUN ---did parse------> MAP
     *   PARSING_SIM_RUN ---run one step---> PARSING_SIM_PAUSE
     *   PARSING_SIM_RUN ---pause sim------> PARSING_SIM_PAUSE
     *
     *               MAP ---load map-------> PARSING
     *               MAP ---prepare sim----> SIM_NEW
     *
     *           SIM_NEW ---accept---------> SIM_PAUSE
     *           SIM_NEW ---cancel---------> if sim = 1 then SIM_PAUSE else MAP end if;
     *
     *         SIM_PAUSE ---load map-------> PARSING_SIM_PAUSE
     *         SIM_PAUSE ---prepare sim----> SIM_NEW
     *         SIM_PAUSE ---edit sim-------> SIM_EDIT
     *         SIM_PAUSE ---run sim--------> SIM_RUN
     *         SIM_PAUSE ---run one step---> SIM_PAUSE
     *
     *           SIM_RUN ---load map-------> PARSING_SIM_PAUSE
     *           SIM_RUN ---prepare sim----> SIM_NEW
     *           SIM_RUN ---edit sim-------> SIM_EDIT
     *           SIM_RUN ---run one step---> SIM_PAUSE
     *           SIM_RUN ---pause sim------> SIM_PAUSE
     *
     *           SIM_EDIT ---accept---------> SIM_PAUSE
     *           SIM_EDIT ---cancel---------> SIM_PAUSE
     *
     *             ****** ---exit-----------> EXIT
     */
}
