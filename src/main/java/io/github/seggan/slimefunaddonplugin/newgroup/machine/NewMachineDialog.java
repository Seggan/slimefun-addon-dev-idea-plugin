package io.github.seggan.slimefunaddonplugin.newgroup.machine;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.regex.Pattern;

class NewMachineDialog extends DialogWrapper {

    private static final Pattern DIGIT = Pattern.compile("\\d");

    private final JTextField classNameField = new JTextField();

    NewMachineDialog() {
        super(true);

        this.init();
        this.setTitle("Add a New Machine");
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Class Name:"));
        panel.add(classNameField);

        return panel;
    }

    @Override
    protected @Nullable ValidationInfo doValidate() {
        if (classNameField.getText().contains(" ")) {
            return new ValidationInfo("Invalid class name", classNameField);
        }

        return null;
    }

    public JTextField getClassNameField() {
        return classNameField;
    }
}
