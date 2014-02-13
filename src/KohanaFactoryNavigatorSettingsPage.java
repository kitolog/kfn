import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.ui.table.JBTable;
import org.jetbrains.annotations.Nls;

import java.awt.*;
import java.io.IOException;
import javax.swing.*;

import com.intellij.ide.util.PropertiesComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KohanaFactoryNavigatorSettingsPage implements Configurable, ProjectComponent {

    Project project;

    private SettingsForm form;
    private KohanaClassesState kohanaClassesState;
    private PathsState pathsState;
    private StorageHelper storageHelper;

    @NotNull
    public String getComponentName() {
        return "KohanaFactoryNavigatorComponent";
    }

    public void initComponent() {
        // TODO: insert component initialization logic here
    }

    public void disposeComponent() {
        // TODO: insert component disposal logic here
    }

    public void projectOpened() {
        // called when project is opened
    }

    public void projectClosed() {
        // called when project is being closed
    }

    public KohanaFactoryNavigatorSettingsPage(Project project) throws Exception {
        this.project = project;
        kohanaClassesState = new KohanaClassesState(project);
        pathsState = new PathsState(project);
        storageHelper = new StorageHelper(project);

    }

    @Nls
    @Override
    public String getDisplayName() {
        return "KohanaFactoryNavigator";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        if (form == null) {
            try {
                form = new SettingsForm(project, storageHelper);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return form.getRootComponent();
    }

    @Override
    public boolean isModified() {
        return form != null && form.isModified();
    }

    @Override
    public void apply() throws ConfigurationException {
        if (form != null) {
            kohanaClassesState = form.getClassData();
            try {
                kohanaClassesState.saveState();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            pathsState = form.getPathData();
            try {
                pathsState.saveState();
            } catch (Exception e) {
                e.printStackTrace();
            }

            storageHelper.setKohanaPSR(form.isKohanaPSR());
            storageHelper.setDebugMode(form.isDebugMode());
            storageHelper.setFactoriesList(form.getFactoriesList());
            reset();
        }
    }

    @Override
    public void reset() {
        if (form != null) {
            form.setInitialState(kohanaClassesState, pathsState, storageHelper.storageObject.kohanaPSR, storageHelper.storageObject.debugMode, storageHelper.storageObject.factoriesList);
            form.mapStateToUI();
        }
    }

    @Override
    public void disposeUIResources() {
        form = null;
    }

    @Override
    public String getHelpTopic() {
        return null;
    }
}
