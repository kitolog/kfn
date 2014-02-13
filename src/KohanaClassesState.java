import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang.SerializationUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class KohanaClassesState {

    public void saveState() throws Exception {
        final StorageHelper store = new StorageHelper(project);
        store.setClasses(classes);
    }

    public class KohanaClass {
        public String name;
        public String regexp;
        public String methods;
        public String path;

        public KohanaClass(String name) {
            if (name == null) {
                name = "tmp";
            }

            this.name = name;
        }

        public String toString() {
            String result = "";
            result += "name:" + this.name;
            result += ",methods:" + this.methods;
            result += ",regexp:" + this.regexp;
            result += ",path:" + this.path;
            return result;
        }

        public KohanaClass set(String key, String value) {
            if (key.equals("name")) {
                this.name = value;
            } else if (key.equals("regexp")) {
                this.regexp = value;
            } else if (key.equals("path")) {
                this.path = value;
            } else {
                this.methods = value;
            }

            return this;
        }
    }

    public List<KohanaClass> classes;
    private Project project;

    public KohanaClassesState(Project pr) throws Exception {
        project = pr;
//        final StorageHelper store = new StorageHelper(project);
//        if (store.storageObject != null && store.storageObject.classes != null) {
//            classes = store.storageObject.classes;
//        } else {
        classes = new ArrayList<KohanaClass>();
//        }
    }

    public ArrayList<String> getKohanaClassNames() {
        ArrayList<String> names = new ArrayList<String>();
        for (KohanaClass cls : classes) {
            names.add(cls.name);
        }
        return names;
    }

    public void setKohanaClassOrder(ArrayList<String> sessionNames) {
        List<KohanaClass> newKohanaClasses = new ArrayList<KohanaClass>();
        for (String name : sessionNames) {
            newKohanaClasses.add(getKohanaClassByName(name));
        }
        classes = newKohanaClasses;
    }

    public KohanaClass getOrCreateKohanaClassByName(String name) {
        KohanaClass kc = getKohanaClassByName(name);
        return kc == null ? createKohanaClass(name) : kc;
    }

    public KohanaClass getKohanaClassByName(String name) {
        KohanaClass namedSession = null;
        for (KohanaClass session : classes) {
            if (name != null && session.name.equals(name)) {
                namedSession = session;
            }
        }
        return namedSession;
    }

    public KohanaClass createKohanaClass(String name) {
        KohanaClass kc = new KohanaClass(name);
        classes.add(kc);
        return kc;
    }

    /**
     * @param name
     * @return
     * @TODO add temp classes creation
     */
    public KohanaClass createTempKohanaClass(String name) {

        try {
            if (this.getKohanaClassByName(name) == null) {
                StorageHelper storageHelper = new StorageHelper(project);
                KohanaClass kc = this.createKohanaClass(name);
                String path = name;
                if (kc.equals("view")) {
                    path = "views";
                } else if (kc.equals("config")) {
                    path = "config";
                } else if (storageHelper.storageObject != null && storageHelper.storageObject.kohanaPSR) {
                    path = "classes/" + name.substring(0, 1).toUpperCase() + name.substring(1);
                }
                kc.path = path;
                classes = storageHelper.storageObject.classes;
                classes.add(kc);
                storageHelper.setClasses(classes);
                return kc;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void removeKohanaClassByName(String name) {
        KohanaClass kc = getKohanaClassByName(name);
        if (kc != null) {
            classes.remove(kc);
        }
    }

    public String toString() {
        String result = "All Kohana Classes:\n";
        for (KohanaClass kc : classes) {
            result += kc.toString() + "\n";
        }
        result += "Classes count: " + classes.size();
        return result;
    }

    public KohanaClassesState clone() {
        KohanaClassesState stateClone = null;
        try {
            stateClone = new KohanaClassesState(project);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (KohanaClass kc : classes) {
            KohanaClass kcClone = new KohanaClass(kc.name);
            kcClone.methods = kc.methods;
            kcClone.regexp = kc.regexp;
            kcClone.path = kc.path;
            stateClone.classes.add(kcClone);
        }
        return stateClone;
    }

    public ArrayList<String> getClassesNames() {
        ArrayList<String> names = new ArrayList<String>();
        for (KohanaClass cls : classes) {
            names.add(cls.name);
        }
        return names;
    }

    public ArrayList<String> getClassesPaths() {
        ArrayList<String> names = new ArrayList<String>();
        for (KohanaClass cls : classes) {
            names.add(cls.path);
        }
        return names;
    }

    public boolean equals(KohanaClassesState otherState) {
        boolean isEqual = true;
        if (classes.size() != otherState.classes.size()) isEqual = false;

        /**
         * @FIXME fix null pointer exception
         */
        if (isEqual) {
            for (int i = 0; i < classes.size(); i++) {
                KohanaClass sessionA = classes.get(i);
                KohanaClass sessionB = otherState.classes.get(i);
                if (!sessionA.name.equals(sessionB.name)
                        || !sessionA.regexp.equals(sessionB.regexp)
                        || !sessionA.methods.equals(sessionB.methods)
                        || !sessionA.regexp.equals(sessionB.regexp)
                        ) {
                    isEqual = false;
                    break;
                }
            }
        }

        return isEqual;
    }

}
