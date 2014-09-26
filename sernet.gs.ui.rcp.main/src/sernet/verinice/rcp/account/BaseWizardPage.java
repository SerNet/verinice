package sernet.verinice.rcp.account;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.ICommandService;

public abstract class BaseWizardPage extends WizardPage {

    private static final Logger LOG = Logger.getLogger(BaseWizardPage.class);
    
    private static final int MIN_WIDTH_TEXT = 100;
    
    private ICommandService commandService;
    
    public BaseWizardPage(String pageName) {
        super(pageName);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {
        try {
            final Composite composite = createEmptyComposite(parent);             
            initGui(composite);                  
            composite.pack(); 
            initData();   
            // Required to avoid an error in the system
            setControl(composite);
            setPageComplete(false);
        } catch(Exception e) {
            LOG.error("Error while opening person page.", e);
        }
    }

    protected abstract void initGui(Composite composite);
    
    protected abstract void initData() throws Exception;
    
    protected Composite createEmptyComposite(Composite parent) {
        final int marginWidth = 10;
        final Composite composite = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout(1, true);
        layout.marginWidth = marginWidth;
        composite.setLayout(layout);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true,true);
        composite.setLayoutData(gd);
        return composite;
    }
    
    protected void createLabel(Composite composite, String labelText) {
        Label label = new Label(composite, SWT.WRAP);
        label.setText(labelText);
    }

    protected Text createTextfield(Composite composite) {
        return createText(composite, SWT.BORDER);
    }
    
    protected Text createPasswordField(Composite composite) {
        return createText(composite, SWT.BORDER | SWT.PASSWORD);
    }
    
    protected Text createText(Composite composite, int style) {
        Text textfield = new Text(composite, style);
        textfield.setLayoutData(getTextGridData());
        return textfield;
    }
    
    protected Button createCheckbox(Composite composite, String label) {
        return createCheckbox(composite, label, false);
    }
    
    protected Button createCheckbox(Composite composite, String label, boolean selected) {
        Button cb = new Button(composite, SWT.CHECK);
        cb.setText(label);
        cb.setSelection(selected);
        return cb;
    }

    protected GridData getTextGridData() {
        GridData gridData = new GridData(SWT.FILL, SWT.NONE, true, false);
        gridData.minimumWidth = MIN_WIDTH_TEXT;
        return gridData;
    }
    
    protected String avoidEmptyStrings(String s) {
        String result = s;
        if(s!=null && s.isEmpty()) {
            result=null;
        }
        return result;
    }
    
    protected void setText(Text text, String s) {
        if(s!=null) {
            text.setText(s);
        }   
    }
    
    protected ICommandService getCommandService() {
        if (commandService == null) {
            commandService = createCommandServive();
        }
        return commandService;
    }

    private ICommandService createCommandServive() {
        return ServiceFactory.lookupCommandService();
    }

}
