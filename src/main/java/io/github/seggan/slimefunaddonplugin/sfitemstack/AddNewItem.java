package io.github.seggan.slimefunaddonplugin.sfitemstack;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiImportList;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.search.GlobalSearchScope;
import io.github.seggan.slimefunaddonplugin.ErrorDialog;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class AddNewItem extends AnAction {

    private static final Pattern NEWLINE = Pattern.compile("\\n");

    private Project project = null;

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(e.getData(CommonDataKeys.PSI_FILE) instanceof PsiJavaFile);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        project = e.getProject();
        NewItemDialog dialog = new NewItemDialog();
        if (dialog.showAndGet()) {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                PsiFile file = e.getDataContext().getData(CommonDataKeys.PSI_FILE);
                PsiJavaFile javaFile = (PsiJavaFile) file;

                PsiClass[] classes = javaFile.getClasses();
                if (classes.length == 0 || classes[0] == null) {
                    new ErrorDialog("No Java classes found in the file").show();
                    return;
                }

                PsiClass mainClass = classes[0];

                PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();

                PsiClass sfisClass = findClass("me.mrCookieSlime.Slimefun.api.SlimefunItemStack");

                String id = dialog.getIdField().getText();

                PsiField field = factory.createField(id, factory.createTypeByFQClassName("me.mrCookieSlime.Slimefun.api.SlimefunItemStack"));
                PsiModifierList modifierList = field.getModifierList();
                modifierList.setModifierProperty(PsiModifier.PUBLIC, true);
                modifierList.setModifierProperty(PsiModifier.STATIC, true);
                modifierList.setModifierProperty(PsiModifier.FINAL, true);

                StringBuilder sb = new StringBuilder();
                sb.append("new SlimefunItemStack(\n\"");
                sb.append(id);
                sb.append("\",\nMaterial.");
                sb.append(dialog.getMaterialField().getText());
                sb.append(",\n\"");

                String name = dialog.getNameField().getText().strip();
                name = name.startsWith("&") ? name : "&f" + name;

                sb.append(name);
                sb.append("\"");
                String[] loreLines = NEWLINE.split(dialog.getLoreField().getText());
                if (loreLines.length > 1) {
                    sb.append(",\n\"");
                    for (int i = 0; i < loreLines.length - 1; i++) {
                        String loreLine = loreLines[i].strip();
                        loreLine = loreLine.startsWith("&") ? loreLine : "&7" + loreLine;
                        sb.append(loreLine);
                        sb.append("\",\n\"");
                    }
                    sb.append(loreLines[loreLines.length - 1]);
                    sb.append("\"");
                }
                sb.append("\n)");

                field.setInitializer(factory.createExpressionFromText(sb.toString(), sfisClass));

                PsiMethod[] ctors = mainClass.getConstructors();
                if (ctors.length == 0) {
                    mainClass.add(field);
                } else {
                    if (mainClass.getMethods().length == 0) {
                        mainClass.addAfter(field, mainClass.getConstructors()[0]);
                    } else {
                        mainClass.addBefore(field, mainClass.getConstructors()[0]);
                    }
                }

                PsiImportList imports = javaFile.getImportList();
                if (imports.findSingleImportStatement("org.bukkit.Material") == null) {
                    javaFile.importClass(findClass("org.bukkit.Material"));
                }
                if (imports.findSingleImportStatement("me.mrCookieSlime.Slimefun.api.SlimefunItemStack") == null) {
                    javaFile.importClass(findClass("me.mrCookieSlime.Slimefun.api.SlimefunItemStack"));
                }
            });
        }
    }

    @NotNull
    private PsiClass findClass(String fqName) {
        return JavaPsiFacade.getInstance(project).findClass(fqName, GlobalSearchScope.everythingScope(project));
    }
}
