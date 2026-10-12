package com.tastybite.ui;

import com.tastybite.service.ServiceException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Window;

/** Popup for creating or renaming a menu section such as "Desserts". */
public class SectionDialog extends JDialog {

    /** What happens on confirm. Throws ServiceException to show a message and stay open. */
    public interface Saver {
        void save(String name) throws ServiceException;
    }

    private final JTextField nameField = new JTextField();
    private final JLabel error = UiTheme.errorLabel();
    private boolean saved;

    /** existingName is null when creating a new section. */
    public SectionDialog(Window owner, String title, String existingName, Saver saver) {
        super(owner, title, ModalityType.APPLICATION_MODAL);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(22, 28, 20, 28));

        JLabel heading = new JLabel(title);
        heading.setFont(UiTheme.HEADING);
        heading.setForeground(UiTheme.TEXT);
        heading.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        content.add(heading);
        content.add(Box.createVerticalStrut(14));

        if (existingName != null) nameField.setText(existingName);
        UiTheme.addField(content, "Section name (for example Desserts)", nameField);
        content.add(error);
        content.add(Box.createVerticalStrut(8));

        JButton confirm = UiTheme.primaryButton(existingName == null ? "Add" : "Save");
        confirm.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        confirm.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        JButton cancel = UiTheme.linkButton("Cancel");
        cancel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        content.add(confirm);
        content.add(Box.createVerticalStrut(4));
        content.add(cancel);

        confirm.addActionListener(e -> {
            error.setText(" ");
            try {
                saver.save(nameField.getText());
                saved = true;
                dispose();
            } catch (ServiceException ex) {
                error.setText(ex.getMessage());
            }
        });
        cancel.addActionListener(e -> dispose());

        setContentPane(content);
        getRootPane().setDefaultButton(confirm);
        setResizable(false);
        pack();
        setMinimumSize(new Dimension(380, getHeight()));
        setLocationRelativeTo(owner);
    }

    public boolean wasSaved() { return saved; }
}
