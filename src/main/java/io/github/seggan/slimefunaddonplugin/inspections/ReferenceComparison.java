package io.github.seggan.slimefunaddonplugin.inspections;

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.JavaTokenType;
import com.intellij.psi.PsiBinaryExpression;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiPrefixExpression;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.PsiType;
import com.intellij.psi.tree.IElementType;
import com.siyeh.ig.psiutils.ExpressionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ReferenceComparison extends AbstractBaseJavaLocalInspectionTool {

    private static final Fix FIX = new Fix();

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitBinaryExpression(PsiBinaryExpression expression) {
                super.visitBinaryExpression(expression);
                IElementType opSign = expression.getOperationTokenType();
                if (opSign == JavaTokenType.EQEQ || opSign == JavaTokenType.NE) {
                    // The binary expression is the correct type for this inspection
                    PsiExpression lOperand = expression.getLOperand();
                    PsiExpression rOperand = expression.getROperand();
                    if (rOperand == null || ExpressionUtils.isNullLiteral(lOperand) || ExpressionUtils.isNullLiteral(rOperand)) {
                        return;
                    }
                    // Nothing is compared to null, now check the types being compared
                    PsiType lType = lOperand.getType();
                    PsiType rType = rOperand.getType();

                    if (!(lType instanceof PsiClassType) || !(rType instanceof PsiClassType)) return;
                    holder.registerProblem(expression, "'==' or '!=' used to compare references", FIX);
                }
            }

            @Override
            public void visitReferenceExpression(PsiReferenceExpression expression) {
            }
        };
    }

    @Override
    public @Nullable JComponent createOptionsPanel() {
        return null;
    }

    private static class Fix implements LocalQuickFix {

        @Override
        public @IntentionFamilyName @NotNull String getFamilyName() {
            return "Fix reference comparison";
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiBinaryExpression comparison = (PsiBinaryExpression) descriptor.getPsiElement();
            IElementType operation = comparison.getOperationTokenType();
            PsiExpression left = comparison.getLOperand();
            PsiExpression right = comparison.getROperand();
            if (right == null) {
                return;
            }

            PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
            PsiMethodCallExpression expression =
                (PsiMethodCallExpression) factory.createExpressionFromText("a.equals(b)", null);

            expression.getMethodExpression().getQualifierExpression().replace(left);
            expression.getArgumentList().getExpressions()[0].replace(right);

            PsiExpression result = (PsiExpression) comparison.replace(expression);

            if (operation == JavaTokenType.NE) {
                PsiPrefixExpression negation = (PsiPrefixExpression) factory.createExpressionFromText("!a", null);
                negation.getOperand().replace(result);
                result.replace(negation);
            }
        }
    }
}
