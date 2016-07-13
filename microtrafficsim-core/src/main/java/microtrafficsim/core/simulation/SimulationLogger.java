package microtrafficsim.core.simulation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SimulationLogger {

    private final Logger logger;
    public boolean       enabled;

    public SimulationLogger() {
        super();
        logger  = LoggerFactory.getLogger(Simulation.class);
        enabled = false;
    }

    public SimulationLogger(boolean enabled) {
        super();
        logger       = LoggerFactory.getLogger(Simulation.class);
        this.enabled = enabled;
    }

    public void info(String msg) {
        logger.info(msg);
    }

    public void info(String[] lines) {
        StringBuilder builder = new StringBuilder();
        for (String line : lines)
            builder.append(line + "\n");
        logger.info(builder.toString());
    }

    public void debug(String msg) {
        logger.debug(msg);
    }

    public void debug(String[] lines) {
        StringBuilder builder = new StringBuilder();
        for (String line : lines)
            builder.append(line + "\n");
        logger.debug(builder.toString());
    }

    public void infoNanoseconds(String label, long ns) {
        logger.info(buildTimeString(label, ns, "ns"));
    }

    public void debugNanoseconds(String label, long ns) {
        logger.debug(buildTimeString(label, ns, "ns"));
    }

    private String buildTimeString(String label, long time, String unit) {
        // Long.MAX_VALUE < 10^20
        // => log10(Long.MAX_VALUE) / 3 < 7
        int[] bla = new int[] {-1, -1, -1, -1, -1, -1, -1};
        int c     = 0;
        while (time != 0) {
            bla[c++] = (int) (time % 1000);
            time     = time / 1000;
        }

        // build string
        c--;
        boolean       first   = true;
        StringBuilder builder = new StringBuilder(label);
        while (c >= 0) {
            int i = bla[c];
            if (i != -1) {
                if (first) {
                    first = false;
                    builder.append(i);
                } else {
                    if (i < 10)
                        builder.append("00" + i);
                    else if (i < 100)
                        builder.append("0" + i);
                    else
                        builder.append(i);
                }

                if (c != 0)
                    builder.append(" ");
                else
                    builder.append(unit);
            }
            c--;
        }
        return builder.toString();
    }
}