/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.bpm.rcp;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import sernet.verinice.interfaces.bpm.IIsaExecutionProcess;
import sernet.verinice.interfaces.bpm.KeyValue;
import sernet.verinice.iso27k.rcp.ComboModel;
import sernet.verinice.iso27k.rcp.ComboModelLabelProvider;
import sernet.verinice.model.bpm.Messages;
/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class TaskFilterDialog extends Dialog {

    private static final String ALL_KEY = "all_elements"; //$NON-NLS-1$
    
    private final KeyMessage allKeyMessage = new KeyMessage(ALL_KEY,sernet.verinice.bpm.rcp.Messages.TaskFilterDialog_1);
    
    private Combo processCombo;
    private ComboModel<KeyMessage> processComboModel;
    
    private Combo typeCombo;
    private ComboModel<KeyMessage> typeComboModel;
    
    private String processKey;
    private String typeId;
    private boolean allTasks = false;
    private boolean allTasksEnabled = false;
    
    /**
     * @param parentShell
     * @param taskId 
     * @param processKey2 
     */
    protected TaskFilterDialog(Shell parentShell, String processKey, String taskId, boolean allTasks) {
        super(parentShell);
        int style = SWT.MAX | SWT.CLOSE | SWT.TITLE;
        style = style | SWT.BORDER | SWT.APPLICATION_MODAL;
        setShellStyle(style | SWT.RESIZE);
        this.processKey = processKey;
        this.typeId = taskId;
        this.allTasks = allTasks;
        typeComboModel = new ComboModel<KeyMessage>(new ComboModelLabelProvider<KeyMessage>() {
            @Override
            public String getLabel(KeyMessage object) {
                return object.getValue();
            }
        });
        processComboModel = new ComboModel<KeyMessage>(new ComboModelLabelProvider<KeyMessage>() {
            @Override
            public String getLabel(KeyMessage object) {
                return object.getValue();
            }
        });
        initCombos();
    }

    /**
     * @param container
     */
    private void addFormElements(Composite container) {
        Label processLabel = new Label(container, SWT.NONE);
        processLabel.setText(sernet.verinice.bpm.rcp.Messages.TaskFilterDialog_2);
        
        GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false);
        
        processCombo = new Combo(container, SWT.VERTICAL | SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
        processCombo.setLayoutData(gd);
        processCombo.setItems(processComboModel.getLabelArray());
        processCombo.select(0);
        processCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                processComboModel.setSelectedIndex(processCombo.getSelectionIndex());
                processKey=processComboModel.getSelectedObject().getKey();
                if(processKey.equals(ALL_KEY)) {
                    processKey=null;
                }
            }
        });
        if(processKey!=null) {
            processComboModel.setSelectedObject(new KeyMessage(processKey));
            processCombo.select(processComboModel.getSelectedIndex());
        }
        
        Label typeLabel = new Label(container, SWT.NONE);
        typeLabel.setText(sernet.verinice.bpm.rcp.Messages.TaskFilterDialog_3);
        
        typeCombo = new Combo(container, SWT.VERTICAL | SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
        typeCombo.setLayoutData(gd);
        typeCombo.setItems(typeComboModel.getLabelArray());
        typeCombo.select(0);
        typeComboModel.setSelectedObject(allKeyMessage);
        typeCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                typeComboModel.setSelectedIndex(typeCombo.getSelectionIndex());
                typeId=typeComboModel.getSelectedObject().getKey();
                if(typeId.equals(ALL_KEY)) {
                    typeId=null;
                }
            }
        });
        if(typeId!=null) {
            typeComboModel.setSelectedObject(new KeyMessage(typeId));
            typeCombo.select(typeComboModel.getSelectedIndex());
        }
        
        final Button[] radios = new Button[2];

        radios[0] = new Button(container, SWT.RADIO);
        radios[0].setSelection(!allTasks);
        radios[0].setText(sernet.verinice.bpm.rcp.Messages.TaskFilterDialog_4);
        radios[0].setEnabled(isAllTasksEnabled());
        radios[0].addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                allTasks = !radios[0].getSelection();
            }
        });

        radios[1] = new Button(container, SWT.RADIO);
        radios[1].setSelection(allTasks);
        radios[1].setText(sernet.verinice.bpm.rcp.Messages.TaskFilterDialog_5);
        radios[1].setEnabled(isAllTasksEnabled());
        radios[1].addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                allTasks = radios[1].getSelection();
            }
        });
    }

    public String getProcessKey() {
        return processKey;
    }

    public void setProcessKey(String processKey) {
        this.processKey = processKey;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public boolean isAllTasks() {
        return allTasks;
    }

    public boolean isAllTasksEnabled() {
        return allTasksEnabled;
    }

    public void setAllTasksEnabled(boolean allTasksEnabled) {
        this.allTasksEnabled = allTasksEnabled;
    }

    public void setAllTasks(boolean allTasks) {
        this.allTasks = allTasks;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout(1,true);
        container.setLayout(layout);      
        addFormElements(container);
        // Build the separator line
        Label separator = new Label(container, SWT.HORIZONTAL | SWT.SEPARATOR);
        separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        container.pack(); 
           
        return container;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(sernet.verinice.bpm.rcp.Messages.TaskFilterDialog_6);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#getInitialLocation(org.eclipse.swt.graphics.Point)
     */
    @Override
    protected Point getInitialLocation(Point initialSize) {
        final int yPointPadding = 20;
        Point cursorPoint = getShell().getDisplay().getCursorLocation();
        int x = cursorPoint.x - initialSize.x;
        int y = cursorPoint.y + yPointPadding;
        return new Point(x,y);
    }
    
    private void initCombos() {
        typeComboModel.add(new KeyMessage("iqm.task.setAssignee")); //$NON-NLS-1$
        typeComboModel.add(new KeyMessage("iqm.task.check")); //$NON-NLS-1$
        typeComboModel.add(new KeyMessage("icf.task.assign")); //$NON-NLS-1$
        typeComboModel.add(new KeyMessage("icf.task.execute")); //$NON-NLS-1$
        typeComboModel.add(new KeyMessage("icf.task.assign.deadline")); //$NON-NLS-1$
        typeComboModel.add(new KeyMessage("icf.task.assign.nr")); //$NON-NLS-1$
        typeComboModel.add(new KeyMessage("icf.task.obtainAdvise")); //$NON-NLS-1$
        typeComboModel.add(new KeyMessage("icf.task.check")); //$NON-NLS-1$
        typeComboModel.add(new KeyMessage("icf.task.assignAuditor")); //$NON-NLS-1$
        typeComboModel.add(new KeyMessage(IIsaExecutionProcess.TASK_SET_ASSIGNEE)); 
        typeComboModel.add(new KeyMessage(IIsaExecutionProcess.TASK_WRITE_PERMISSION)); 
        typeComboModel.add(new KeyMessage(IIsaExecutionProcess.TASK_IMPLEMENT)); 
        typeComboModel.add(new KeyMessage(IIsaExecutionProcess.TASK_ESCALATE));
        typeComboModel.add(new KeyMessage(IIsaExecutionProcess.TASK_CHECK_IMPLEMENTATION)); 
        typeComboModel.add(new KeyMessage("indi.task.assign")); //$NON-NLS-1$
        typeComboModel.add(new KeyMessage("indi.task.assign.deadline")); //$NON-NLS-1$
        typeComboModel.add(new KeyMessage("indi.task.assign.nr")); //$NON-NLS-1$
        typeComboModel.add(new KeyMessage("indi.task.execute")); //$NON-NLS-1$
        typeComboModel.add(new KeyMessage("indi.task.check")); //$NON-NLS-1$
        
        typeComboModel.sort();
        typeComboModel.add(0,allKeyMessage);
        
        processComboModel.add(new KeyMessage("isa-execution")); //$NON-NLS-1$
        processComboModel.add(new KeyMessage("isa-control-flow")); //$NON-NLS-1$
        processComboModel.add(new KeyMessage("isa-quality-management")); //$NON-NLS-1$
        processComboModel.add(new KeyMessage("individual-task")); //$NON-NLS-1$
        processComboModel.sort();
        processComboModel.add(0,allKeyMessage);
    }
    
    class KeyMessage extends KeyValue {
        public KeyMessage(String key) {
            super(key,Messages.getString(key));
        }
        public KeyMessage(String key,String message) {
            super(key,message);
        }
    }
    
}
