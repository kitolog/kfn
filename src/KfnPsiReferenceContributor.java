import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiElement;

public class KfnPsiReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(PsiReferenceRegistrar registrar)
    {
        final StorageHelper store;

        Project[] projects = ProjectManager.getInstance()
                .getOpenProjects();
        if (projects.length > 0)
        {
            Project project = projects[0];
            if (project != null)
            {
                try
                {
                    store = new StorageHelper(project);
                    if (store.storageObject != null)
                    {
                        KfnPsiReferenceProvider provider = new KfnPsiReferenceProvider(store);
                        registrar.registerReferenceProvider(StandardPatterns.instanceOf(PsiElement.class), provider);
                    }
                }
                catch (Exception e)
                {
                }
            }
        }
    }
}