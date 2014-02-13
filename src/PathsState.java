import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.ArrayList;
import java.util.List;

public class PathsState {

    public class Path {
        public String name;
        public String path;

        public Path(String name) {
            if (name == null) {
                name = "tmp";
            }
            this.name = name;
        }

        public String toString() {
            String result = "";
            result += "name:" + this.name;
            result += ",path:" + this.path;
            return result;
        }

        public Path set(String key, String value) {
            if (key.equals("name")) {
                this.name = value;
            } else {
                this.path = value;
            }

            return this;
        }
    }

    public List<Path> paths;
    private Project project;

    public PathsState(Project pr) throws Exception {
        project = pr;
//        final StorageHelper store = new StorageHelper(project);
//        if (store.storageObject != null && store.storageObject.paths != null) {
//            paths = store.storageObject.paths;
//        } else {
        paths = new ArrayList<Path>();
//        }
    }

    public void saveState() throws Exception {
        final StorageHelper store = new StorageHelper(project);
        store.setPaths(paths);
    }

    public ArrayList<String> getPathsNames() {
        ArrayList<String> names = new ArrayList<String>();
        for (Path path : paths) {
            names.add(path.name);
        }
        return names;
    }

    public void setPathsOrder(ArrayList<String> pathsNames) {
        List<Path> newPaths = new ArrayList<Path>();
        for (String name : pathsNames) {
            newPaths.add(getPathByName(name));
        }
        paths = newPaths;
    }

    public Path getOrCreatePathByName(String name) {
        Path kc = getPathByName(name);
        return kc == null ? createPath(name) : kc;
    }

    public Path getPathByName(String name) {
        Path namedSession = null;
        for (Path p : paths) {
            if (name != null && p.name.equals(name)) {
                namedSession = p;
            }
        }
        return namedSession;
    }

    public Path createPath(String name) {
        Path kc = new Path(name);
        paths.add(kc);
        return kc;
    }

    public void removePathByName(String name) {
        Path path = getPathByName(name);
        if (path != null) {
            paths.remove(path);
        }
    }

    public List<Path> scanDirs(KohanaClassesState kcs, Boolean savePath) {
        /**
         * @TODO write better
         */
        ArrayList<String> apppath = new ArrayList();
        apppath.add("application");
        apppath.add("wwwroot/application");
        ArrayList<String> modpath = new ArrayList();
        modpath.add("modules");
        modpath.add("wwwroot/modules");

        String basePath = project.getBaseDir().getCanonicalPath() + "/";
        for (String ap : apppath) {
            VirtualFile apppathDir = project.getBaseDir().findFileByRelativePath(ap);
            if ((apppathDir != null) && apppathDir.isDirectory()) {
                Path path = this.getOrCreatePathByName(apppathDir.getName());
                path.path = apppathDir.getCanonicalPath().replace(basePath, "");
                break;
            }
        }

        if (kcs != null && kcs.classes != null) {
            for (String mp : modpath) {
                ArrayList<String> kcp = kcs.getClassesPaths();
                VirtualFile modpathDir = project.getBaseDir().findFileByRelativePath(mp);
                if (kcp != null && !kcp.isEmpty() && (modpathDir != null) && modpathDir.isDirectory()) {

                    scanModulesDirs(modpathDir, kcp, 0, basePath);
                }
            }
        }

        if (this.paths != null) {
            try {
                if (savePath)
                {
                    this.saveState();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return this.paths;
    }

    protected void scanModulesDirs(VirtualFile modpathDir, ArrayList<String> kcp, Integer level, String basePath) {

        if (modpathDir.isDirectory() && level < 30) {
            VirtualFile[] modules = modpathDir.getChildren();
            for (VirtualFile module : modules) {
                for (String class_path : kcp) {
                    VirtualFile found = module.findFileByRelativePath(class_path);
                    if ((found != null) && found.isDirectory()) {

                        Path module_path = this.getOrCreatePathByName(module.getName());
                        module_path.path = module.getCanonicalPath().replace(basePath, "");
                        break;
                    } else {
                        level++;
                        scanModulesDirs(module, kcp, level, basePath);
                    }
                }
            }
        }
    }

    public String toString() {
        String result = "All Paths:\n";
        return result;
    }

    public PathsState clone() {
        PathsState stateClone = null;
        try {
            stateClone = new PathsState(project);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (Path kc : paths) {
            Path kcClone = new Path(kc.name);
            kcClone.path = kc.path;
            stateClone.paths.add(kcClone);
        }
        return stateClone;
    }

    public boolean equals(PathsState otherState) {
        boolean isEqual = true;
        if (paths.size() != otherState.paths.size()) isEqual = false;

        if (isEqual) {
            for (int i = 0; i < paths.size(); i++) {
                Path sessionA = paths.get(i);
                Path sessionB = otherState.paths.get(i);
                if (!sessionA.name.equals(sessionB.name) || !sessionA.path.equals(sessionB.path)) {
                    isEqual = false;
                    break;
                }
            }
        }

        return isEqual;
    }

}