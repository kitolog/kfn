import com.intellij.dvcs.repo.Repository;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import git4idea.*;

import java.util.ArrayList;
import java.util.List;

import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryChangeListener;
import git4idea.repo.GitRepositoryManager;
import org.apache.commons.lang.StringUtils;

public class StorageHelper {

    public static Storage storageObject;
    private Project project;
    private String gitBranchName;

    public class Storage {
        public List<KohanaClassesState.KohanaClass> classes;
        public List<PathsState.Path> paths;
        public Boolean kohanaPSR;
        public Boolean debugMode;
        public String factoriesList;
    }

    public StorageHelper(Project pr) throws Exception
    {
        project = pr;
        storageObject = new Storage();

        gitBranchName = new String("");

        try
        {
            GitRepositoryManager repoManager = ServiceManager.getService(project, GitRepositoryManager.class);
            List<git4idea.repo.GitRepository> repositories = repoManager.getRepositories();
            for (git4idea.repo.GitRepository repo : repositories)
            {
                GitLocalBranch branch = repo.getCurrentBranch();
                gitBranchName = branch.getName() + ".";
            }
        }
        catch (Exception e)
        {
//            Notifications.Bus.notify(new Notification("KohanaFactoryNavigator", "StorageHelper", "Reading current git branch failed. Exception: " + e.getMessage(), NotificationType.ERROR));
        }

        read();
    }

    public void write()
    {

        PropertiesComponent properties = PropertiesComponent.getInstance(project);
        if (storageObject.kohanaPSR != null)
        {
            properties.setValue(gitBranchName + "kohanaPSR", String.valueOf(storageObject.kohanaPSR));
        }

        if (storageObject.debugMode != null)
        {
            properties.setValue(gitBranchName + "debugMode", String.valueOf(storageObject.debugMode));
        }

        if (storageObject.factoriesList != null)
        {
            properties.setValue(gitBranchName + "factoriesList", storageObject.factoriesList);
        }

        String pathString = new String();
        if (!storageObject.paths.isEmpty())
        {
            ArrayList<String> pathList = new ArrayList<String>();
            for (PathsState.Path path : storageObject.paths)
            {
                pathList.add(path.toString());
            }
            pathString = StringUtils.join(pathList.toArray(), ';');
        }

        properties.setValue(gitBranchName + "paths", pathString);

        String classesString = new String();
        if (!storageObject.classes.isEmpty())
        {
            ArrayList<String> classesList = new ArrayList<String>();
            for (KohanaClassesState.KohanaClass kc : storageObject.classes)
            {
                classesList.add(kc.toString());
            }
            classesString = StringUtils.join(classesList.toArray(), ';');
        }

        properties.setValue(gitBranchName + "classes", classesString);
    }

    public void read() throws Exception
    {

        PropertiesComponent properties = PropertiesComponent.getInstance(project);
        storageObject.factoriesList = properties.getValue(gitBranchName + "factoriesList", DefaultSettings.factoriesList);
        storageObject.kohanaPSR = properties.getBoolean(gitBranchName + "kohanaPSR", true);
        storageObject.debugMode = properties.getBoolean(gitBranchName + "debugMode", false);

        /**
         * classes
         */
        String classes = properties.getValue(gitBranchName + "classes", DefaultSettings.classes);
        KohanaClassesState kcs = new KohanaClassesState(project);
        if (classes.isEmpty())
        {
            classes = DefaultSettings.classes;
        }
        if (!classes.isEmpty())
        {
            String[] classesList = StringUtils.split(classes, ';');
            if (classesList.length > 0)
            {
                for (String classesPart : classesList)
                {
                    KohanaClassesState.KohanaClass kc = kcs.getOrCreateKohanaClassByName("~tmp");
                    String[] classProperties = StringUtils.split(classesPart, ',');
                    if (classProperties.length > 0)
                    {
                        for (String classProperty : classProperties)
                        {
                            String[] classValues = StringUtils.split(classProperty, ':');
                            if (classValues.length == 2)
                            {
                                kc.set(classValues[0], classValues[1]);
                            }
                        }
                    }
                }
                storageObject.classes = kcs.classes;
            }
        }

        /**
         * paths
         */
        String path = properties.getValue(gitBranchName + "paths", DefaultSettings.paths);
        PathsState ps = new PathsState(project);
        if (!path.isEmpty())
        {
            String[] pathList = StringUtils.split(path, ';');
            if (pathList.length > 0)
            {
                for (String pathPart : pathList)
                {
                    PathsState.Path p = ps.getOrCreatePathByName("~tmp");
                    String[] pathProperties = StringUtils.split(pathPart, ',');
                    if (pathProperties.length > 0)
                    {
                        for (String pathProperty : pathProperties)
                        {
                            String[] pathValues = StringUtils.split(pathProperty, ':');
                            if (pathValues.length > 0)
                            {
                                if (pathValues.length == 2)
                                {
                                    p.set(pathValues[0], pathValues[1]);
                                }
                            }
                        }
                    }
                }
                storageObject.paths = ps.paths;
            }
        }
        else
        {
            if (kcs != null)
            {
                ps.scanDirs(kcs, false);
                if (ps.paths != null && !ps.paths.isEmpty())
                {
                    setPaths(ps.paths);
                    write();
                }
            }
        }
    }

    public void setKohanaPSR(Boolean psr)
    {
        storageObject.kohanaPSR = psr;
        write();
    }

    public void setDebugMode(Boolean dm)
    {
        storageObject.debugMode = dm;
        write();
    }

    public void setFactoriesList(String factories)
    {
        storageObject.factoriesList = factories;
        write();
    }

    public void setPaths(List<PathsState.Path> paths)
    {
        storageObject.paths = paths;
        write();
    }

    public void setClasses(List<KohanaClassesState.KohanaClass> classes)
    {
        storageObject.classes = classes;
        write();
    }
}
