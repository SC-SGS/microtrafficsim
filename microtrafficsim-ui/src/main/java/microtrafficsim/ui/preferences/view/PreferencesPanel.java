package microtrafficsim.ui.preferences.view;

import javax.swing.*;
import java.awt.*;


/**
 * @author Dominic Parga Cacheiro
 */
public abstract class PreferencesPanel extends JPanel implements PreferencesView {

    protected void addTextFieldToGridBagLayout(int row, String labeledText, JTextField tf) {
        GridBagConstraints constraints = new GridBagConstraints();


        /* JLabel */
        JLabel label = new JLabel(labeledText);
        label.setFont(PreferencesFrame.TEXT_FONT);

        constraints.gridx   = 0;
        constraints.gridy   = row;
        constraints.weightx = 0;
        constraints.anchor  = GridBagConstraints.WEST;
        constraints.insets  = new Insets(0, 0, 0, 5);
        add(label, constraints);


        /* JTextField */
        tf.setHorizontalAlignment(JTextField.RIGHT);
        tf.setColumns(14);

        constraints           = new GridBagConstraints();
        constraints.gridx     = 1;
        constraints.gridy     = row;
        constraints.weightx   = 0;
        constraints.gridwidth = 1;
        constraints.fill      = GridBagConstraints.HORIZONTAL;
        constraints.anchor    = GridBagConstraints.WEST;
        constraints.insets    = new Insets(0, 5, 0, 0);
        add(tf, constraints);


        /* gap panel */
        JPanel gap = new JPanel(null);
        constraints           = new GridBagConstraints();
        constraints.gridx     = 2;
        constraints.gridy     = row;
        constraints.weightx   = 1;
        constraints.gridwidth = 1;
        constraints.fill      = GridBagConstraints.HORIZONTAL;
        constraints.anchor    = GridBagConstraints.WEST;
        add(gap, constraints);
    }
}