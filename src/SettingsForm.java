import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.ActionToolbarPosition;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.ui.*;
import com.intellij.ui.components.JBList;
import com.intellij.util.containers.ContainerUtil;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: user
 * Date: 08.12.12
 * Time: 13:05
 * To change this template use File | Settings | File Templates.
 */
public class SettingsForm {

    private JPanel rootComponent;
    private JSplitPane splitPane;
    private JPanel leftPanel;
    private JPanel rightPanel;
    private JComboBox focusedFile;
    private JPanel pathListPanel;
    private JBList pathList;
    private CollectionListModel pathListModel;
    private JPanel classesListPanel;
    private JCheckBox usePSR0CheckBox;
    private JTextField textField1;
    private JCheckBox debugModeCheckBox;
    private CollectionListModel classesListModel;
    private JBList classesList;
    private KohanaClassesState kohanaClassesState;
    private KohanaClassesState kohanaClassesStateCopy;
    private PathsState pathState;
    private PathsState pathStateCopy;
    private Project project;
    private StorageHelper storageHelper;
    private Boolean kohanaPSRCopy;
    private Boolean debugModeCopy;
    private String factoriesListCopy;

    public SettingsForm(Project project, StorageHelper storageHelper) throws Exception {
        this.project = project;
        this.storageHelper = storageHelper;

        kohanaClassesState = new KohanaClassesState(project);

//        focusedFile.addItemListener(new ItemListener() {
//            public void itemStateChanged(ItemEvent e) {
//                String kcName = (String) classesList.getSelectedValue();
//                KohanaClassesState.KohanaClass kc = kohanaClassesStateCopy.getKohanaClassByName(kcName);
//                session.focusedFile = (String) focusedFile.getSelectedItem();
//                classesList.setSelectedValue(kcName, true);
//        }
//        });
    }

    public JComponent getRootComponent() {
        return rootComponent;
    }

