import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class EditPathDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField fieldName;
    private JPanel messagePanel;
    private JLabel errorMessage;
    private JTextField fieldPath;
    private String editedPath = null;
    private boolean newNameFirstKeyTyped = false;
    private Container relativeContainer;
    private ArrayList<String> pathNames;
    private Project project;
    private PathsState.Path path;
    ActionListener buttonCancelListener;

    public EditPathDialog() {
        setTitle("Path Settings");
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        messagePanel.setVisible(false);

        fieldName.addKeyListener(new KeyListener() {
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

    public EditPathDialog(Project p) {
        this();
        project = p;
    }

    public EditPathDialog(Project p, PathsState.Path pa) {
        this();
        project = p;
        path = pa;
        if (path.name != null && !path.name.equals("~tmp") && !path.name.equals("null") && !path.name.isEmpty()) {
            fieldName.setText(path.name);
        }

        if (path.path != null && !path.path.equals("null") && !path.path.isEmpty()) {
            fieldPath.setText(path.path);
        }

        editedPath = path.name;
        buttonOK.setEnabled(true);
    }

    private void validateSavedNameTextField() {
        if (!newNameFirstKeyTyped) return;

        String enteredName = fieldName.getText();
        boolean isValid = !enteredName.isEmpty();

        if (isValid) {
            for (String sessionName : pathNames) {
                if (sessionName.equals(enteredName) && (editedPath == null || !editedPath.equals(enteredName))) {
                    isValid = false;
                    errorMessage.setText("Path exists");
                    break;
                } else {
                    VirtualFile moduleDir = project.getBaseDir().findFileByRelativePath(enteredName.trim());
                    if (!moduleDir.isDirectory()) {
                        isValid = false;
                        errorMessage.setText("Path is incorrect");
                        break;
                    }
                }
            }
        } else {
            errorMessage.setText("Path must not be empty");
        }

        messagePanel.setVisible(!isValid);
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

    public void setSavedPaths(ArrayList<String> pathNames) {
        this.pathNames = pathNames;
    }

    public void addOnOKListener(ActionListener listener) {
        buttonOK.addActionListener(listener);
    }

    public void addOnCancelListener(ActionListener listener) {
        buttonCancelListener = listener;
        buttonCancel.addActionListener(listener);
    }

    public String getPathName() {
        return fieldName.getText();
    }

    public String getPathPath() {
        return fieldPath.getText();
    }
}
