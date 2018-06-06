package microtrafficsim.core.vis.scenario.areas.ui;

import microtrafficsim.core.vis.glui.UIManager;
import microtrafficsim.core.vis.scenario.areas.Area;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;


public class PropertyFrame extends JFrame {

    private UIManager ui;
    private JComboBox<TypeComboBoxEntry> type;
    private JCheckBox monitored;
    private JPanel panel;

    private Collection<AreaComponent> areas = null;
    private Area.Type lastSelectedType = Area.Type.ORIGIN;
    private boolean lastSelectedMonitored = false;


    public PropertyFrame(UIManager ui) {
        super("Area Properties");
        this.ui = ui;
        setupUI();
        pack();
    }

    private void setupUI() {
        this.setLayout(new BorderLayout());

        panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(5, 2, 2, 2));
        panel.setLayout(new GridBagLayout());

        {
            JLabel typelabel = new JLabel("Type: ");
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.fill = GridBagConstraints.BOTH;

            panel.add(typelabel, gbc);
        }

        {
            final DefaultComboBoxModel<TypeComboBoxEntry> typemodel = new DefaultComboBoxModel<>();
            typemodel.addElement(new TypeComboBoxEntry(Area.Type.ORIGIN, "Origin"));
            typemodel.addElement(new TypeComboBoxEntry(Area.Type.DESTINATION, "Destination"));

            type = new JComboBox<>();
            type.setModel(typemodel);

            type.addActionListener(a -> {
                if (areas.isEmpty()) return;

                ui.getContext().addTask(c -> {
                    ArrayList<AreaComponent> selected = new ArrayList<>(areas);
                    Area.Type type = getSelectedType();

                    if (type != null) {
                        lastSelectedType = type;
                        for (AreaComponent area : selected)
                            area.setType(type);
                    }

                    return null;
                });
            });

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;

            panel.add(type, gbc);
        }

        {
            monitored = new JCheckBox("Is monitored?");
            monitored.setSelected(false);

            monitored.addActionListener(a -> {
                if (areas.isEmpty()) return;

                ui.getContext().addTask(c -> {
                    ArrayList<AreaComponent> selected = new ArrayList<>(areas);
                    boolean isMonitored = isSelectedMonitored();

                    lastSelectedMonitored = isMonitored;
                    for (AreaComponent area : selected)
                        area.setMonitored(isMonitored);

                    return null;
                });
            });

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 1;
            gbc.gridy = 1;
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;

            panel.add(monitored, gbc);
        }

        this.add(panel, BorderLayout.NORTH);
        this.setMinimumSize(new Dimension(250, 10));
    }


    public void setAreas(Collection<AreaComponent> areas) {
        this.areas = areas;

        if (areas.isEmpty()) {
            type.setSelectedIndex(-1);
            type.setEnabled(false);
            monitored.setSelected(false);
            monitored.setEnabled(false);

        } else {
            Iterator<AreaComponent> iter = areas.iterator();
            AreaComponent a = iter.next();

            boolean equal = true;
            while (iter.hasNext()) {
                if (a.getType() != iter.next().getType()) {
                    equal = false;
                    break;
                }
            }

            type.setEnabled(true);
            monitored.setEnabled(true);

            if (equal) {
                lastSelectedType = a.getType();
                type.setSelectedItem(new TypeComboBoxEntry(a.getType(), ""));

                lastSelectedMonitored = a.isMonitored();
                monitored.setSelected(a.isMonitored());
            } else {
                type.setSelectedIndex(-1);
                monitored.setSelected(false);
            }
        }
    }

    public Collection<AreaComponent> getAreas() {
        return areas;
    }

    public Area.Type getSelectedType() {
        TypeComboBoxEntry entry = (TypeComboBoxEntry) type.getSelectedItem();
        return entry != null ? entry.type : null;
    }

    public Area.Type getLastSelectedType() {
        return lastSelectedType;
    }

    public boolean isSelectedMonitored() {
        return monitored.isSelected();
    }

    public boolean isLastSelectedMonitored() {
        return lastSelectedMonitored;
    }


    private static class TypeComboBoxEntry {
        Area.Type type;
        String name;

        TypeComboBoxEntry(Area.Type type, String name) {
            this.type = type;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof TypeComboBoxEntry && ((TypeComboBoxEntry) obj).type == type;
        }
    }
}
