package io.github.seggan.slimefunaddonplugin.inspections;

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiStatement;
import com.intellij.psi.PsiVariable;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class SlimefunItemNotRegistered extends AbstractBaseJavaLocalInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {

            @Override
            public void visitExpression(PsiExpression expression) {
                super.visitExpression(expression);
                if (!expression.getText().startsWith("new SlimefunItem") || expression.getText().contains(".register"))
                    return;

                PsiVariable stored = expression.getParent() instanceof PsiVariable ? (PsiVariable) expression.getParent() : null;
                PsiMethod method = PsiTreeUtil.getTopmostParentOfType(expression, PsiMethod.class);
                if (method == null) return;

                if (stored == null) {
                    if (!method.getText().contains(").register")) {
                        holder.registerProblem(expression, "Slimefun item not registered", new Fix(null));
                    }
                } else if (!method.getText().contains(stored.getName() + ".register")) {
                    holder.registerProblem(expression, "Slimefun item not registered", new Fix(stored));
                }
            }
        };
    }

    private static class Fix implements LocalQuickFix {

        @Nullable
        private final SmartPsiElementPointer<PsiVariable> var;

        private Fix(@Nullable PsiVariable var) {
            this.var = var == null ? null : SmartPointerManager.createPointer(var);
        }


        @Override
        public @IntentionFamilyName @NotNull String getFamilyName() {
            return "Register item";
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);
            if (var != null) {
                PsiVariable localVar = var.getElement();
                if (localVar == null) return;

                PsiStatement methodCall = factory.createStatementFromText("\n" + localVar.getName() + ".register();", localVar);

                localVar.getParent().addAfter(methodCall, localVar);
            } else {
                PsiElement element = descriptor.getPsiElement();
                PsiStatement methodCall = factory.createStatementFromText(".register()", element);
                element.add(methodCall);
            }
        }
    }

    @NotNull
    private static PsiClass findClass(String fqName, Project project) {
        return Objects.requireNonNull(JavaPsiFacade.getInstance(project).findClass(fqName, GlobalSearchScope.everythingScope(project)));
    }
}
