/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.dialogs;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.actions.ConfigurationAction;
import sernet.gs.ui.rcp.main.bsi.editors.BSIElementEditor;
import sernet.gs.ui.rcp.main.bsi.editors.InputHelperFactory;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyType;
import sernet.hui.swt.widgets.HitroUIComposite;
import sernet.snutils.DBException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IRightsService;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.service.commands.CheckUserName;

/**
 * Dialog for user accounts which is opened by {@link ConfigurationAction}.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class AccountDialog extends TitleAreaDialog {
    
    private static transient Logger log = Logger.getLogger(AccountDialog.class);
    
    // usernames that should not be assigned by a user, use only lowercase here
    private static String[] reservedUsernames = new String[]{"admin"};
    
    private EntityType entType;
    private Entity entity = null;
    private boolean useRules = false;
    private String title = Messages.BulkEditDialog_0;
	private Text textPassword2;
	private String password2;
	private Text textPassword;
	private String password;
	private Text textName;
	private String name;
	private boolean isScopeOnly;
	
	private String initialUserName;
	
	private HitroUIComposite huiComposite;
	

    private AccountDialog(Shell parent, EntityType entType) {
        super(parent);
        setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
        this.entType = entType;

        isScopeOnly = getAuthService().isScopeOnly();
    }

    public AccountDialog(Shell shell, EntityType entType2, String title, Entity entity) {
        this(shell, entType2);
        useRules = true;
        this.title = title;
        this.entity = entity;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        final int shellWidth = 460;
        final int shellHeight = 640;
        final int cursorLocationXSubtrahend = 200;
        final int cursorLocationYSubtrahend = 400;
        newShell.setText(title);
        newShell.setSize(shellWidth, shellHeight);
        
        // open the window right under the mouse pointer:
        Point cursorLocation = Display.getCurrent().getCursorLocation();
        newShell.setLocation(new Point(cursorLocation.x-cursorLocationXSubtrahend, cursorLocation.y-cursorLocationYSubtrahend));
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        try {
        	setTitle(title);
        	setMessage(Messages.AccountDialog_0);
        	
            Composite container = (Composite) super.createDialogArea(parent);
    		GridData gd = new GridData(GridData.GRAB_HORIZONTAL);
    		gd.grabExcessHorizontalSpace = true;
    		gd.grabExcessVerticalSpace = true;
    		gd.horizontalAlignment = GridData.FILL;
    		gd.verticalAlignment = GridData.FILL;
    		container.setLayoutData(gd);

    		ScrolledComposite scrolledComposite = new ScrolledComposite(container, SWT.V_SCROLL);
            scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
            scrolledComposite.setExpandHorizontal(true);
            
            Composite innerComposite = new Composite (scrolledComposite, SWT.NONE); 
            scrolledComposite.setContent(innerComposite); 
            innerComposite.setLayoutData(new GridData (SWT.FILL, SWT.FILL,true, false)); 
            innerComposite.setLayout(new GridLayout (1, false));
    		
    		createPasswordComposite(innerComposite);
    		
            huiComposite = new HitroUIComposite(innerComposite, false);
            try {
                if (this.entity == null) {
                    entity = new Entity(entType.getId());
                }

                String[] tags = BSIElementEditor.getEditorTags(); 

                boolean strict = Activator.getDefault().getPluginPreferences().getBoolean(PreferenceConstants.HUI_TAGS_STRICT);

                // no validation check here, so empty list passed
                huiComposite.createView(entity, true, useRules, tags, strict, new ArrayList<String>(0), Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.USE_VALIDATION_GUI_HINTS));

                configureScopeOnly((Combo) huiComposite.getField(Configuration.PROP_SCOPE));
                
                configureIsAdmin((Combo)huiComposite.getField(Configuration.PROP_ISADMIN));
                
                InputHelperFactory.setInputHelpers(entType, huiComposite);
            } catch (DBException e) {
                ExceptionUtil.log(e, Messages.BulkEditDialog_1);
            }
            
            scrolledComposite.setVisible(true);
            Point size = innerComposite.computeSize(SWT.DEFAULT,SWT.DEFAULT);
            innerComposite.setSize(size); 
            container.layout(); 
            return container;
        } catch (Exception e) {
            getLog().error("Error while creating account dialog", e);
            return null;
        }
        
    }

	/**
     * @param field
     */
    private void configureScopeOnly(Combo combo) {
        if(isScopeOnly) {
            if(combo.getSelectionIndex()==-1) {
                combo.select(0);
            }
            combo.setEnabled(false);
        }
        combo.addSelectionListener(getScopeGroupSelectionListener());
    }
    
    private void configureIsAdmin(Combo combo){
        combo.addSelectionListener(getIsAdminSelectionListener());
    }
    
    /**
     *  ensures that admin groups are set / unset when isAdmin value is changed
     * @return
     */
    private SelectionListener getIsAdminSelectionListener(){
        return new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if(e.getSource() instanceof Combo){
                    Combo combo = (Combo)e.getSource();
                    String isAdminYES = HUITypeFactory.getInstance().getMessage(Configuration.PROP_ISADMIN_YES);
                    String isAdminNO  = HUITypeFactory.getInstance().getMessage(Configuration.PROP_ISADMIN_NO);
                    if(combo.getItem(combo.getSelectionIndex()).equals(isAdminYES)){
                        toggleIsAdminGroup(true);
                    } else if(combo.getItem(combo.getSelectionIndex()).equals(isAdminNO)){
                        toggleIsAdminGroup(false);
                    }
                }
            }
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        };
    }
    
    
    /**
     * ensures that usergroups are set when changed 
     * every user has to be in one of the 4 groups definend via IRightsService
     * @return
     */
    private SelectionListener getScopeGroupSelectionListener(){
        return new SelectionListener() {
            
            @Override
            public void widgetSelected(SelectionEvent e) {
                Combo combo = null;
                combo = (e.getSource() instanceof Combo) ? (Combo)e.getSource() : null;
                String scopeYes = HUITypeFactory.getInstance().getMessage(Configuration.PROP_SCOPE_YES);
                String scopeNo = HUITypeFactory.getInstance().getMessage(Configuration.PROP_SCOPE_NO);
                if(combo != null && combo.getItem(combo.getSelectionIndex()).equals(scopeYes)){
                    toggleScopeGroup(true);
                } else if(combo != null && combo.getItem(combo.getSelectionIndex()).equals(scopeNo)){
                    toggleScopeGroup(false);
                }
            }
            
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        };
    }
    
    /**
     * removes every default group from user configuration entity but groupNotToRemove
     * creates groupNotToRemove if not existent before
     * @param groupNotToRemove
     */
    private void removeAllButOneGroup(String groupNotToRemove){
        String[] groupNames = new String[]{IRightsService.ADMINDEFAULTGROUPNAME,
                IRightsService.ADMINSCOPEDEFAULTGROUPNAME,
                IRightsService.USERDEFAULTGROUPNAME,
                IRightsService.USERSCOPEDEFAULTGROUPNAME};
        PropertyType type = HitroUtil.getInstance().getTypeFactory().getPropertyType(Configuration.TYPE_ID, Configuration.PROP_ROLES);
        ArrayList<Property> propsToRemove = new ArrayList<Property>(0);
        Property notRemovedProp = null;
        for(Property prop : getEntity().getProperties(Configuration.PROP_ROLES).getProperties()){
            if(Arrays.asList(groupNames).contains(prop.getPropertyValue())){
                if(prop.getPropertyValue().equals(groupNotToRemove)){
                    notRemovedProp = prop;
                } else {
                    propsToRemove.add(prop);
                }
            }
        }
        for(Property prop : propsToRemove){
            removeProperty(Configuration.PROP_ROLES, prop);
        }
        if(notRemovedProp == null){
            type.getReferenceResolver().addNewEntity(this.entity, groupNotToRemove);
        }
    }
    
    
    private void toggleIsAdminGroup(boolean isAdmin){
        if(isAdmin && isScopeOnly()){
            removeAllButOneGroup(IRightsService.ADMINSCOPEDEFAULTGROUPNAME);
        } else if(isAdmin && !isScopeOnly()){
            removeAllButOneGroup(IRightsService.ADMINDEFAULTGROUPNAME);
        } else if(!isAdmin && isScopeOnly()){
            removeAllButOneGroup(IRightsService.USERSCOPEDEFAULTGROUPNAME);
        } else if(!isAdmin && !isScopeOnly()){
            removeAllButOneGroup(IRightsService.USERDEFAULTGROUPNAME);
        }
        toggleGroupText();
    }
    
    /**
     * returns true if scopeOnly-Combo has "Yes"-value selected
     * @return
     */
    private boolean isScopeOnly(){
        return ((Combo)huiComposite.getField(Configuration.PROP_SCOPE)).
                getItem(((Combo)huiComposite.getField(Configuration.PROP_SCOPE)).
                        getSelectionIndex()).equals(HUITypeFactory.getInstance().
                                getMessage(Configuration.PROP_SCOPE_YES));
    }
    
    /**
     * returns true if admin-Combo has "Yes"-value selected
     * @return
     */
    private boolean isAdmin(){
        return((Combo)huiComposite.getField(Configuration.PROP_ISADMIN)).
                getItem(((Combo)huiComposite.getField(Configuration.PROP_ISADMIN)).
                        getSelectionIndex()).equals(HUITypeFactory.getInstance().
                                getMessage(Configuration.PROP_ISADMIN_YES));
                
    }
    /**
     * removes defined property from current entity
     * @param propertyType
     * @param property
     */
    private void removeProperty(String propertyType, Property property){
        getEntity().getProperties(propertyType).getProperties().remove(property);
    }
    
    private boolean isPropertySet(String propertyID){
        return getEntity() != null &&
                getEntity().getProperties(propertyID) != null &&
                getEntity().getProperties(propertyID).getProperty(0) != null &&
                getEntity().getProperties(propertyID).getProperty(0).getPropertyValue() != null;
    }
    
    /**
     * 
     */
    private void toggleScopeGroup(boolean isScope) {
            if(isScope && isAdmin()){
                removeAllButOneGroup(IRightsService.ADMINSCOPEDEFAULTGROUPNAME);
            }
            if(!isScope && isAdmin()){
                removeAllButOneGroup(IRightsService.ADMINDEFAULTGROUPNAME);
            }
            if(isScope && !isAdmin()){
                removeAllButOneGroup(IRightsService.USERSCOPEDEFAULTGROUPNAME);
            }
            if(!isScope && !isAdmin()){
                removeAllButOneGroup(IRightsService.USERDEFAULTGROUPNAME);
            }
            toggleGroupText();
    }
    
    
    private void toggleGroupText(){
        Text text = (Text)huiComposite.getField(Configuration.PROP_ROLES);
        StringBuilder sb = new StringBuilder();
        for(Property prop : getEntity().getProperties(Configuration.PROP_ROLES).getProperties()){
            if(sb.length() == 0){
                sb.append(prop.getPropertyValue());
            } else {
                sb.append(" / " + prop.getPropertyValue());
            }
        }
        text.setText(sb.toString());
    }
    


    private void createPasswordComposite(final Composite composite) {
		GridData gd = new GridData(GridData.GRAB_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.horizontalAlignment = GridData.FILL;
		gd.verticalAlignment = GridData.FILL;
		final Composite compositePassword = new Composite(composite, SWT.NONE);
		GridLayout layoutPassword = new GridLayout(2, false);
		compositePassword.setLayout(layoutPassword);
		compositePassword.setLayoutData(gd);
		
		Label labelName = new Label(compositePassword, SWT.NONE);
		labelName.setText(Messages.AccountDialog_1);
		
		textName = new Text(compositePassword, SWT.BORDER | SWT.SINGLE);
		GridData gdText = new GridData(GridData.GRAB_HORIZONTAL);
		gdText.grabExcessHorizontalSpace = true;
		gdText.horizontalAlignment = GridData.FILL;
		textName.setLayoutData(gdText);
		
		Label labelPassword = new Label(compositePassword, SWT.NONE);
		labelPassword.setText(Messages.AccountDialog_2);
		
		textPassword = new Text(compositePassword, SWT.BORDER | SWT.SINGLE | SWT.PASSWORD);
		textPassword.setLayoutData(gdText);
		
		Label labelPassword2 = new Label(compositePassword, SWT.NONE);
		labelPassword2.setText(Messages.AccountDialog_3);
		
		textPassword2 = new Text(compositePassword, SWT.BORDER | SWT.SINGLE | SWT.PASSWORD);
		textPassword2.setLayoutData(gdText);
		
		if(isPropertySet(Configuration.PROP_USERNAME)) {
			textName.setText(getEntity().getProperties(Configuration.PROP_USERNAME).getProperty(0).getPropertyValue());
			initialUserName = textName.getText();
		}
	}
	
	@Override
    protected void okPressed() {
		password=textPassword.getText();
		password2=textPassword2.getText();
		name=textName.getText();
		if(validateInput()){
		    super.okPressed();
		}
	}
	
	public String getPassword() {
		return password;
	}
	
	public String getPassword2() {
		return password2;
	}
	
	public String getUserName() {
		return name;
	}

	public Entity getEntity() {
        return entity;
    }
	
	public static Logger getLog(){
	    if(log == null){
	        log = Logger.getLogger(AccountDialog.class);
	    }
	    return log;
	}
	
	private boolean validateInput(){
	    return checkUserName() && checkPassword1() && checkPassword2();
	}
	
	private boolean checkPassword1(){
        String pwd = textPassword.getText();
        if(pwd.matches(".*[ÄäÖöÜüß€]+.*")) { //$NON-NLS-1$
            MessageDialog.openWarning(AccountDialog.this.getShell(), Messages.AccountDialog_5, Messages.AccountDialog_6);
            textPassword.setText(""); //$NON-NLS-1$
            textPassword2.setText(""); //$NON-NLS-1$
            textPassword.setFocus();
            return false;
        } else {
            return true;
        }
	}
	
	private boolean checkPassword2(){
	    boolean passwordsEqual = textPassword.getText().equals(textPassword2.getText());
	    boolean passwordEmpty = textPassword.getText().isEmpty() && !isPasswordSet();
	    boolean passwordsManagedByVerinice = getAuthService().isHandlingPasswords();
	    if(!passwordsEqual || (passwordsManagedByVerinice && passwordEmpty)){
	        if(!passwordsEqual){
	            toggleValidationError(textPassword, Messages.AccountDialog_8);
	        } else if(passwordEmpty){
	            toggleValidationError(textPassword, Messages.AccountDialog_11);
	        }
	        return false;
	    } else {
	        textPassword2.setToolTipText("");
	        return true;
	    }
	}
	
	private boolean isPasswordSet(){
	    if(isPropertySet(Configuration.PROP_PASSWORD)){
	        String pw = getEntity().getProperties(Configuration.PROP_PASSWORD).getProperty(0).getPropertyValue();
	        if(pw != null && !pw.isEmpty()){
	            return true;
	        }
	    } 
	    return false;
	}
	
	private boolean checkUserName(){
	    boolean retVal = false;
	    String enteredName = textName.getText();
	    final boolean noPasswordEntered = enteredName.isEmpty();
	    if(isReservedUsername(enteredName)){
	        toggleValidationError(textName, Messages.AccountDialog_7);
            return false;
	    }
        if((initialUserName != null && !initialUserName.equals(enteredName)) ||
                (initialUserName == null && !enteredName.isEmpty())){
            return checkUserNameOnServer(enteredName, noPasswordEntered);
        } else if (enteredName.equals(initialUserName)){
            return true;
        } else if(initialUserName == null && noPasswordEntered){
            toggleValidationError(textName, Messages.AccountDialog_10);
        }
        return retVal;
	}

    private boolean checkUserNameOnServer(String enteredName, final boolean noPasswordEntered) {
        CheckUserName command = new CheckUserName(enteredName);
        try {
            command = ServiceFactory.lookupCommandService().executeCommand(command);
            boolean userNameExists = command.getResult();
            if(userNameExists){
                toggleValidationError(textName, Messages.AccountDialog_7);
                return false;
            } else if(noPasswordEntered){
                toggleValidationError(textName, Messages.AccountDialog_10);
                return false;
            } else {
                textName.setToolTipText("");
                return true;
            }
        } catch (CommandException e1) {
            getLog().error("Error while checking username", e1);
        }
        return false;
    }
	
	private boolean isReservedUsername(String username){
	    for(String s : reservedUsernames){
	        if(username.equalsIgnoreCase(s)){
	            return true;
	        }
	    }
	    return false;
	}
	
	private void toggleValidationError(final Text control, final String dialogMsg){
	    Display.getDefault().asyncExec(new Runnable(){
	       @Override
	       public void run(){
	           control.setToolTipText(dialogMsg);
	           control.selectAll();
	           MessageDialog.openWarning(getParentShell(), Messages.AccountDialog_9, dialogMsg);
	           control.setFocus();
	           control.forceFocus();
	           
	       }
	    });
	}

    /**
     * @return the authService
     */
    public IAuthService getAuthService() {
        return (IAuthService) VeriniceContext.get(VeriniceContext.AUTH_SERVICE);
    }
	
}
