import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import java.awt.*;
import javax.swing.*;
import  com.intellij.ide.util.PropertiesComponent;

public class KohanaFactoryNavigatorSettingsPage  implements Configurable  {

    private JTextField appPathTextField;
    private JCheckBox kohanaPSR;
    private JTextField classesListTextField;
    private JTextField methodsListTextField;
    private JTextField factoriesListTextField;
    private JTextField modulesListTextField;
    Project project;

    public KohanaFactoryNavigatorSettingsPage(Project project) {
        this.project = project;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "KohanaFactoryNavigator";
    }

    @Override
    public JComponent createComponent() {

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout
                (panel,  BoxLayout.Y_AXIS));

        JPanel panel5 = new JPanel();
        panel5.setLayout(new BoxLayout(panel5, BoxLayout.X_AXIS));
        kohanaPSR = new JCheckBox("Use PSR-0");
        panel5.add(kohanaPSR);
        panel5.add(Box.createHorizontalGlue());

        /**
         * Path list
         */
        JPanel panel2 = new JPanel();
        panel2.setLayout( new BoxLayout(panel2,  BoxLayout.X_AXIS));
        appPathTextField = new JTextField(50);
        JLabel label = new JLabel("Kohana APPPATH, MODPATH:");
        panel2.add( label );
        label.setLabelFor(appPathTextField);
        panel2.add( appPathTextField );
        panel2.add(Box.createHorizontalGlue());
        appPathTextField.setMaximumSize( appPathTextField.getPreferredSize() );

        /**
         *   Classes list
         */
        JPanel panel4 = new JPanel();
        panel4.setLayout( new BoxLayout(panel4,  BoxLayout.X_AXIS));
        classesListTextField = new JTextField(50);
        JLabel label4 = new JLabel("Kohana classes:");
        panel4.add( label4 );
        label4.setLabelFor(classesListTextField);
        panel4.add( classesListTextField );
        panel4.add(Box.createHorizontalGlue());
        classesListTextField.setMaximumSize( classesListTextField.getPreferredSize() );

        /**
         *   Methods list
         */
        JPanel panel6 = new JPanel();
        panel6.setLayout( new BoxLayout(panel6,  BoxLayout.X_AXIS));
        methodsListTextField = new JTextField(50);
        JLabel label6 = new JLabel("Methods, used factory classes list:");
        panel6.add( label6 );
        label6.setLabelFor(methodsListTextField);
        panel6.add( methodsListTextField );
        panel6.add(Box.createHorizontalGlue());
        methodsListTextField.setMaximumSize( methodsListTextField.getPreferredSize() );

        /**
         *   Factories methods list
         */
        JPanel panel7 = new JPanel();
        panel7.setLayout( new BoxLayout(panel7,  BoxLayout.X_AXIS));
        factoriesListTextField = new JTextField(50);
        JLabel label7 = new JLabel("Factories methods list:");
        panel7.add( label7 );
        label7.setLabelFor(factoriesListTextField);
        panel7.add( factoriesListTextField );
        panel7.add(Box.createHorizontalGlue());
        factoriesListTextField.setMaximumSize( factoriesListTextField.getPreferredSize() );

        /**
         *  Modules list
         */
        JPanel panel3 = new JPanel();
        panel3.setLayout( new BoxLayout(panel3,  BoxLayout.X_AXIS));
        modulesListTextField = new JTextField(50);
        JLabel label3 = new JLabel("Allowed modules(leave empty for all):");
        panel3.add( label3 );
        label3.setLabelFor(modulesListTextField);
        panel3.add( modulesListTextField );
        panel3.add(Box.createHorizontalGlue());
        modulesListTextField.setMaximumSize( modulesListTextField.getPreferredSize() );

        /**

         */
        panel.add( panel5);
        panel.add(Box.createVerticalStrut(8));
        panel.add( panel2 );
        panel.add(Box.createVerticalStrut(8));
        panel.add(panel4);
        panel.add(Box.createVerticalStrut(8));
        panel.add(panel7);
        panel.add(Box.createVerticalStrut(8));
        panel.add(panel6);
        panel.add(Box.createVerticalStrut(8));
        panel.add(panel3);
        panel.add(Box.createVerticalGlue());
        PropertiesComponent properties = PropertiesComponent.getInstance(project);
        appPathTextField.setText(properties.getValue("kohanaAppPath", DefaultSettings.kohanaAppPath));
        kohanaPSR.setSelected(properties.getBoolean("kohanaPSR", true));
        classesListTextField.setText(properties.getValue("classesList", DefaultSettings.classesList));
        methodsListTextField.setText(properties.getValue("methodsList", DefaultSettings.methodsList));
        factoriesListTextField.setText(properties.getValue("factoriesList", DefaultSettings.factoriesList));
        modulesListTextField.setText(properties.getValue("modulesList", DefaultSettings.modulesList));

        return panel;
    }

    @Override
    public void apply() throws ConfigurationException {
        PropertiesComponent properties = PropertiesComponent.getInstance(project);
        properties.setValue("kohanaAppPath", appPathTextField.getText());
        properties.setValue("kohanaPSR", String.valueOf(kohanaPSR.isSelected()) );
        properties.setValue("classesList", classesListTextField.getText());
        properties.setValue("methodsList", methodsListTextField.getText());
        properties.setValue("factoriesList", factoriesListTextField.getText());
        properties.setValue("modulesList", modulesListTextField.getText());
    }

    @Override
    public boolean isModified() {
        return true;
    }

    @Override
    public String getHelpTopic() {
        return null;
    }

    @Override
    public void disposeUIResources() {

    }

    @Override
    public void reset() {

    }
}
