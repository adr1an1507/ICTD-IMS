/*
 * ICTD IMS - FORM DIALOG
 *
 * A small pop-up form with one text box for each label.
 * Used to add or edit a log entry and to enter the attendance details.
 */
package frontend.components;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public final class Formdialog {

    private Formdialog () {
        // Only a static method, so nobody needs to create an object
    }

    /**
     * Shows the form and waits until the user clicks OK or Cancel.
     *
     * @param labels        the name of each field
     * @param optional      true for fields that may stay empty (shown as "(optional)")
     * @param initialValues the text to start with in each box
     * @return the typed values, or null if the user cancelled
     */
    public static String[] show(Component parent, String title, String[] labels,
                                boolean[] optional, String[] initialValues) {
        JTextField[] fields = new JTextField[labels.length];

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        for (int i = 0; i < labels.length; i++) {
            String labelText = optional[i] ? labels[i] + " (optional)" : labels[i];
            JLabel label = new JLabel(labelText);
            label.setFont(new Font("SansSerif", Font.BOLD, 13));

            fields[i] = new JTextField(initialValues[i], 24);
            fields[i].setFont(new Font("SansSerif", Font.PLAIN, 13));

            GridBagConstraints labelConstraints = new GridBagConstraints();
            labelConstraints.gridx = 0;
            labelConstraints.gridy = i;
            labelConstraints.anchor = GridBagConstraints.WEST;
            labelConstraints.insets = new Insets(4, 0, 4, 12);
            panel.add(label, labelConstraints);

            GridBagConstraints fieldConstraints = new GridBagConstraints();
            fieldConstraints.gridx = 1;
            fieldConstraints.gridy = i;
            fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
            fieldConstraints.weightx = 1.0;
            fieldConstraints.insets = new Insets(4, 0, 4, 0);
            panel.add(fields[i], fieldConstraints);
        }

        int choice = JOptionPane.showConfirmDialog(parent, panel, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) {
            return null; // cancelled
        }

        String[] values = new String[labels.length];
        for (int i = 0; i < labels.length; i++) {
            values[i] = fields[i].getText();
        }
        return values;
    }
}