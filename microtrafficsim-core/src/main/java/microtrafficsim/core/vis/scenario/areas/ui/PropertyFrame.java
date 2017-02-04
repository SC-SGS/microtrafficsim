package microtrafficsim.core.vis.scenario.areas.ui;

import microtrafficsim.core.vis.glui.UIManager;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;


public class PropertyFrame extends JFrame {

    private UIManager ui;
    private JComboBox<TypeComboBoxEntry> type;
    private JPanel panel;

    private Collection<AreaComponent> areas = null;
    private AreaComponent.Type lastSelectedType = AreaComponent.Type.ORIGIN;


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
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.fill = GridBagConstraints.BOTH;

            panel.add(typelabel, gbc);
        }

        {
            final DefaultComboBoxModel<TypeComboBoxEntry> typemodel = new DefaultComboBoxModel<>();
            typemodel.addElement(new TypeComboBoxEntry(AreaComponent.Type.ORIGIN, "Origin"));
            typemodel.addElement(new TypeComboBoxEntry(AreaComponent.Type.DESTINATION, "Destination"));

            type = new JComboBox<>();
            type.setModel(typemodel);

            type.addActionListener(a -> {
                if (areas.isEmpty()) return;

                ui.getContext().addTask(c -> {
                    ArrayList<AreaComponent> selected = new ArrayList<>(areas);
                    AreaComponent.Type type = getSelectedType();

                    if (type != null) {
                        lastSelectedType = type;
                        for (AreaComponent area : selected)
                            area.setType(type);
                    }

                    return null;
                });
            });

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;

            panel.add(type, gbc);
        }

        this.add(panel, BorderLayout.NORTH);
        this.setMinimumSize(new Dimension(250, 10));
    }


    public void setAreas(Collection<AreaComponent> areas) {
        this.areas = areas;

        if (areas.isEmpty()) {
            type.setSelectedIndex(-1);
            type.setEnabled(false);

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

            if (equal) {
                lastSelectedType = a.getType();
                type.setSelectedItem(new TypeComboBoxEntry(a.getType(), ""));
            } else {
                type.setSelectedIndex(-1);
            }
        }
    }

    public Collection<AreaComponent> getAreas() {
        return areas;
    }

    public AreaComponent.Type getSelectedType() {
        TypeComboBoxEntry entry = (TypeComboBoxEntry) type.getSelectedItem();
        return entry != null ? entry.type : null;
    }

    public AreaComponent.Type getLastSelectedType() {
        return lastSelectedType;
    }


    private static class TypeComboBoxEntry {
        AreaComponent.Type type;
        String name;

        TypeComboBoxEntry(AreaComponent.Type type, String name) {
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
