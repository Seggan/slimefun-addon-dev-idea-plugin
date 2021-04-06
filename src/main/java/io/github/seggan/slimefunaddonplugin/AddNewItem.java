package io.github.seggan.slimefunaddonplugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierList;
import org.jetbrains.annotations.NotNull;

public class AddNewItem extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        NewItemDialog dialog = new NewItemDialog();
        if (dialog.showAndGet()) {
            WriteCommandAction.runWriteCommandAction(e.getProject(), () -> {
                PsiFile file = e.getDataContext().getData(CommonDataKeys.PSI_FILE);
                if (!(file instanceof PsiJavaFile)) {
                    dialog.close(DialogWrapper.CLOSE_EXIT_CODE, false);
                    new ErrorDialog("The file opened is not a Java file").show();
                    return;
                }
                PsiJavaFile javaFile = (PsiJavaFile) file;

                PsiClass[] classes = javaFile.getClasses();
                if (classes.length == 0 || classes[0] == null) {
                    dialog.close(DialogWrapper.CLOSE_EXIT_CODE, false);
                    new ErrorDialog("No Java classes found in the file").show();
                    return;
                }

                PsiClass mainClass = classes[0];

                PsiElementFactory factory = JavaPsiFacade.getInstance(e.getProject()).getElementFactory();

                PsiClassType sfisClass = factory.createTypeByFQClassName("me.mrCookieSlime.Slimefun.api.SlimefunItemStack");

                String id = dialog.getIdField().getText();

                PsiField field = factory.createField(id, sfisClass);
                PsiModifierList modifierList = field.getModifierList();
                modifierList.setModifierProperty(PsiModifier.PUBLIC, true);
                modifierList.setModifierProperty(PsiModifier.STATIC, true);
                modifierList.setModifierProperty(PsiModifier.FINAL, true);

                field.setInitializer(factory.createExpressionFromText(
                    "new SlimefunItemStack(\n" + id + ")", sfisClass.getPsiContext()));

                PsiMethod[] ctors = mainClass.getConstructors();
                if (ctors.length == 0) {
                    mainClass.add(field);
                } else {
                    mainClass.addAfter(field, mainClass.getConstructors()[0]);
                }

                dialog.close(DialogWrapper.OK_EXIT_CODE, true);
            });
        }
    }
}
