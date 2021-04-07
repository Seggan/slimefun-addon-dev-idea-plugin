package io.github.seggan.slimefunaddonplugin;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ErrorDialog extends DialogWrapper {

    private final String message;

    public ErrorDialog(String message) {
        super(true);
        this.message = message;

        this.init();
        this.setTitle("Error");
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return new JLabel(message);
    }
}
