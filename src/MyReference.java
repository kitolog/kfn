import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
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
	protected Boolean kohanaPSR;
	protected String className;
	protected ArrayList<VirtualFile> resultDirs;
	protected Map<String, String> classesTemplateMap;

	public MyReference(String path, PsiElement element, TextRange textRange, Project project, ArrayList<VirtualFile> resultDirs, String className, Boolean kohanaPSR, Map classesTemplateMap)
	{
		this.element = element;
		this.textRange = textRange;
		this.project = project;
		this.path = path;
		this.resultDirs = resultDirs;
		this.className = className;
		this.kohanaPSR = kohanaPSR;
		this.classesTemplateMap = classesTemplateMap;
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
		// TODO: Implement this method
		return new Object[0];
	}

	public boolean isSoft()
	{
		return false;
	}


	@Nullable
	public PsiElement resolve()
	{
//		Admin_Settings_European
		String action = new String("");
//		String className = MyPsiReferenceProvider.checkClass("orm");

		path = path.toLowerCase();
//		if (MyPsiReferenceProvider.checkClass("orm"))
//		{
//			path = path.toLowerCase().replace('_', '/');
//		}
//		else
		if (MyPsiReferenceProvider.checkClass("request"))
		{
			String[] parts = path.split("/");
			if (parts.length > 1)
			{
				action = parts[1];
			}
			path = parts[0].toLowerCase().replace('_', '/');
		}
		else
		if (MyPsiReferenceProvider.checkClass("config"))
		{
			String[] parts = path.split("\\.");
			path = parts[0].toLowerCase();
		}
		else
//		if (!MyPsiReferenceProvider.checkClass("view"))
//		{
//			path = path.toLowerCase().replace('_', '/');
//		}

		if (kohanaPSR && !MyPsiReferenceProvider.checkClass("view") && !MyPsiReferenceProvider.checkClass("config"))
		{
			String[] path_parts = path.split("/");
			String prepared = "";
			for (String part : path_parts)
			{
				if (prepared.length() > 0)
				{
					prepared += "/";
				}
				prepared += part.substring(0, 1).toUpperCase() + part.substring(1);
			}

			path = prepared;
		}

		path += ".php";
		for (VirtualFile dir : resultDirs)
		{
			VirtualFile targetFile = dir.findFileByRelativePath(path);
			if (targetFile != null)
			{
				PsiFile file = PsiManager.getInstance(project).findFile(targetFile);
				if (!action.isEmpty())
				{
					PsiViewerTreeModel tree = new PsiViewerTreeModel(file);
					PsiElement method = tree.getMethod(action);
					if (method != null)
					{
						return method;
					}
				}
				return file;
			}
		}

//        Notifications.Bus.notify(new Notification("KohanaFactoryNavigator", "path", path, NotificationType.WARNING));
//        Notifications.Bus.notify(new Notification("KohanaFactoryNavigator", "targetFile", "not exists", NotificationType.WARNING));

		return null;
	}

	@Override
	public String getCanonicalText()
	{
		return path;
	}
}
