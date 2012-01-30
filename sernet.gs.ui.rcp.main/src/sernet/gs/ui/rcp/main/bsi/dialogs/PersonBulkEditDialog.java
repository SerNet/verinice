/*******************************************************************************
 * Copyright (c) 2011 Sebastian Hagedorn <sh[at]sernet[dot]de>.
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
 *     Sebastian Hagedorn <sh[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.dialogs;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.editors.BSIElementEditor;
import sernet.gs.ui.rcp.main.bsi.editors.InputHelperFactory;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.swt.widgets.HitroUIComposite;
import sernet.snutils.DBException;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.model.common.configuration.Configuration;

/**
 * Dialog for editing account data bulk style
 */
public class PersonBulkEditDialog extends TitleAreaDialog {

    
    private static final Logger LOG = Logger.getLogger(PersonBulkEditDialog.class);
    
    private EntityType entType;
    private boolean b = false;
    private String title;
    private boolean isScopeOnly;
    private boolean useRules = true;
    private Entity entity;
    
    private PersonBulkEditDialog(Shell parent){
        super(parent);
        this.entType = entType;
        setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
        IAuthService authService = (IAuthService) VeriniceContext.get(VeriniceContext.AUTH_SERVICE);
        isScopeOnly = authService.isScopeOnly();
    }
    
    /**
     * @param shell
     * @param entType2
     * @param b
     * @param title
     * @param entity
     */
    public PersonBulkEditDialog(Shell shell, boolean b, String title) {
        this(shell);
        this.b = b;
        this.title = title;
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
    
    
    // tried overriding sernet.gs.ui.rcp.main.bsi.dialogs.AccountDialog.createDialogArea() here, but then always method in superclass will be executed also 
    @SuppressWarnings({ "restriction", "deprecation" })
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
            
            HitroUIComposite huiComposite = new HitroUIComposite(innerComposite, SWT.NULL, false);
            try {
                // is always Configuration here
                entity = new Entity(Configuration.TYPE_ID);
                
                String[] tags = BSIElementEditor.getEditorTags(); 
                
                boolean strict = Activator.getDefault().getPluginPreferences().getBoolean(PreferenceConstants.HUI_TAGS_STRICT);
                
                huiComposite.createView(entity, true, useRules, tags, strict);
               
                configureScopeOnly((Combo) huiComposite.getField(Configuration.PROP_SCOPE));
                
                InputHelperFactory.setInputHelpers(HUITypeFactory.getInstance().getEntityType(entity.getEntityType()), huiComposite);
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
    
    @Override
    protected void okPressed() {
        super.okPressed();
    }
    
    
    public Entity getEntity() {
        return entity;
    }
    
}
