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

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
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
import sernet.gs.ui.rcp.main.bsi.editors.BSIElementEditor;
import sernet.gs.ui.rcp.main.bsi.editors.InputHelperFactory;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.swt.widgets.HitroUIComposite;
import sernet.hui.swt.widgets.SingleSelectionControl;
import sernet.snutils.DBException;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.interfaces.IAuthService;

public class AccountDialog extends TitleAreaDialog {
    
    private static final Logger LOG = Logger.getLogger(AccountDialog.class);
    
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

    private AccountDialog(Shell parent, EntityType entType) {
        super(parent);
        setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
        this.entType = entType;
        IAuthService authService = (IAuthService) VeriniceContext.get(VeriniceContext.AUTH_SERVICE);
        isScopeOnly = authService.isScopeOnly();
    }

    public AccountDialog(Shell shell, EntityType entType2, boolean b, String title, Entity entity) {
        this(shell, entType2);
        useRules = true;
        this.title = title;
        this.entity = entity;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(title);
        newShell.setSize(460, 640);
        
        // open the window right under the mouse pointer:
        Point cursorLocation = Display.getCurrent().getCursorLocation();
        newShell.setLocation(new Point(cursorLocation.x-200, cursorLocation.y-400));
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        try {
        	setTitle(title);
        	setMessage(Messages.AccountDialog_0);
        	
            Composite container = (Composite) super.createDialogArea(parent);
            GridLayout layoutRoot = (GridLayout) container.getLayout();
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
    		
            HitroUIComposite huiComposite = new HitroUIComposite(innerComposite, SWT.NULL, false);
            try {
                if (this.entity == null) {
                    entity = new Entity(entType.getId());
                }
                
                String[] tags = BSIElementEditor.getEditorTags(); 
                
                boolean strict = Activator.getDefault().getPluginPreferences().getBoolean(PreferenceConstants.HUI_TAGS_STRICT);
     
                huiComposite.createView(entity, true, useRules, tags, strict);
                
                configureScopeOnly((Combo) huiComposite.getField(Configuration.PROP_SCOPE));
               
                
                InputHelperFactory.setInputHelpers(entType, huiComposite);
                //return huiComposite;
            } catch (DBException e) {
                ExceptionUtil.log(e, Messages.BulkEditDialog_1);
            }
            
            scrolledComposite.setVisible(true);
            Point size = innerComposite.computeSize(SWT.DEFAULT,SWT.DEFAULT);
            innerComposite.setSize(size); 
            container.layout(); 
            return container;
        } catch (Exception e) {
            LOG.error("Error while creating account dialog", e);
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
		textPassword.addFocusListener(new FocusListener() {
            
            @Override
            public void focusLost(FocusEvent e) {
                String pwd = textPassword.getText();
                if(pwd.matches(".*[ÄäÖöÜüß€]+.*")) { //$NON-NLS-1$
                    MessageDialog.openWarning(AccountDialog.this.getShell(), Messages.AccountDialog_5, Messages.AccountDialog_6);
                    textPassword.setText(""); //$NON-NLS-1$
                    textPassword2.setText(""); //$NON-NLS-1$
                    textPassword.setFocus();
                }
            }
            
            @Override
            public void focusGained(FocusEvent e) {
                // nothing to do
            }
        });
		
		Label labelPassword2 = new Label(compositePassword, SWT.NONE);
		labelPassword2.setText(Messages.AccountDialog_3);
		
		textPassword2 = new Text(compositePassword, SWT.BORDER | SWT.SINGLE | SWT.PASSWORD);
		textPassword2.setLayoutData(gdText);
		
		if(getEntity()!=null 
			&& getEntity().getProperties(Configuration.PROP_USERNAME)!=null
			&& getEntity().getProperties(Configuration.PROP_USERNAME).getProperty(0)!=null) {
			textName.setText(getEntity().getProperties(Configuration.PROP_USERNAME).getProperty(0).getPropertyValue());
		}
	}
	
	@Override
	protected void okPressed() {
		password=textPassword.getText();
		password2=textPassword2.getText();
		name=textName.getText();
		super.okPressed();
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

}
