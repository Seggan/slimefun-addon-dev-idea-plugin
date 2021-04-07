package io.github.seggan.slimefunaddonplugin;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import org.jetbrains.annotations.NotNull;

public class SlimefunGroup extends DefaultActionGroup {

    @Override
    public void update(@NotNull AnActionEvent e) {
        // e.getPresentation().setEnabledAndVisible(e.getData(CommonDataKeys.NAVIGATABLE) instanceof PsiDirectory);
    }
}
