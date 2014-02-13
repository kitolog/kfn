import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import com.intellij.notification.Notification;
import com.intellij.notification.Notifications;
import com.intellij.notification.NotificationType;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MyPsiReferenceProvider extends PsiReferenceProvider {

    protected static KohanaClassesState.KohanaClass KohanaClass;
    protected static String elementMethodName;
    protected static Boolean kohanaPSR;
    protected static Boolean debugMode;

    public static final PsiReferenceProvider[] EMPTY_ARRAY = new PsiReferenceProvider[0];

    public MyPsiReferenceProvider() {
    }

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull final ProcessingContext context) {

        Project project = element.getProject();
        final StorageHelper store;
        try {
            store = new StorageHelper(project);
            if (store.storageObject == null) {
                return PsiReference.EMPTY_ARRAY;
            }

            kohanaPSR = store.storageObject.kohanaPSR;
            debugMode = store.storageObject.debugMode;
            String factoriesString = store.storageObject.factoriesList;
            List<KohanaClassesState.KohanaClass> classesList = store.storageObject.classes;
            List<PathsState.Path> pathList = store.storageObject.paths;

            String className = element.getClass().getName();
            Class elementClass = element.getClass();
            if (className.endsWith("StringLiteralExpressionImpl") && classesList != null && pathList != null) {
                try {
                    Method method = elementClass.getMethod("getValueRange");
                    Object obj = method.invoke(element);
                    TextRange textRange = (TextRange) obj;
                    Class _PhpPsiElement = elementClass.getSuperclass().getSuperclass().getSuperclass();
                    Method phpPsiElementGetText = _PhpPsiElement.getMethod("getText");
                    Object obj2 = phpPsiElementGetText.invoke(element);
                    String str = obj2.toString();
                    String uri = str.substring(textRange.getStartOffset(), textRange.getEndOffset());
                    int start = textRange.getStartOffset();
                    int len = textRange.getLength();
                    if (uri.endsWith(".tpl") || uri.startsWith("smarty:") || isKohanaFactoryCall(element, classesList, factoriesString, project)) {
                        ArrayList<VirtualFile> resultDirs = new ArrayList<VirtualFile>();

                        for (PathsState.Path path : pathList) {
                            if (!path.path.isEmpty()) {
                                VirtualFile found = project.getBaseDir().findFileByRelativePath(getClassPath(path.path));
                                if ((found != null) && found.isDirectory()) {
                                    resultDirs.add(found);
                                }
                            }
                        }
                        if (resultDirs.isEmpty()) {
                            if (debugMode) {
                                Notifications.Bus.notify(new Notification("KohanaFactoryNavigator", "getReferencesByElement", "resultDirs are empty", NotificationType.ERROR));
                            }
                            return PsiReference.EMPTY_ARRAY;
                        }

                        PsiReference ref = new MyReference(uri, element, new TextRange(start, start + len), project, resultDirs, KohanaClass, kohanaPSR, debugMode);
                        return new PsiReference[]{ref};
                    }else{
                        if (debugMode) {
                            Notifications.Bus.notify(new Notification("KohanaFactoryNavigator", "getReferencesByElement", "isKohanaFactoryCall is equal to false", NotificationType.ERROR));
                        }
                    }

                } catch (Exception e) {

                    if (debugMode) {
                        Notifications.Bus.notify(new Notification("KohanaFactoryNavigator", "getReferencesByElement", "Exception: " + e.getMessage(), NotificationType.ERROR));
                        Notifications.Bus.notify(new Notification("KohanaFactoryNavigator", "getReferencesByElement", "Exception: " + e.getStackTrace(), NotificationType.ERROR));
                    }
                }
            }
        } catch (Exception e) {
            if (debugMode) {
                Notifications.Bus.notify(new Notification("KohanaFactoryNavigator", "getReferencesByElement", "Exception!", NotificationType.ERROR));
                e.printStackTrace();
            }
        }
        return PsiReference.EMPTY_ARRAY;
    }

    public static boolean isKohanaFactoryCall(PsiElement element, List<KohanaClassesState.KohanaClass> classesList, String factoriesString, Project project) {
        PsiElement prevEl = element.getParent();
        PsiElement orig = element.getOriginalElement();

        String elClassName;
        if (prevEl != null) {
            elClassName = prevEl.getClass().getName();
        }
        prevEl = prevEl.getParent();
        if (prevEl != null) {
            elClassName = prevEl.getClass().getName();
            if (elClassName.endsWith("MethodReferenceImpl")) {
                try {
                    Method phpPsiElementGetName = prevEl.getClass().getMethod("getName");
                    elementMethodName = (String) phpPsiElementGetName.invoke(prevEl);
                    String allowedMethods = new String();
                    for (KohanaClassesState.KohanaClass kc : classesList) {
                        if (kc.methods != null && kc.methods.length() > 0) {
                            allowedMethods += kc.methods;
                        }
                    }
                    if (factoriesString.contains(elementMethodName.toLowerCase()) || (allowedMethods.length() > 0 && allowedMethods.contains(elementMethodName.toLowerCase()))) {
                        String elementClassName = new String();
                        if (factoriesString.contains(elementMethodName.toLowerCase())) {
                            Method getClassReference = prevEl.getClass().getMethod("getClassReference");
                            Object classRef = getClassReference.invoke(prevEl);

                            String phpClassName = (String) phpPsiElementGetName.invoke(classRef);
                            elementClassName = phpClassName.toLowerCase();
                        }

                        KohanaClass = null;
                        for (KohanaClassesState.KohanaClass kc : classesList) {
                            if (factoriesString.contains(elementMethodName.toLowerCase()) && !kc.name.isEmpty() && kc.name.equals(elementClassName)) {
                                KohanaClass = kc;
                                break;
                            } else if (!kc.methods.isEmpty() && kc.methods.contains(elementMethodName.toLowerCase())) {
                                KohanaClass = kc;
                                break;
                            }
                        }

                        if (KohanaClass != null) {
                            return true;
                        } else {
                            /**
                             * @TODO add temp classes creation
                             */
                            KohanaClassesState kohanaClassesState = new KohanaClassesState(project);
                            KohanaClassesState.KohanaClass tkc = kohanaClassesState.createTempKohanaClass(elementClassName);
                        }
                    }
                } catch (Exception ex) {
                    if (debugMode) {
                        Notifications.Bus.notify(new Notification("KohanaFactoryNavigator", "isKohanaFactoryCall", "Exception: " + ex.getStackTrace(), NotificationType.ERROR));
                    }
                }
            }
        }
        return false;
    }

    public String getClassPath(String path) {
        String result;

        result = path + "/" + KohanaClass.path.trim();

        result += '/';
        return result;
    }
}
