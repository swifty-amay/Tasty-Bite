package com.tastybite.ui;

import com.tastybite.model.MenuItem;
import com.tastybite.service.ServiceException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.io.File;

/**
 * The small popup from the sketch: Item Name, Price and an Add button (Save when editing),
 * plus an optional photo. Errors from the backend are shown inside the popup.
 */
public class ItemDialog extends JDialog {

    /** What happens when the person confirms. Throws ServiceException to show a message and stay open. */
    public interface Saver {
        void save(String name, String price, File newImage, boolean removeImage) throws ServiceException;
    }

    private final JTextField nameField = new JTextField();
    private final JTextField priceField = new JTextField();
    private final JLabel photoLabel = new JLabel();
    private final JButton removePhoto = UiTheme.linkButton("Remove");
    private final JLabel error = UiTheme.errorLabel();
    private final boolean hasExistingPhoto;

    private File chosenImage;
    private boolean removeImage;
    private boolean saved;

    public ItemDialog(Window owner, String title, String sectionName, MenuItem existing, Saver saver) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        hasExistingPhoto = existing != null && existing.imagePath() != null && !existing.imagePath().isBlank();

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(22, 28, 20, 28));

        JLabel heading = new JLabel(title);
        heading.setFont(UiTheme.HEADING);
        heading.setForeground(UiTheme.TEXT);
        heading.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        JLabel section = new JLabel("Section: " + sectionName);
        section.setFont(UiTheme.FONT);
        section.setForeground(UiTheme.MUTED);
        section.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        content.add(heading);
        content.add(section);
        content.add(Box.createVerticalStrut(16));

        if (existing != null) {
            nameField.setText(existing.name());
            priceField.setText(existing.price().toPlainString());
        }
        UiTheme.addField(content, "Item Name", nameField);
        UiTheme.addField(content, "Price", priceField);

        // photo row
        JLabel photoTitle = new JLabel("Photo (optional)");
        photoTitle.setFont(UiTheme.FONT_BOLD);
        photoTitle.setForeground(UiTheme.TEXT);
        photoTitle.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        content.add(photoTitle);
        content.add(Box.createVerticalStrut(4));

        JButton choose = UiTheme.linkButton("Choose image...");
        photoLabel.setFont(UiTheme.FONT_SMALL);
        photoLabel.setForeground(UiTheme.MUTED);
        JPanel photoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        photoRow.setOpaque(false);
        photoRow.add(choose);
        photoRow.add(photoLabel);
        photoRow.add(removePhoto);
        photoRow.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        photoRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        content.add(photoRow);
        content.add(Box.createVerticalStrut(8));
        updatePhotoRow();

        choose.addActionListener(e -> chooseImage());
        removePhoto.addActionListener(e -> {
            chosenImage = null;
            removeImage = hasExistingPhoto;
            updatePhotoRow();
        });

        content.add(error);
        content.add(Box.createVerticalStrut(8));

        JButton confirm = UiTheme.primaryButton(existing == null ? "Add" : "Save");
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
                saver.save(nameField.getText(), priceField.getText(), chosenImage, removeImage);
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

    private void chooseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose a photo");
        chooser.setFileFilter(new FileNameExtensionFilter("Images (JPG, PNG, GIF)", "jpg", "jpeg", "png", "gif"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            chosenImage = chooser.getSelectedFile();
            removeImage = false;
            updatePhotoRow();
        }
    }

    private void updatePhotoRow() {
        if (chosenImage != null) {
            photoLabel.setText("  " + chosenImage.getName() + "  ");
        } else if (removeImage) {
            photoLabel.setText("  No photo  ");
        } else {
            photoLabel.setText(hasExistingPhoto ? "  Current photo  " : "  No photo  ");
        }
        removePhoto.setVisible(chosenImage != null || (hasExistingPhoto && !removeImage));
        photoLabel.revalidate();
    }
}