    private void createUIComponents() {
        pathListModel = new CollectionListModel(ContainerUtil.newArrayList());
        pathList = new JBList(pathListModel);
        pathList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pathListPanel = ToolbarDecorator.createDecorator(pathList)
                .setAddAction(new AnActionButtonRunnable() {
                    public void run(AnActionButton button) {
                        doNewPath();
                    }
                }).setEditAction(new AnActionButtonRunnable() {
                    public void run(AnActionButton button) {
                        doEditPath();
                    }
                }).setRemoveAction(new AnActionButtonRunnable() {
                    public void run(AnActionButton button) {
                        doRemovePath();
                    }
//            }).addExtraAction(new AnActionButton("Export", AllIcons.Actions.Export) {
//                public void actionPerformed(AnActionEvent e) {
//                }
                })
                .setToolbarPosition(ActionToolbarPosition.TOP)
                .createPanel();

        new DoubleClickListener() {
            protected boolean onDoubleClick(MouseEvent e) {
                doEditPath();
                return true;
            }
        }.installOn(pathList);

        pathListModel.addListDataListener(new ListDataListener() {
            public void intervalAdded(ListDataEvent e) {
            }

            public void intervalRemoved(ListDataEvent e) {
            }

            public void contentsChanged(ListDataEvent e) {
                doReorderPath();
            }
        });

        classesListModel = new CollectionListModel(ContainerUtil.newArrayList());
        classesList = new JBList(classesListModel);
        classesList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        classesListPanel = ToolbarDecorator.createDecorator(classesList)
                .setAddAction(new AnActionButtonRunnable() {
                    public void run(AnActionButton button) {
                        doNewClass();
                    }
                }).setEditAction(new AnActionButtonRunnable() {
                    public void run(AnActionButton button) {
                        doEditClass();
                    }
                }).setRemoveAction(new AnActionButtonRunnable() {
                    public void run(AnActionButton button) {
                        doRemoveClass();
                    }
                })
                .setToolbarPosition(ActionToolbarPosition.TOP)
                .createPanel();

        new DoubleClickListener() {
            protected boolean onDoubleClick(MouseEvent e) {
                doEditClass();
                return true;
            }
        }.installOn(classesList);

        classesListModel.addListDataListener(new ListDataListener() {
            public void intervalAdded(ListDataEvent e) {
            }

            public void intervalRemoved(ListDataEvent e) {
            }

            public void contentsChanged(ListDataEvent e) {
                doReorderClasses();
            }
        });

//        ToolbarDecorator.findAddButton(classesListPanel).addCustomUpdater(new AnActionButtonUpdater() {
//            public boolean isEnabled(AnActionEvent e) {
//                return !pathList.isSelectionEmpty();
//            }
//        });

        pathList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                String sessionName = (String) pathList.getSelectedValue();
//                for(SessionState.Session session : sessionStateCopy.sessions) {
//                    if(session.name.equals(sessionName)) {
//                        DefaultComboBoxModel focusedFileModel = new DefaultComboBoxModel();
//                        fileListModel.removeAll();
//                        for (String path : session.files) {
//                            fileListModel.add(path);
//                            focusedFileModel.addElement(path);
//                        }
//                        focusedFileModel.setSelectedItem(session.focusedFile);
//                        focusedFile.setModel(focusedFileModel);
//                    }
//                }
            }
        });

    }

    protected void doNewPath() {
        final String selectedName = "~tmp";
        final PathsState.Path path = pathStateCopy.getOrCreatePathByName(selectedName);
        showEditPathDialog(path, new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        final EditPathDialog editPathDialog = (EditPathDialog) ((JComponent) e.getSource()).getRootPane().getParent();
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {

                                String pathName = editPathDialog.getPathName();
                                String pathPath = editPathDialog.getPathPath();

                                if (pathName != null && !pathName.isEmpty() && pathPath != null && !pathPath.isEmpty()) {
                                    path.name = pathName;
                                    path.path = pathPath;
                                } else {
                                    pathStateCopy.removePathByName(path.name);
                                }
                                mapStateToUI();
                            }
                        });
                    }
                }, new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        final EditPathDialog editPathDialog = (EditPathDialog) ((JComponent) e.getSource()).getRootPane().getParent();
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                pathStateCopy.removePathByName(path.name);
                                mapStateToUI();
                            }
                        });
                    }
                }
        );
    }

    protected void doEditPath() {
        final String selectedName = (String) pathList.getSelectedValue();
        final PathsState.Path path = pathStateCopy.getOrCreatePathByName(selectedName);
        showEditPathDialog(path, new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        final EditPathDialog editPathDialog = (EditPathDialog) ((JComponent) e.getSource()).getRootPane().getParent();
                        final String pathName = editPathDialog.getPathName();
                        final String pathPath = editPathDialog.getPathPath();
                        if (pathName != null && !pathName.isEmpty() && pathPath != null && !pathPath.isEmpty() && (!pathName.equals(path.name) || !pathPath.equals(path.path))) {
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    if (path.name.equals("~tmp")) {
                                        pathStateCopy.removePathByName(path.name);
                                    } else {
                                        path.name = pathName;
                                        path.path = pathPath;
                                    }
                                    mapStateToUI();
                                }
                            });
                        }
                    }
                }, new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                    }
                }
        );
    }

    private EditPathDialog showEditPathDialog(final PathsState.Path path, ActionListener actionOnOKListener, ActionListener actionOnCancelListener) {
        final EditPathDialog editPathDialog = path.name != null ? new EditPathDialog(project, path) : new EditPathDialog(project);

        ArrayList<String> paths = pathStateCopy.getPathsNames();
        editPathDialog.setSavedPaths(new ArrayList<String>(paths));
        editPathDialog.addOnOKListener(actionOnOKListener);
        editPathDialog.addOnCancelListener(actionOnCancelListener);
        editPathDialog.display(pathList);
        return editPathDialog;
    }

    protected void doRemovePath() {
        final String selectedName = (String) pathList.getSelectedValue();
        pathStateCopy.removePathByName(selectedName);
        mapStateToUI();
    }

    protected void doReorderPath() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ArrayList<String> pathNames = new ArrayList<String>();
                for (Object name : pathListModel.getItems()) {
                    pathNames.add((String) name);
                }
                pathStateCopy.setPathsOrder(pathNames);
                mapStateToUI();
            }
        });
    }

    protected void doNewClass() {
        final String className = "~tmp";
        final KohanaClassesState.KohanaClass kc = kohanaClassesStateCopy.getOrCreateKohanaClassByName(className);
        showEditClassesDialog(kc, new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        final EditClassesDialog editClassesDialog = (EditClassesDialog) ((JComponent) e.getSource()).getRootPane().getParent();
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                String clsName = editClassesDialog.getClassName();
                                String aliasMethods = editClassesDialog.getAliasMethods();
                                String namePattern = editClassesDialog.getNamePattern();
                                String path = editClassesDialog.getPath();

                                if (clsName != null && !clsName.isEmpty()) {
                                    kc.regexp = namePattern;
                                    kc.name = clsName;
                                    kc.methods = aliasMethods;
                                    kc.path = path;
                                } else {
                                    kohanaClassesStateCopy.removeKohanaClassByName(kc.name);
                                }


                                mapStateToUI();
                            }
                        });
                    }
                }, new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        final EditClassesDialog editClassesDialog = (EditClassesDialog) ((JComponent) e.getSource()).getRootPane().getParent();
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                kohanaClassesStateCopy.removeKohanaClassByName(kc.name);
                                mapStateToUI();
                            }
                        });
                    }
                }
        );
    }

    protected void doEditClass() {
        final String className = (String) classesList.getSelectedValue();
        final KohanaClassesState.KohanaClass kc = kohanaClassesStateCopy.getOrCreateKohanaClassByName(className);
        showEditClassesDialog(kc, new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        final EditClassesDialog editClassesDialog = (EditClassesDialog) ((JComponent) e.getSource()).getRootPane().getParent();
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                String clsName = editClassesDialog.getClassName();
                                String aliasMethods = editClassesDialog.getAliasMethods();
                                String namePattern = editClassesDialog.getNamePattern();
                                String path = editClassesDialog.getPath();

                                if (clsName != null && !clsName.isEmpty()) {
                                    if (kc.name.equals("~tmp")) {
                                        kohanaClassesStateCopy.removeKohanaClassByName(kc.name);
                                    } else {
                                        kc.regexp = namePattern;
                                        kc.name = clsName;
                                        kc.methods = aliasMethods;
                                        kc.path = path;
                                    }
                                }

                                mapStateToUI();
                            }
                        });
                    }
                }, new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                    }
                }
        );
    }

    private EditClassesDialog showEditClassesDialog(final KohanaClassesState.KohanaClass kc, ActionListener actionOnOKListener, ActionListener actionOnCancelListener) {
        final EditClassesDialog editClassesDialog = kc.name != null ? new EditClassesDialog(kc) : new EditClassesDialog();
        String className = (String) classesList.getSelectedValue();
        ArrayList<String> classes = kohanaClassesStateCopy.getClassesNames();
        editClassesDialog.setSavedClasses(new ArrayList<String>(classes));
        editClassesDialog.addOnOKListener(actionOnOKListener);
        editClassesDialog.addOnCancelListener(actionOnCancelListener);
        editClassesDialog.display(classesList);
        return editClassesDialog;
    }


    protected void doRemoveClass() {
        String className = (String) classesList.getSelectedValue();
        kohanaClassesStateCopy.removeKohanaClassByName(className);
        mapStateToUI();
    }

    protected void doReorderClasses() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ArrayList<String> classesNames = new ArrayList<String>();
                for (Object name : classesListModel.getItems()) {
                    classesNames.add((String) name);
                }
                kohanaClassesStateCopy.setKohanaClassOrder(classesNames);
                mapStateToUI();
            }
        });
    }

    public void setInitialState(KohanaClassesState kohanaClassesState, PathsState pathsState, Boolean kohanaPSR, Boolean debugMode, String factoriesList) {
        this.kohanaClassesState = kohanaClassesState;
        if (storageHelper.storageObject.classes != null) {
            this.kohanaClassesState.classes = storageHelper.storageObject.classes;
        }
        kohanaClassesStateCopy = kohanaClassesState.clone();

        this.pathState = pathsState;
        if (storageHelper.storageObject.paths != null) {
            this.pathState.paths = storageHelper.storageObject.paths;
        } else {
            this.pathState.scanDirs(this.kohanaClassesState, true);
        }

        pathStateCopy = pathsState.clone();
        kohanaPSRCopy = kohanaPSR;
        debugModeCopy = debugMode;
        factoriesListCopy = factoriesList;

        mapStateToUI();
    }

    public void mapStateToUI() {
        final String className = (String) classesList.getSelectedValue();
        final String pathName = (String) pathList.getSelectedValue();

        classesListModel.removeAll();
        pathListModel.removeAll();

        for (KohanaClassesState.KohanaClass kc : kohanaClassesStateCopy.classes) {
            classesListModel.add(kc.name);
        }

        for (PathsState.Path p : pathStateCopy.paths) {
            pathListModel.add(p.name);
        }

        if (className != null) {
            classesList.setSelectedValue(className, true);
        }

        if (pathName != null) {
            pathList.setSelectedValue(pathName, true);
        }
        textField1.setText(storageHelper.storageObject.factoriesList);
        usePSR0CheckBox.setSelected(storageHelper.storageObject.kohanaPSR);
        debugModeCheckBox.setSelected(storageHelper.storageObject.debugMode);
    }

    public KohanaClassesState getClassData() {
        return kohanaClassesStateCopy;
    }

    public PathsState getPathData() {
        return pathStateCopy;
    }

    public boolean isModified() {
        return !kohanaClassesState.equals(kohanaClassesStateCopy) || !pathState.equals(pathStateCopy) || !getFactoriesList().equals(factoriesListCopy) || isKohanaPSR() != kohanaPSRCopy || isDebugMode() != debugModeCopy;
    }

    public boolean isKohanaPSR() {
        return usePSR0CheckBox.isSelected();
    }

    public boolean isDebugMode() {
        return debugModeCheckBox.isSelected();
    }

    public String getFactoriesList() {
        return textField1.getText();
    }

    public void editNewTempClass(final KohanaClassesState.KohanaClass kc) {

        showEditClassesDialog(kc, new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        final EditClassesDialog editClassesDialog = (EditClassesDialog) ((JComponent) e.getSource()).getRootPane().getParent();
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                String clsName = editClassesDialog.getClassName();
                                String aliasMethods = editClassesDialog.getAliasMethods();
                                String namePattern = editClassesDialog.getNamePattern();
                                String path = editClassesDialog.getPath();

                                if (clsName != null && !clsName.isEmpty()) {
                                    if (kc.name.equals("~tmp")) {
                                        kohanaClassesStateCopy.removeKohanaClassByName(kc.name);
                                    } else {
                                        kc.regexp = namePattern;
                                        kc.name = clsName;
                                        kc.methods = aliasMethods;
                                        kc.path = path;
                                    }
                                }

                                mapStateToUI();
                            }
                        });
                    }
                }, new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                    }
                }
        );
    }
}
