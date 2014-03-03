import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class KfnPolyVariantReference implements PsiPolyVariantReference {
    protected PsiElement element;
    protected TextRange textRange;
    protected Project project;
    protected String path;
    protected String requestAction;
    protected Boolean kohanaPSR;
    protected Boolean debugMode;
    protected KohanaClassesState.KohanaClass KohanaClass;
    protected ArrayList<VirtualFile> resultDirs;
    protected List<ResolveResult> resolveResultsList;

    public KfnPolyVariantReference(String path, PsiElement element, TextRange textRange, Project project, ArrayList<VirtualFile> resultDirs, KohanaClassesState.KohanaClass kc, Boolean kohanaPSR, Boolean debugMode)
    {
        this.element = element;
        this.textRange = textRange;
        this.project = project;
        this.path = path;
        this.resultDirs = resultDirs;
        this.KohanaClass = kc;
        this.kohanaPSR = kohanaPSR;
        this.debugMode = debugMode;

        this.resolveResultsList = new ArrayList<ResolveResult>();
        this.findPsiElements();
    }

    @Override
    public String toString()
    {
        return getCanonicalText();
    }

    public PsiElement getElement()
    {
        return this.element;
    }

    public TextRange getRangeInElement()
    {
        return textRange;
    }

    @Nullable
    @Override
    public PsiElement resolve()
    {
        return null;
    }

    public PsiElement handleElementRename(String newElementName)
            throws IncorrectOperationException
    {
        // TODO: Implement this method
        throw new IncorrectOperationException();
    }

    public PsiElement bindToElement(PsiElement element) throws IncorrectOperationException
    {
        // TODO: Implement this method
        throw new IncorrectOperationException();
    }

    public boolean isReferenceTo(PsiElement element)
    {
        return resolve() == element;
    }

    public Object[] getVariants()
    {
        LookupElement[] results = new LookupElement[0];
        List<ResolveResult> resultsList = getResultElement();
        if (resultsList != null && resultsList.size() > 0)
        {
            results = resultsList.toArray(new LookupElement[resultsList.size()]);
        }

        return results;
    }

    @Override
    public String getCanonicalText()
    {
        return path;
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean b)
    {
        ResolveResult[] resultMultiResolve = new ResolveResult[]{};
        List<ResolveResult> resultsList = getResultElement();
        if (resultsList != null && resultsList.size() > 0)
        {
            resultMultiResolve = resultsList.toArray(new ResolveResult[resultsList.size()]);
        }

        return resultMultiResolve;
    }

    public boolean isSoft()
    {
        return false;
    }


    @Nullable
    public void findPsiElements()
    {
        try
        {
            String originalPath = preparePath(path, false, false);
            String searchPath = preparePath(path, false, true);
            path = preparePath(path, true, false);
            ResolveResult resolveResultElement;
            for (VirtualFile directory : resultDirs)
            {
                VirtualFile targetFile = directory.findFileByRelativePath(path);
                if (targetFile != null)
                {
                    resolveResultElement = null;
                    PsiFile file = PsiManager.getInstance(project)
                            .findFile(targetFile);

                    if (file != null)
                    {
                        if (requestAction != null && !requestAction.isEmpty())
                        {
                            PsiViewerTreeModel tree = new PsiViewerTreeModel(file);
                            PsiElement method = tree.getMethod(requestAction);
                            if (method != null)
                            {
                                resolveResultElement = new PsiElementResolveResult(method);
                            }
                            else
                            {
                                resolveResultElement = new PsiElementResolveResult(file);
                            }
                        }
                        else
                        {
                            resolveResultElement = new PsiElementResolveResult(file);
                        }
                    }

                    setResultElement(resolveResultElement);
                }

                VirtualFile originalTargetFile = directory.findFileByRelativePath(originalPath);
                if (originalTargetFile != null)
                {
                    resolveResultElement = null;
                    PsiFile originalFile = PsiManager.getInstance(project)
                            .findFile(originalTargetFile);

                    resolveResultElement = new PsiElementResolveResult(originalFile);

                    setResultElement(resolveResultElement);
                }

                /**
                 * @TODO: implement search by part of name
                 */
//                VirtualFile searchTargetFile = directory.findFileByRelativePath(searchPath);
//                if (searchTargetFile != null)
//                {
//                    resolveResultElement = null;
//                    PsiFile searchFile = PsiManager.getInstance(project)
//                            .findFile(searchTargetFile);
//
//                    resolveResultElement = new PsiElementResolveResult(searchFile);
//
//                    setResultElement(resolveResultElement);
//                }
            }
        }
        catch (Exception e)
        {
            String message = e.getMessage();
            if (debugMode)
            {
                Notifications.Bus.notify(new Notification("KohanaFactoryNavigator", "KfnPolyVariantReference findPsiElements", "Exception: " + message, NotificationType.ERROR));
                Notifications.Bus.notify(new Notification("KohanaFactoryNavigator", "KfnPolyVariantReference findPsiElements", "Exception: " + e.getStackTrace(), NotificationType.ERROR));
            }
        }

        if (debugMode)
        {
            Notifications.Bus.notify(new Notification("KohanaFactoryNavigator", "path", path, NotificationType.WARNING));
            Notifications.Bus.notify(new Notification("KohanaFactoryNavigator", "targetFile", "not exists", NotificationType.WARNING));
        }
    }

    protected void setResultElement(ResolveResult resolveResultElement)
    {
        if (resolveResultElement != null && resolveResultsList.indexOf(resolveResultElement) < 0)
        {
            resolveResultsList.add(resolveResultElement);
        }
    }

    protected List<ResolveResult> getResultElement()
    {
        return resolveResultsList;
    }

    public String preparePath(String pathString, Boolean replaceParts, Boolean addSearch)
    {
        pathString = pathString.toLowerCase();
        if (KohanaClass.regexp != null && replaceParts == true)
        {
            String[] regexpParts = StringUtils.split(KohanaClass.regexp, '|');
            if (regexpParts.length > 0)
            {
                for (String regexp : regexpParts)
                {
                    pathString = pathString.replace(regexp.toLowerCase(), "");
                }
            }
        }

        if (KohanaClass.name.equals("request"))
        {
            String[] parts = pathString.split("/");
            if (parts.length > 1)
            {
                requestAction = parts[1];
            }
            pathString = parts[0].toLowerCase()
                    .replace('_', '/');
        }
        else if (KohanaClass.name.equals("config"))
        {
            String[] parts = pathString.split("\\.");
            pathString = parts[0].toLowerCase();
        }
        else if (!KohanaClass.name.equals("view"))
        {
            pathString = pathString.toLowerCase()
                    .replace('_', '/');
        }

        if (!KohanaClass.name.equals("view") && !KohanaClass.name.equals("config") && kohanaPSR)
        {
            String[] path_parts = pathString.split("/");
            String prepared = "";
            for (String part : path_parts)
            {
                if (prepared.length() > 0)
                {
                    prepared += "/";
                }
                prepared += part.substring(0, 1)
                        .toUpperCase() + part.substring(1);
            }

            pathString = prepared;
        }

        if (addSearch == true)
        {
            pathString += "*";
        }

        pathString += ".php";

        return pathString;
    }
}
