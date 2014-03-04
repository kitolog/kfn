import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class TemplateLookupElement extends LookupElement {

    private String templateName;
    private PsiFile psiFile;
    private PsiElement psiElement = null;

    @Nullable
    private InsertHandler<LookupElement> insertHandler = null;

    public TemplateLookupElement(String templateName, PsiFile psiFile)
    {
        this.templateName = templateName;
        this.psiFile = psiFile;
    }

    public TemplateLookupElement(String templateName, PsiFile psiFile, PsiElement psiElement, InsertHandler<LookupElement> insertHandler)
    {
        this.templateName = templateName;
        this.psiFile = psiFile;
        this.insertHandler = insertHandler;
        this.psiElement = psiElement;
    }

    @NotNull
    @Override
    public String getLookupString()
    {
        return templateName;
    }

    @NotNull
    public Object getObject()
    {
        return this.psiElement != null ? this.psiElement : super.getObject();
    }

    public void handleInsert(InsertionContext context)
    {
        if (this.insertHandler != null)
        {
            this.insertHandler.handleInsert(context, this);
        }
    }

    public void renderElement(LookupElementPresentation presentation)
    {
        presentation.setItemText(getLookupString());
        presentation.setIcon(this.psiFile.getIcon(Iconable.ICON_FLAG_VISIBILITY));
        presentation.setTypeText(VfsUtil.getRelativePath(psiFile.getContainingDirectory()
                .getVirtualFile(), psiFile.getProject()
                .getBaseDir(), '/'));
        presentation.setTypeGrayed(true);
    }

}