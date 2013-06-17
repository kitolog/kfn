import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.thoughtworks.xstream.mapper.ArrayMapper;
import org.jetbrains.annotations.NotNull;
import com.intellij.notification.Notification;
import com.intellij.notification.Notifications;
import com.intellij.notification.NotificationType;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;

public class MyPsiReferenceProvider extends PsiReferenceProvider {

    protected static String elementClassName;
    protected static String elementMethodName;
    protected static String methodsList;
    protected static Boolean kohanaPSR;
    protected static Map<String, String> classMethods = new HashMap<String, String>(50);

    public static final PsiReferenceProvider[] EMPTY_ARRAY = new PsiReferenceProvider[0];

    public MyPsiReferenceProvider() {
    }

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull final ProcessingContext context) {

        Project project = element.getProject();
        PropertiesComponent properties = PropertiesComponent.getInstance(project);
        String classesString = properties.getValue("classesList", "view,orm,request");
        String factoriesString = properties.getValue("factoriesList", "");
        methodsList = properties.getValue("methodsList", "");
        kohanaPSR = properties.getBoolean("kohanaPSR", false);
        String modulesList = properties.getValue("modulesList", "");
        String kohanaAppDir = properties.getValue("kohanaAppPath", "application, modules");

        String className = element.getClass().getName();
        Class elementClass = element.getClass();
        if (className.endsWith("StringLiteralExpressionImpl")) {
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
                if (uri.endsWith(".tpl") || uri.startsWith("smarty:") || isKohanaFactoryCall(element, classesString, factoriesString)) {

                    String[] appDirs = kohanaAppDir.split(",");
                    ArrayList<VirtualFile> resultDirs = new ArrayList<VirtualFile>();

                    for (String dir : appDirs) {
                        if (!dir.isEmpty()) {
                            if (dir.contains("modules")) {
                                VirtualFile modulesDir = project.getBaseDir().findFileByRelativePath(dir.trim());
                                if ((dir != null) && modulesDir.isDirectory()) {
                                    VirtualFile[] modules = modulesDir.getChildren();
                                    for (VirtualFile module : modules) {
                                        if ((modulesList.isEmpty()) || modulesList.contains(module.getName())) {
                                            VirtualFile found = module.findFileByRelativePath(getClassPath());
                                            if ((found != null) && found.isDirectory()) {
                                                resultDirs.add(found);
                                            }
                                        }
                                    }
                                }
                            } else {
                                VirtualFile found = project.getBaseDir().findFileByRelativePath(dir + getClassPath());
                                if ((found != null) && found.isDirectory()) {
                                    resultDirs.add(found);
                                }
                            }
                        }
                    }
                    if (resultDirs.isEmpty()) {
                        Notifications.Bus.notify(new Notification("KohanaFactoryNavigator", "getReferencesByElement", "resultDirs are empty", NotificationType.ERROR));
                        return PsiReference.EMPTY_ARRAY;
                    }

                    PsiReference ref = new MyReference(uri, element, new TextRange(start, start + len), project, resultDirs, elementClassName, kohanaPSR);
                    return new PsiReference[]{ref};
                }

            } catch (Exception e) {
                Notifications.Bus.notify(new Notification("KohanaFactoryNavigator", "getReferencesByElement", "Exception: " + e.getMessage(), NotificationType.ERROR));
                Notifications.Bus.notify(new Notification("KohanaFactoryNavigator", "getReferencesByElement", "Exception: " + e.getStackTrace(), NotificationType.ERROR));
            }
        }

        return PsiReference.EMPTY_ARRAY;
    }

    public static boolean isKohanaFactoryCall(PsiElement element, String classesList, String factoriesString) {
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

                    if(!methodsList.isEmpty()){
                        String[] methods = methodsList.split("\\)(\\s+|),");
                        for (String method : methods) {
                            String[] splittedMethod = method.trim().split("\\(");

                            if(splittedMethod.length != 0){
                                classMethods.put(splittedMethod[0], splittedMethod[1]);
                            }
                        }
                    }

                    if (factoriesString.contains(elementMethodName.toLowerCase())) {
                        Method getClassReference = prevEl.getClass().getMethod("getClassReference");
                        Object classRef = getClassReference.invoke(prevEl);

                        String phpClassName = (String) phpPsiElementGetName.invoke(classRef);

                        elementClassName = new String(phpClassName.toLowerCase());
                        if (classesList.contains(elementClassName)) {
                            return true;
                        }
                    } else if (!methodsList.isEmpty() && methodsList.contains(elementMethodName.toLowerCase())) {
                        elementClassName = classByMethodName(elementMethodName.toLowerCase());
                        return true;
                    }
                } catch (Exception ex) {
                    Notifications.Bus.notify(new Notification("KohanaFactoryNavigator", "isKohanaFactoryCall", "Exception: " + ex.getStackTrace(), NotificationType.ERROR));
                }
            }
        }
        return false;
    }

    public String getClassPath() {

        String result;
        String folder;

        if (checkClass("view")) {
            result = "/views";
        }
        else if (checkClass("config")) {
            result = "/config";
        }else {
            result = "/classes/";

            if (checkClass("orm")) {
                folder = "model";
            } else if (checkClass("request")) {
                folder = "controller";
            } else if (checkClass("forge")) {
                folder = "form";
            } else {
                folder = elementClassName.toLowerCase();
            }

            if (kohanaPSR) {
                result += folder.substring(0, 1).toUpperCase() + folder.substring(1);
            } else {
                result += folder;
            }
        }

        result += '/';
        return result;
    }

    static boolean checkClass(String className) {

        Boolean result = ((elementClassName != null ) && elementClassName.equals(className)) || (classMethods.containsKey(className) && classMethods.get(className).contains(elementMethodName));
        return result;
    }

    static String classByMethodName(String methodName) {

        String result = new String();
        for( Entry<String, String> entry : classMethods.entrySet() ){

            if(entry.getValue().contains(methodName)){
                result = entry.getKey();
                break;
            }
        }
        return result;
    }
}
