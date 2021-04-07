package io.github.seggan.slimefunaddonplugin;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class NewItemDialog extends DialogWrapper {

    private static final Pattern SCREAMING_SNAKE = Pattern.compile("[A-Z_]+");
    private static final Pattern NEWLINE = Pattern.compile("\\n");

    private static final Set<String> materials = new HashSet<>();

    static {
        try (InputStream stream = NewItemDialog.class.getClassLoader().getResourceAsStream("ids.txt")) {
            materials.addAll(Arrays.asList(NEWLINE.split(new String(stream.readAllBytes(), StandardCharsets.UTF_8))));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private final JTextField idField = new JTextField();
    private final JTextField materialField = new JTextField();
    private final JTextField nameField = new JTextField();
    private final JTextArea loreField = new JTextArea();
    private final JCheckBox insertWhiteSpace = new JCheckBox();

    public NewItemDialog() {
        super(true);

        this.init();
        this.setTitle("Create a Slimefun Item");
        this.setSize(600, 500);
        this.setOKButtonText("Create");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel dialogPanel = new JPanel();
        dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.PAGE_AXIS));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEADING, 3, 3));

        JPanel individual = new JPanel();

        JLabel label = new JLabel("Id:");
        individual.add(label);
        individual.add(idField);
        top.add(individual);

        individual = new JPanel();

        label = new JLabel("Material:");
        individual.add(label);
        individual.add(materialField);
        top.add(individual);

        individual = new JPanel();

        label = new JLabel("Name:");
        individual.add(label);
        individual.add(nameField);
        top.add(individual);

        individual = new JPanel();

        label = new JLabel("Insert Leading Whitespace in Lore");
        insertWhiteSpace.setSelected(true);
        individual.add(insertWhiteSpace);
        individual.add(label);
        top.add(individual);

        top.setAlignmentX(Component.CENTER_ALIGNMENT);
        dialogPanel.add(top);

        label = new JLabel("Lore");
        dialogPanel.add(label);
        loreField.setPreferredSize(new Dimension(100, 100));
        dialogPanel.add(loreField);

        return dialogPanel;
    }

    @Override
    protected @Nullable ValidationInfo doValidate() {
        if (!SCREAMING_SNAKE.matcher(idField.getText()).matches()) {
            return new ValidationInfo("Id must be in SCREAMING_SNAKE_CASE", idField);
        }

        if (!materials.contains(materialField.getText())) {
            return new ValidationInfo("Unknown material: " + materialField.getText(), materialField);
        }

        return null;
    }

    public JTextField getIdField() {
        return idField;
    }

    public JTextField getMaterialField() {
        return materialField;
    }

    public JTextField getNameField() {
        return nameField;
    }

    public JTextArea getLoreField() {
        return loreField;
    }

    public JCheckBox getInsertWhiteSpace() {
        return insertWhiteSpace;
    }
}
