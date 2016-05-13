package microtrafficsim.ui.gui;

import microtrafficsim.core.simulation.controller.Simulation;
import microtrafficsim.core.simulation.controller.configs.SimulationConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Hashtable;

/**
 * @author Dominic Parga Cacheiro
 */
public class ParameterFrame extends JFrame {

    private static final Font HEADER_FONT = new Font("Arial", Font.BOLD, 16);
    private static final Font TEXT_FONT = new Font("Arial", Font.PLAIN, 12);
    private static final int[] SPEEDUP_TABLE = new int[] { 1, 2, 4, 5, 10, 20, 25, 50, 100 };
    private static int SPEEDUP_TABLE_REVERSE(long ms) {

        long SPEEDUP = Math.max(1, 1000 / ms);
        for (int i = SPEEDUP_TABLE.length - 1; i >= 0; i--) {
            if (SPEEDUP_TABLE[i] <= SPEEDUP)
                return i;
        }
        return 0;
    }

    private ParameterFrame() {
        super("Logic parameter settings");

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setMinimumSize(new Dimension(100, 100));

        setVisible(true);
    }

    public static ParameterFrame create(SimulationConfig config) {

//        create_demo();

        /* init */
        ParameterFrame jframe = new ParameterFrame();
        jframe.setLayout(new GridBagLayout());

        GridBagConstraints constraints;
        JComponent c;

        /* msPerTimeStep - JLabel */
        constraints = new GridBagConstraints();
        constraints.weightx = 0;
        constraints.insets = new Insets(5, 5, 5, 5);
        c = new JLabel("speedup");
        c.setFont(TEXT_FONT);
        jframe.add(c, constraints);

        /* msPerTimeStep - JSlider */
        constraints = new GridBagConstraints();
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 5, 5, 5);
        // speedup: 1, 2, 4, 5, 10, 20, 25, 50, 100
        JSlider slider = new JSlider(0, SPEEDUP_TABLE.length - 1, SPEEDUP_TABLE_REVERSE(config.msPerTimeStep.get()));
        slider.setMajorTickSpacing(1);
        Hashtable labelTable = new Hashtable();
        for (int i = 0; i < SPEEDUP_TABLE.length; i++)
            labelTable.put(i, new JLabel("" + SPEEDUP_TABLE[i]));
        slider.setLabelTable(labelTable);
        slider.setPaintTicks(true);
        slider.setSnapToTicks(true);
        slider.setPaintLabels(true);
        slider.addChangeListener(e -> {
            JSlider source = (JSlider)e.getSource();
            if (!source.getValueIsAdjusting())
                config.msPerTimeStep.set(1000L / SPEEDUP_TABLE[source.getValue()]);
        });
        jframe.add(slider, constraints);

        /* TODO */

        /* pack and center frame on screen */
        jframe.pack();
        jframe.setLocationRelativeTo(null); // center on screen; close to setVisible

        return jframe;
    }

    public static ParameterFrame create_demo() {

        ParameterFrame jframe = new ParameterFrame();
        jframe.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        JComponent c;

        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.gridheight = 2;
        constraints.insets = new Insets(0, 5, 0, 5);
        JLabel header = new JLabel("Benno's Pizza Service");
        header.setFont(HEADER_FONT);
        jframe.add(header, constraints);

        constraints.gridheight = 1;
        constraints.gridwidth = GridBagConstraints.RELATIVE;
        constraints.anchor = GridBagConstraints.WEST;
        c = new JLabel("Name:");
        c.setFont(TEXT_FONT);
        jframe.add(c, constraints);

        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 5, 0, 5);
        c = new JTextField("");
        c.setFont(TEXT_FONT);
        jframe.add(c, constraints);

        constraints.weightx = 1.0;
        constraints.gridwidth = GridBagConstraints.RELATIVE;
        c = new JLabel("Sorte:");
        c.setFont(TEXT_FONT);
        jframe.add(c, constraints);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        c = new JLabel("Extras:");
        c.setFont(TEXT_FONT);
        jframe.add(c, constraints);

        constraints.fill = GridBagConstraints.NONE;
        constraints.gridwidth = GridBagConstraints.RELATIVE;
        c = new JCheckBox("Margherita");
        c.setFont(TEXT_FONT);
        jframe.add(c, constraints);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        c = new JCheckBox("Mais");
        c.setFont(TEXT_FONT);
        jframe.add(c, constraints);
        c = new JCheckBox("Schinken");
        c.setFont(TEXT_FONT);
        constraints.gridwidth = GridBagConstraints.RELATIVE;
        jframe.add(c, constraints);
        c = new JCheckBox("Pfeffersalami");
        c.setFont(TEXT_FONT);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        jframe.add(c, constraints);

        c = new JLabel("Anmerkungen:");
        c.setFont(TEXT_FONT);
        jframe.add(c, constraints);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridwidth = 1;
        constraints.gridheight = 2;
        constraints.insets = new Insets(0, 5, 0, 5);

        c = new JTextField();
        c.setFont(TEXT_FONT);
        jframe.add(c, constraints);

        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.gridheight = 1;
        c = new JCheckBox("Abholer");
        c.setFont(TEXT_FONT);
        jframe.add(c, constraints);
        c = new JButton("Löschen");
        c.setFont(TEXT_FONT);
        jframe.add(c, constraints);

        constraints.anchor = GridBagConstraints.CENTER;
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridwidth = GridBagConstraints.RELATIVE;
        constraints.insets = new Insets(20, 0, 0, 0);
        c = new JButton("OK");
        c.setFont(TEXT_FONT);
        jframe.add(c, constraints);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        c = new JButton("Abbruch");
        c.setFont(TEXT_FONT);
        jframe.add(c, constraints);
        jframe.pack();
        jframe.setLocationRelativeTo(null); // center on screen; close to setVisible

        return jframe;
    }
}
