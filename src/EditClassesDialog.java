import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class EditClassesDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField className;
    private JTextField aliasMethods;
    private JTextField namePattern;
    private JTextField pathField;
    private String editedClass = null;
    private boolean newNameFirstKeyTyped = false;
    private Container relativeContainer;
    private ArrayList<String> classNames;
    ActionListener buttonCancelListener;

    public EditClassesDialog() {
        setTitle("Path Settings");
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        className.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {
            }

            public void keyPressed(KeyEvent e) {
            }

            public void keyReleased(KeyEvent e) {
                newNameFirstKeyTyped = true;
                validateSavedNameTextField();
            }
        });

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel(true);
            }
        });

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel(false);
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel(false);
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    public EditClassesDialog(KohanaClassesState.KohanaClass kc) {
        this();

        if (kc.name != null && !kc.name.equals("~tmp") && !kc.name.equals("null") && !kc.name.isEmpty()) {
            className.setText(kc.name);
        }

        if (kc.methods != null && !kc.methods.equals("null") && !kc.methods.isEmpty()) {
            aliasMethods.setText(kc.methods);
        }

        if (kc.regexp != null && !kc.regexp.equals("null") && !kc.regexp.isEmpty()) {
            namePattern.setText(kc.regexp);
        }

        if (kc.path != null && !kc.path.equals("null") && !kc.path.isEmpty()) {
            pathField.setText(kc.path);
        }

        editedClass = kc.name;

        buttonOK.setEnabled(true);
    }

    private void validateSavedNameTextField() {
        if (!newNameFirstKeyTyped) return;

        String enteredName = className.getText();
        boolean isValid = !enteredName.isEmpty();

        if (isValid) {
            for (String className : classNames) {
                if (className.equals(enteredName) && (editedClass == null || !editedClass.equals(enteredName))) {
                    isValid = false;
                    break;
                }
            }
        }

//        messagePanel.setVisible(!isValid);
        refresh();
    }

    private void onOK() {
// add your code here
        dispose();
    }

    public void refresh() {
        pack();
        setLocationRelativeTo(relativeContainer);
    }

    public void display(Container relativeContainer) {
        this.relativeContainer = relativeContainer;
        refresh();
        setVisible(true);
    }

    private void onCancel(Boolean is_direct) {

        if (buttonCancelListener != null) {
            buttonCancel.addActionListener(buttonCancelListener);
            if(!is_direct){
                buttonCancel.doClick();
            }
        }
// add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        EditPathDialog dialog = new EditPathDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    public void setSavedClasses(ArrayList<String> classNames) {
        this.classNames = classNames;
    }

    public void addOnOKListener(ActionListener listener) {
        buttonOK.addActionListener(listener);
    }

    public void addOnCancelListener(ActionListener listener) {
        buttonCancelListener = listener;
        buttonCancel.addActionListener(listener);
    }

    public String getClassName() {
        return className.getText();
    }

    public String getAliasMethods() {
        return aliasMethods.getText();
    }

    public String getNamePattern() {
        return namePattern.getText();
    }

    public String getPath() {
        return pathField.getText();
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}