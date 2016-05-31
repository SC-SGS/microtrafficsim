package microtrafficsim.ui.preferences.impl;

import microtrafficsim.core.simulation.controller.configs.SimulationConfig;
import microtrafficsim.ui.preferences.ComponentId;
import microtrafficsim.ui.preferences.IPreferences;
import microtrafficsim.ui.preferences.PrefPanel;
import microtrafficsim.ui.preferences.PreferencesListener;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;

/**
 * @author Dominic Parga Cacheiro
 */
public class PrefFrame extends JFrame implements IPreferences {

    public static final Font HEADER_FONT = new Font("Arial", Font.BOLD, 14);
    public static final Font TEXT_FONT = new Font("Arial", Font.PLAIN, 14);

    private JPanel content, bottom;
    private Queue<PrefPanel> panels;
    private HashSet<PreferencesListener> listeners;

    public PrefFrame() {

        super("Simulation parameter settings");
        listeners = new HashSet<>();

        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                content = null;
                bottom = null;
                panels.clear();
                panels = null;
                listeners.clear();
                listeners = null;
            }
        });
//        setMinimumSize(new Dimension(550, 330));

        panels = new LinkedList<>();
        /* JFrame <- JScrollPane <- JPanel content <- JPanels top and bottom */
        content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        add(new JScrollPane(content), BorderLayout.CENTER);

        bottom = new JPanel(new GridBagLayout());
        add(bottom, BorderLayout.SOUTH);
    }

    public void addPrefPanel(PrefPanel panel) {
        panels.add(panel);
    }

    public void setEnabled(boolean enabled, ComponentId component) {
        for (PrefPanel panel : panels)
            panel.setEnabled(enabled, component);
    }

    /*
    |==================|
    | (i) IPreferences |
    |==================|
    */
    @Override
    public void initSettings() {
        panels.forEach(PrefPanel::initSettings);
    }

    @Override
    public void create() {

        for (PrefPanel panel : panels) {

            /* define margins and line */
            Border margins = BorderFactory.createEmptyBorder(10, 5, 10, 5);
            Border line = BorderFactory.createLineBorder(Color.black);

            /* set titled border */
            TitledBorder border = BorderFactory.createTitledBorder(
                    line,
                    panel.title);
            border.setTitleFont(HEADER_FONT);
            /* add */
            panel.setBorder(BorderFactory.createCompoundBorder(
                    margins, border));
            panel.create();
            content.add(panel);
        }

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        JPanel gap = new JPanel();
        bottom.add(gap, constraints);

        constraints = new GridBagConstraints();
        constraints.weightx = 0;
        JButton buAccept = new JButton("Accept");
        buAccept.addActionListener(e -> acceptChanges(new SimulationConfig()));
        bottom.add(buAccept, constraints);

        constraints = new GridBagConstraints();
        constraints.weightx = 0;
        JButton buCancel = new JButton("Cancel");
        buCancel.addActionListener(e -> cancelChanges());
        bottom.add(buCancel, constraints);
    }

    @Override
    public void addListener(PreferencesListener listener) {
        listeners.add(listener);
    }

    @Override
    public boolean acceptChanges(SimulationConfig newConfig) {
        boolean errorOccured = false;

        for (PrefPanel panel : panels)
            errorOccured |= panel.acceptChanges(newConfig);

        if (!errorOccured)
            for (PreferencesListener listener : listeners)
                listener.didAcceptChanges(newConfig);

        if (!errorOccured)
            dispose();

        return errorOccured;
    }

    @Override
    public void cancelChanges() {
        panels.forEach(PrefPanel::cancelChanges);
        dispose();
    }

    /**
     * TUTORIAL todo remove
     */
    public static JFrame create_demo() {

        JFrame jframe = new JFrame();
        jframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        jframe.setMinimumSize(new Dimension(500, 100));
        jframe.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        JComponent c;

        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.gridheight = 2;
        constraints.insets = new Insets(0, 5, 0, 5);
        JLabel header = new JLabel("Benno's Pizza Service");
        header.setFont(PrefFrame.HEADER_FONT);
        jframe.add(header, constraints);

        constraints.gridheight = 1;
        constraints.gridwidth = GridBagConstraints.RELATIVE;
        constraints.anchor = GridBagConstraints.WEST;
        c = new JLabel("Name:");
        c.setFont(PrefFrame.TEXT_FONT);
        jframe.add(c, constraints);

        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 5, 0, 5);
        c = new JTextField("");
        c.setFont(PrefFrame.TEXT_FONT);
        jframe.add(c, constraints);

        constraints.weightx = 1.0;
        constraints.gridwidth = GridBagConstraints.RELATIVE;
        c = new JLabel("Sorte:");
        c.setFont(PrefFrame.TEXT_FONT);
        jframe.add(c, constraints);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        c = new JLabel("Extras:");
        c.setFont(PrefFrame.TEXT_FONT);
        jframe.add(c, constraints);

        constraints.fill = GridBagConstraints.NONE;
        constraints.gridwidth = GridBagConstraints.RELATIVE;
        c = new JCheckBox("Margherita");
        c.setFont(PrefFrame.TEXT_FONT);
        jframe.add(c, constraints);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        c = new JCheckBox("Mais");
        c.setFont(PrefFrame.TEXT_FONT);
        jframe.add(c, constraints);
        c = new JCheckBox("Schinken");
        c.setFont(PrefFrame.TEXT_FONT);
        constraints.gridwidth = GridBagConstraints.RELATIVE;
        jframe.add(c, constraints);
        c = new JCheckBox("Pfeffersalami");
        c.setFont(PrefFrame.TEXT_FONT);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        jframe.add(c, constraints);

        c = new JLabel("Anmerkungen:");
        c.setFont(PrefFrame.TEXT_FONT);
        jframe.add(c, constraints);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridwidth = 1;
        constraints.gridheight = 2;
        constraints.insets = new Insets(0, 5, 0, 5);

        c = new JTextField();
        c.setFont(PrefFrame.TEXT_FONT);
        jframe.add(c, constraints);

        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.gridheight = 1;
        c = new JCheckBox("Abholer");
        c.setFont(PrefFrame.TEXT_FONT);
        jframe.add(c, constraints);
        c = new JButton("Löschen");
        c.setFont(PrefFrame.TEXT_FONT);
        jframe.add(c, constraints);

        constraints.anchor = GridBagConstraints.CENTER;
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridwidth = GridBagConstraints.RELATIVE;
        constraints.insets = new Insets(20, 0, 0, 0);
        c = new JButton("OK");
        c.setFont(PrefFrame.TEXT_FONT);
        jframe.add(c, constraints);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        c = new JButton("Abbruch");
        c.setFont(PrefFrame.TEXT_FONT);
        jframe.add(c, constraints);
        jframe.pack();
        jframe.setLocationRelativeTo(null); // center on screen; close to setVisible

        return jframe;
    }
}
