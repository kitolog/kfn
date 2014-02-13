import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.IncorrectOperationException;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.List;

import com.intellij.psi.search.SearchScope;
import org.apache.commons.lang.StringUtils;
import org.apache.xmlbeans.XmlToken;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.apache.commons.lang.WordUtils;

import java.util.ArrayList;

public class MyReference implements PsiReference {
    protected PsiElement element;
    protected TextRange textRange;
    protected Project project;
    protected String path;
    protected String requestAction;
    protected Boolean kohanaPSR;
    protected Boolean debugMode;
    protected KohanaClassesState.KohanaClass KohanaClass;
    protected ArrayList<VirtualFile> resultDirs;
    protected List<KohanaClassesState.KohanaClass> classesList;

    public MyReference(String path, PsiElement element, TextRange textRange, Project project, ArrayList<VirtualFile> resultDirs, KohanaClassesState.KohanaClass kc, Boolean kohanaPSR, Boolean debugMode) {
        this.element = element;
        this.textRange = textRange;
        this.project = project;
        this.path = path;
        this.resultDirs = resultDirs;
        this.KohanaClass = kc;
        this.kohanaPSR = kohanaPSR;
        this.debugMode = debugMode;
    }

    @Override
    public String toString() {
        return getCanonicalText();
    }

    public PsiElement getElement() {
        return this.element;
    }

    public TextRange getRangeInElement() {
        return textRange;
    }

    public PsiElement handleElementRename(String newElementName)
            throws IncorrectOperationException {
        // TODO: Implement this method
        throw new IncorrectOperationException();
    }

    public PsiElement bindToElement(PsiElement element) throws IncorrectOperationException {
        // TODO: Implement this method
        throw new IncorrectOperationException();
    }

    public boolean isReferenceTo(PsiElement element) {
        return resolve() == element;
    }

    public Object[] getVariants() {
        // TODO: Implement this method
        return new Object[0];
    }

    public boolean isSoft() {
        return false;
    }


    @Nullable
    public PsiElement resolve() {

        String originalPath = preparePath(path, false);
        path = preparePath(path, true);

        for (VirtualFile dir : resultDirs) {
            VirtualFile targetFile = dir.findFileByRelativePath(path);
            if (targetFile != null) {
                PsiFile file = PsiManager.getInstance(project).findFile(targetFile);
                if (requestAction != null && !requestAction.isEmpty()) {
                    PsiViewerTreeModel tree = new PsiViewerTreeModel(file);
                    PsiElement method = tree.getMethod(requestAction);
                    if (method != null) {
                        return method;
                    }
                }
                return file;
            } else {
                VirtualFile originalTargetFile = dir.findFileByRelativePath(originalPath);
                if (originalTargetFile != null) {
                    PsiFile originalFile = PsiManager.getInstance(project).findFile(originalTargetFile);
                    return originalFile;
                }
            }
        }

        if (debugMode) {
            Notifications.Bus.notify(new Notification("KohanaFactoryNavigator", "path", path, NotificationType.WARNING));
            Notifications.Bus.notify(new Notification("KohanaFactoryNavigator", "targetFile", "not exists", NotificationType.WARNING));
        }

        return null;
    }

    public String preparePath(String pathString, Boolean replaceParts) {
        pathString = pathString.toLowerCase();
        if (KohanaClass.regexp != null && replaceParts == true) {
            String[] regexpParts = StringUtils.split(KohanaClass.regexp, '|');
            if (regexpParts.length > 0) {
                for (String regexp : regexpParts) {
                    pathString = pathString.replace(regexp.toLowerCase(), "");
                }
            }
        }

        if (KohanaClass.name.equals("request")) {
            String[] parts = pathString.split("/");
            if (parts.length > 1) {
                requestAction = parts[1];
            }
            pathString = parts[0].toLowerCase().replace('_', '/');
        } else if (KohanaClass.name.equals("config")) {
            String[] parts = pathString.split("\\.");
            pathString = parts[0].toLowerCase();
        } else if (!KohanaClass.name.equals("view")) {
            pathString = pathString.toLowerCase().replace('_', '/');
        }

        if (!KohanaClass.name.equals("view") && !KohanaClass.name.equals("config") && kohanaPSR) {
            String[] path_parts = pathString.split("/");
            String prepared = "";
            for (String part : path_parts) {
                if (prepared.length() > 0) {
                    prepared += "/";
                }
                prepared += part.substring(0, 1).toUpperCase() + part.substring(1);
            }

            pathString = prepared;
        }

        pathString += ".php";

        return pathString;
    }

    @Override
    public String getCanonicalText() {
        return path;
    }
}
