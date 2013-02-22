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

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import sernet.gs.ui.rcp.main.Activator;
import sernet.verinice.interfaces.bpm.IndividualServiceParameter;
import sernet.verinice.iso27k.rcp.ComboModel;
import sernet.verinice.iso27k.rcp.ComboModelLabelProvider;

/**
 * Wizard page of wizard {@link IndividualProcessWizard}.
 * User enters a title and a description of the task on this page.
 * There is combo-box to select a task template.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class DescriptionPage extends WizardPage {

    public static final String NAME = "DESCRIPTION_PAGE"; //$NON-NLS-1$
    
    private static final Logger LOG = Logger.getLogger(DescriptionPage.class);
      
    private Combo templateCombo;
    private ComboModel<IndividualServiceParameter> templateComboModel;
    private Text text;  
    private Text textArea;
    private Button deleteButton;
    private Button overwriteTemplateCheckbox;
    
    private Map<String, IndividualServiceParameter> templateMap;
    
    private Preferences preferences;
    private Preferences bpmPreferences;
    
    private String elementTitle;
    
    private String taskTitle;

    private String taskDescription;
    
    private boolean overwriteTemplate = true;
    
    private static final int PAGE_WIDTH = 600;
    
    /**
     * @param elementTitle Title of affected element for the task
     */
    protected DescriptionPage(String elementTitle) {
        super(NAME);
        setTitle(Messages.DescriptionPage_0);
        setMessage(Messages.DescriptionPage_2);
        this.elementTitle = elementTitle;
        setControl(text);
        initComboValues();
    }

    private void addFormElements(Composite composite) {
        final int pageWidthSubtrahend = 30;
        final int gdHeightHint = 150;
        if(elementTitle!=null) {
            final Label objectLabel = new Label(composite, SWT.NONE);
            objectLabel.setText(Messages.DescriptionPage_3);
            final Label object = new Label(composite, SWT.NONE);
           
            FontData[] fD = object.getFont().getFontData();
            for (int i = 0; i < fD.length; i++) {
                fD[i].setStyle(SWT.BOLD);
            }
            Font newFont = new Font(getShell().getDisplay(),fD);
            object.setFont(newFont);
            GC gc = new GC(object);
            Point size = gc.textExtent(elementTitle);
            if(size.x > PAGE_WIDTH - pageWidthSubtrahend) {
                elementTitle = trimTitleByWidthSize(gc,elementTitle,PAGE_WIDTH-pageWidthSubtrahend) + "..."; //$NON-NLS-1$
            }
            object.setText(elementTitle);
        }
        
        final Label templateLabel = new Label(composite, SWT.NONE);
        templateLabel.setText(Messages.DescriptionPage_4);
        
        Composite templateComposite = new Composite(composite, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        templateComposite.setLayout(layout);
        GridData gd = new GridData(SWT.FILL, SWT.TOP, true,false);       
        templateComposite.setLayoutData(gd);
        
        templateCombo = new Combo(templateComposite, SWT.VERTICAL | SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
        gd = new GridData(SWT.FILL, SWT.TOP, true, false);
        templateCombo.setLayoutData(gd);
           
        deleteButton = new Button(templateComposite, SWT.PUSH);
        deleteButton.setText(Messages.DescriptionPage_1);
        deleteButton.setEnabled(false);
        deleteButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                deleteTemplate();
            }
        });
           
        overwriteTemplateCheckbox = new Button(composite, SWT.CHECK);
        overwriteTemplateCheckbox.setText(Messages.DescriptionPage_7);
        overwriteTemplateCheckbox.setSelection(true);
        overwriteTemplateCheckbox.setEnabled(false);
        overwriteTemplateCheckbox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Button checkBox = (Button) e.getSource();
                overwriteTemplate = checkBox.getSelection();
            }
        });
        
        templateCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                templateComboModel.setSelectedIndex(templateCombo.getSelectionIndex());
                IndividualServiceParameter template=templateComboModel.getSelectedObject();
                ((IndividualProcessWizard) getWizard()).setTemplate(template);
                deleteButton.setEnabled(true);
                overwriteTemplateCheckbox.setEnabled(true);
            }
        });
        showComboValues();
        
        final Label titleLabel = new Label(composite, SWT.NONE);
        titleLabel.setText(Messages.DescriptionPage_5);
        text = new Text(composite, SWT.BORDER | SWT.SINGLE);
        gd = new GridData(SWT.FILL, SWT.TOP, true, false);
 
        text.setLayoutData(gd);
        text.addKeyListener(new KeyListener() {          
            @Override
            public void keyReleased(KeyEvent e) {
                taskTitle = text.getText();
                setPageComplete(isValid());
            }
            
            @Override
            public void keyPressed(KeyEvent e) {}
        });
        
        final Label descriptionLabel = new Label(composite, SWT.NONE);
        descriptionLabel.setText(Messages.DescriptionPage_6);
        int style = SWT.MULTI | SWT.LEAD | SWT.BORDER;
        textArea = new Text(composite, style | SWT.WRAP | SWT.V_SCROLL );
        gd = new GridData(SWT.FILL, SWT.TOP, true, false);
        gd.heightHint = gdHeightHint;
        
        textArea.setLayoutData(gd);
        textArea.addKeyListener(new KeyListener() {            
            @Override
            public void keyReleased(KeyEvent e) {
                taskDescription = textArea.getText();
                setPageComplete(isValid());
            }            
            @Override
            public void keyPressed(KeyEvent e) {}
        });
    }

    public void showComboValues() {
        templateCombo.setItems(templateComboModel.getLabelArray());
        if(templateComboModel.isEmpty()) {
            templateCombo.setEnabled(false);
            deleteButton.setEnabled(false);
            overwriteTemplateCheckbox.setEnabled(false);
        }
    }
    
    protected void deleteTemplate() {
        try {
            IndividualServiceParameter internalTemplate = templateComboModel.getSelectedObject();
            if(internalTemplate!=null) {
                getTemplateMap().remove(internalTemplate.getTitle());
                String value = IndividualProcessWizard.toString((Serializable) getTemplateMap());
                getBpmPreferences().put(IndividualProcessWizard.PREFERENCE_NAME, value);
                getPreferences().flush();
                templateComboModel.removeSelected();
                showComboValues();
                deleteButton.setEnabled(false);
                overwriteTemplateCheckbox.setEnabled(false);
            }
        } catch(Exception e) {
            LOG.error("Error while deleting template", e); //$NON-NLS-1$
            setErrorMessage(Messages.DescriptionPage_9);
        }
    }

    private void initComboValues() {
        templateComboModel = new ComboModel<IndividualServiceParameter>(new ComboModelLabelProvider<IndividualServiceParameter>() {
            @Override
            public String getLabel(IndividualServiceParameter template) {
                return template.getTitle();
            }
        });
        try {          
            for (String name : getTemplateMap().keySet()) {
                templateComboModel.add(getTemplateMap().get(name));
            }
            if(!templateComboModel.isEmpty()) {
                templateComboModel.sort();
            }
        } catch (Exception e) {
            LOG.error("Error while loading templates", e); //$NON-NLS-1$
            setErrorMessage(Messages.DescriptionPage_8);
            removeTemplatesFromPreferences();
        }
    }
    
    private Map<String, IndividualServiceParameter> getTemplateMap() {
        if(templateMap==null) {
            templateMap = loadTemplateMap(); 
        }
        return templateMap;
    }
    
    private Map<String, IndividualServiceParameter> loadTemplateMap() {
       String value = getBpmPreferences().get(IndividualProcessWizard.PREFERENCE_NAME, null);           
        Map<String, IndividualServiceParameter> map;
        if(value!=null) {
            map = (Hashtable<String, IndividualServiceParameter>) IndividualProcessWizard.fromString(value);
        } else {
            map = new Hashtable<String, IndividualServiceParameter>();
        }
        return map;
    }
    
    public void removeTemplatesFromPreferences() {
        if(getBpmPreferences()!=null && getPreferences()!=null) {
            getBpmPreferences().remove(IndividualProcessWizard.PREFERENCE_NAME);
            try {
                getPreferences().flush();
            } catch (BackingStoreException e1) {
                LOG.error("Error while flushing preferences.", e1); //$NON-NLS-1$
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {  
        final Composite composite = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout(1, true);
        composite.setLayout(layout);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true,true);
        composite.setLayoutData(gd);  
        
        addFormElements(composite);
        
        composite.pack(); 
        
        // Required to avoid an error in the system
        setControl(composite);
        setPageComplete(false);
    }
    
    public boolean isValid() {
        boolean valid = true;
        setErrorMessage(null);
        if(taskTitle==null || taskTitle.isEmpty()) {
            valid = false;
            setErrorMessage(Messages.DescriptionPage_10);
        }
        if(valid && (taskDescription==null || taskDescription.isEmpty())) {
            valid = false;
            setErrorMessage(Messages.DescriptionPage_11);
        }
        return valid;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
     */
    @Override
    public boolean isPageComplete() {
        boolean complete = super.isPageComplete();
        if (LOG.isDebugEnabled()) {
            LOG.debug("page complete: " + complete); //$NON-NLS-1$
        }
        return complete;
    }
    
    private String trimTitleByWidthSize(GC gc, String elementTitle, int width) {
        String newTitle = elementTitle.substring(0, elementTitle.length()-1);
        Point size = gc.textExtent(newTitle + "..."); //$NON-NLS-1$
        if(size.x>width) {
            newTitle = trimTitleByWidthSize(gc, newTitle, width);
        }
        return newTitle;
    }
    
    public String getTaskTitle() {
        return taskTitle;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
        this.text.setText(taskTitle);
        setPageComplete(isValid());
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
        this.textArea.setText(taskDescription);
        setPageComplete(isValid());
    }
    
    public boolean isOverwriteTemplate() {
        return overwriteTemplate;
    }

    public Preferences getPreferences() {
        if(preferences==null) {
            preferences = ConfigurationScope.INSTANCE.getNode(Activator.PLUGIN_ID);
        }
        return preferences;
    }

    public Preferences getBpmPreferences() {
        if(bpmPreferences==null) {
            bpmPreferences = getPreferences().node(IndividualProcessWizard.PREFERENCE_NODE_NAME);
        }
        return bpmPreferences;
    }

}
