import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;

public class MyPsiReferenceContributor extends PsiReferenceContributor {
	@Override
	public void registerReferenceProviders(PsiReferenceRegistrar registrar)
	{
		MyPsiReferenceProvider provider = new MyPsiReferenceProvider();

		//registrar.registerReferenceProvider(StandardPatterns.instanceOf(XmlAttributeValue.class), provider);
		//registrar.registerReferenceProvider(StandardPatterns.instanceOf(XmlTag.class), provider);

		registrar.registerReferenceProvider(StandardPatterns.instanceOf(PsiElement.class), provider);
	}
}
