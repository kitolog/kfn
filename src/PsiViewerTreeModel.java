import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class PsiViewerTreeModel implements TreeModel {
    private PsiElement _root;
    private final PsiFile _file;

    public PsiViewerTreeModel(PsiFile file)
    {
        this._file = file;
        PsiElement root = file.getFirstChild();
        this._root = root;
    }

    public Object getRoot()
    {
        return this._root;
    }

    public void setRoot(PsiElement root)
    {
        this._root = root;
    }

    public Object getChild(Object parent, int index)
    {
        PsiElement psi = (PsiElement) parent;
        List children = getFilteredChildren(psi);
        return children.get(index);
    }

    public int getChildCount(Object parent)
    {
        PsiElement psi = (PsiElement) parent;
        return getFilteredChildren(psi).size();
    }

    public boolean isLeaf(Object node)
    {
        PsiElement psi = (PsiElement) node;
        return getFilteredChildren(psi).size() == 0;
    }

    private List<PsiElement> getFilteredChildren(PsiElement psi)
    {
        List filteredChildren = new ArrayList();

        for (PsiElement e = psi.getFirstChild(); e != null; e = e.getNextSibling())
        {
            if (isValid(e))
            {
                filteredChildren.add(e);
            }
        }
        return filteredChildren;
    }

    private boolean isValid(PsiElement psi)
    {
        return !(psi instanceof PsiWhiteSpace);
    }

    public List getElementIndex(String name)
    {
        List psiChildren = getFilteredChildren(this._root);
        return psiChildren;
    }

    public PsiElement getMethod(String name)
    {
        List psiClasses = getClasses(this._root);
        if (psiClasses.size() != 0)
        {
            PsiElement psiClass;
            for (int i = 0; i < psiClasses.size(); i++)
            {
                Object item = psiClasses.get(i);
                psiClass = (PsiElement) item;
                for (PsiElement e = psiClass.getFirstChild(); e != null; e = e.getNextSibling())
                {
                    if (isMethod(e))
                    {
                        PropertyDescriptor[] propertyDescriptors;
                        Object el = e;
                        PropertyDescriptor type = getProperty(el.getClass(), "text");
                        if (type != null)
                        {
                            String value = formattedToString(getValue(el, type));
                            if (value.contains(name))
                            {
                                return e;
                            }
                        }
                    }
                }
                return psiClass;
            }
        }
        return null;
    }

    public List getClasses(PsiElement root)
    {
        List psiClasses = new ArrayList();
        for (PsiElement e = root.getFirstChild(); e != null; e = e.getNextSibling())
        {
            if (isValid(e) && isClass(e))
            {
                psiClasses.add(e);
            }
        }
        return psiClasses;
    }

    private boolean isMethod(PsiElement psiElement)
    {
        PropertyDescriptor[] propertyDescriptors;
        Object element = psiElement;
        PropertyDescriptor type = getProperty(element.getClass(), "elementType");
        String value = formattedToString(getValue(element, type));
        return value.equals("Class method");
    }

    private boolean isClass(PsiElement psiElement)
    {
        PropertyDescriptor[] propertyDescriptors;

        Object element = psiElement;
        PropertyDescriptor type = getProperty(element.getClass(), "elementType");
        String value = formattedToString(getValue(element, type));
        return value.equals("Class");
    }

    public static Object getValue(Object target, PropertyDescriptor property)
    {

        Method getter = property.getReadMethod();

        String name = property.getDisplayName();

        Object value;

        try
        {

            Object[] args = new Object[0];

            getter.setAccessible(true);


            value = getter.invoke(target, args);

        }
        catch (InvocationTargetException ex)
        {

            value = "<exception=" + ex.getMessage() + ">";

        }
        catch (Exception ex)
        {

            value = "<exception=" + ex.getMessage() + ">";

        }

        return value;

    }

    private static String formattedToString(Object object)
    {

        if (object == null)
        {
            return "null";
        }

        if (!object.getClass()
                .isArray())
        {
            return object.toString();
        }

        StringBuffer buf = new StringBuffer();

        buf.append("[");

        Object[] array = (Object[]) object;

        for (int i = 0; i < array.length; i++)
        {

            if (i != 0)
            {
                buf.append(", ");
            }

            buf.append(array[i] == null ? "null" : array[i].toString());
        }

        buf.append("]");

        return buf.toString();

    }

    public static PropertyDescriptor[] getProperties(Class targetClass)
    {

        PropertyDescriptor[] propertyDescriptors;

        try
        {
            BeanInfo bi = Introspector.getBeanInfo(targetClass);
            propertyDescriptors = bi.getPropertyDescriptors();
        }
        catch (IntrospectionException ex)
        {
            propertyDescriptors = new PropertyDescriptor[0];
        }

        return propertyDescriptors;

    }

    public static PropertyDescriptor getProperty(Class targetClass, String propertyName)
    {

        PropertyDescriptor[] propertyDescriptors = getProperties(targetClass);

        for (PropertyDescriptor propertyDescriptor : propertyDescriptors)
        {

            if (propertyDescriptor.getName()
                    .equals(propertyName))
            {
                return propertyDescriptor;
            }

        }

        return null;

    }

    public int getIndexOfChild(Object parent, Object child)
    {
        PsiElement psiParent = (PsiElement) parent;
        List psiChildren = getFilteredChildren(psiParent);

        return psiChildren.indexOf((PsiElement) child);
    }

    public void valueForPathChanged(TreePath path, Object newValue)
    {
    }

    public synchronized void addTreeModelListener(TreeModelListener l)
    {
    }

    public synchronized void removeTreeModelListener(TreeModelListener l)
    {
    }
}