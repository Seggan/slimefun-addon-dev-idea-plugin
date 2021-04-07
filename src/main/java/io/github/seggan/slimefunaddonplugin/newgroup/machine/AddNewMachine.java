package io.github.seggan.slimefunaddonplugin.newgroup.machine;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiType;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

public class AddNewMachine extends AnAction {

    JavaPsiFacade facade = null;
    Project project = null;

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        project = e.getProject();
        assert project != null;

        NewMachineDialog dialog = new NewMachineDialog();
        if (dialog.showAndGet()) {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                Navigatable navigatable = e.getData(CommonDataKeys.NAVIGATABLE);
                if (!(navigatable instanceof PsiDirectory)) return;

                PsiDirectory directory = (PsiDirectory) navigatable;
                facade = JavaPsiFacade.getInstance(project);
                PsiElementFactory factory = facade.getElementFactory();

                String path = directory.getPresentation().getLocationString();
                String className = dialog.getClassNameField().getText();

                // PsiFile file = factory.createFileFromText(className + ".java", JavaLanguage.INSTANCE, String.format(s.replace("\r", ""), path, className));

                PsiClass created = JavaDirectoryService.getInstance().createClass(directory, className);

                PsiMethod ctor = factory.createMethodFromText("public " + className + "(Category category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {\n" +
                    "super(category, item, recipeType, recipe);\n}", created);
                created.add(ctor);

                PsiMethod method = factory.createMethod("registerDefaultRecipes", PsiType.VOID);
                PsiModifierList modifiers = method.getModifierList();
                modifiers.addAnnotation("java.lang.Override");
                created.add(method);

                method = factory.createMethodFromText("@Override\npublic ItemStack getProgressBar() {\n" +
                    "return null;\n}", findClass("org.bukkit.inventory.ItemStack"));
                created.add(method);

                method = factory.createMethodFromText("@Override\npublic String getMachineIdentifier() {\n" +
                    "return null;\n}", findClass("java.lang.String"));
                created.add(method);

                PsiJavaFile file = (PsiJavaFile) created.getContainingFile();
                file.importClass(findClass("me.mrCookieSlime.Slimefun.Lists.RecipeType"));
                file.importClass(findClass("me.mrCookieSlime.Slimefun.Objects.Category"));
                file.importClass(findClass("me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.AContainer"));
                file.importClass(findClass("me.mrCookieSlime.Slimefun.api.SlimefunItemStack"));
                file.importClass(findClass("org.bukkit.inventory.ItemStack"));

                PsiDocumentManager manager = PsiDocumentManager.getInstance(project);
                Document document = manager.getDocument(file);
                manager.doPostponedOperationsAndUnblockDocument(document);
                document.setText(document.getText().replaceFirst(className, className + " extends AContainer"));
            });
        }
    }

    @NotNull
    private PsiClass findClass(String fqName) {
        return facade.findClass(fqName, GlobalSearchScope.everythingScope(project));
    }
}
