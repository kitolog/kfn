import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.impl.VirtualDirectoryImpl;
import com.intellij.openapi.vfs.newvfs.impl.VirtualFileImpl;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
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
    protected List<ResolveResult> resultsList;

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

        this.resultsList = new ArrayList<ResolveResult>();
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

    @NotNull
    @Override
    public Object[] getVariants()
    {
        Object[] results;
        List<LookupElement> resultFiles = new ArrayList<LookupElement>();
        String searchPath = preparePath(path, false, true);
        String[] pathParts = searchPath.split("/");
        if (pathParts.length > 0)
        {
            pathParts[pathParts.length - 1] = pathParts[pathParts.length - 1].replace(".php", "")
                    .replace("intellijidearulezzz ", "")
                    .replace("Intellijidearulezzz ", "");

            //remove last element if it's empty
            if (pathParts[pathParts.length - 1].equals(""))
            {
                pathParts = Arrays.copyOf(ArrayUtils.removeElement(pathParts, pathParts[pathParts.length - 1]), ArrayUtils.removeElement(pathParts, pathParts[pathParts.length - 1]).length, String[].class);
            }

            for (VirtualFile directory : resultDirs)
            {
                for (int i = 0; i < pathParts.length; i++)
                {
                    VirtualFile subDir = directory.findFileByRelativePath(pathParts[i]);
                    if (subDir != null && subDir.isDirectory())
                    {
                        directory = subDir;
                    }
                }

                String preparedFileName = pathParts[pathParts.length - 1];

                if (directory.getName()
                        .contains(preparedFileName))
                {
                    preparedFileName = "";
                }

                ArrayList<String> allFiles = getAllFilesInDirectory(directory, null);
                String foundFile;
                String psiFileNamePrefix = "";
                for (int i = 0; i < (preparedFileName.equals("") ? pathParts.length : pathParts.length - 1); i++)
                {
                    psiFileNamePrefix += pathParts[i] + "_";
                }

                for (int i = 0; i < allFiles.size(); i++)
                {
                    foundFile = allFiles.get(i);
                    if (foundFile.contains(preparedFileName) && foundFile.endsWith(".php"))
                    {
                        VirtualFile foundVirtualFile = directory.findFileByRelativePath(foundFile);
                        if (foundVirtualFile != null)
                        {
                            PsiFile foundPsiFile = PsiManager.getInstance(project)
                                    .findFile(foundVirtualFile);

                            if (foundPsiFile != null)
                            {
                                String psiFileName = psiFileNamePrefix + foundFile.replace("/", "_")
                                        .replace(".php", "");


                                TemplateLookupElement lookup = new TemplateLookupElement(psiFileName, foundPsiFile);
                                Boolean exists = false;

                                for (LookupElement resultFile : resultFiles)
                                {
                                    if (resultFile.getLookupString()
                                            .equals(lookup.getLookupString()))
                                    {
                                        exists = true;
                                    }
                                }

                                if (!exists)
                                {
                                    resultFiles.add(lookup);
                                }
                            }
                        }
                    }
                }
            }
        }

        results = resultFiles.toArray();

        return results;
    }

    protected ArrayList<String> getAllFilesInDirectory(VirtualFile directory, @Nullable String parentName)
    {
        ArrayList<String> files = new ArrayList<String>();

        VirtualFile[] children = directory.getChildren();
        if (children.length != 0)
        {
            for (int i = 0; i < children.length; i++)
            {
                String childName = "";
                if (children[i] instanceof VirtualDirectoryImpl)
                {
                    files.addAll(getAllFilesInDirectory(children[i], children[i].getName()));
                }
                else if (children[i] instanceof VirtualFileImpl)
                {
                    if (parentName != null)
                    {
                        childName += parentName + "/";
                    }
                    childName += children[i].getPath()
                            .replace(directory.getPath() + "/", "");

                    files.add(childName);
                }
            }
        }

        return files;
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
        if (resolveResultElement != null && resultsList.indexOf(resolveResultElement) < 0)
        {
            resultsList.add(resolveResultElement);
        }
    }

    protected List<ResolveResult> getResultElement()
    {
        return resultsList;
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

        if (addSearch == false)
        {
            pathString += ".php";
        }

        return pathString;
    }
}
