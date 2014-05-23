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

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.editors.BSIElementEditor;
import sernet.gs.ui.rcp.main.bsi.editors.InputHelperFactory;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.swt.widgets.HitroUIComposite;
import sernet.snutils.DBException;

public class BulkEditDialog extends Dialog {

    private static final Logger LOG = Logger.getLogger(BulkEditDialog.class);

    private EntityType entType;
    private Entity entity = null;
    private boolean useRules = false;
    private String title = Messages.BulkEditDialog_0;
    
    public BulkEditDialog(Shell parent, EntityType entType) {
        super(parent);
        setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
        this.entType = entType;
    }

    public BulkEditDialog(Shell shell, EntityType entType2, String title, Entity entity) {
        this(shell, entType2);
        useRules = true;
        this.title = title;
        this.entity = entity;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        final int shellWidth = 440;
        final int shellHeight = 800;
        final int shellLocationXSubtrahend = 200;
        final int shellLocationYSubtrahend = 400;
        newShell.setText(title);
        newShell.setSize(shellWidth, shellHeight);
        
        // open the window right under the mouse pointer:
        Point cursorLocation = Display.getCurrent().getCursorLocation();
        newShell.setLocation(new Point(cursorLocation.x-shellLocationXSubtrahend, cursorLocation.y-shellLocationYSubtrahend));
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        try {
            Composite container = (Composite) super.createDialogArea(parent);
            container.setLayout(new FillLayout());      
            
            HitroUIComposite huiComposite = new HitroUIComposite(container, false);        
            
            try {
                if (this.entity == null) {
                    entity = new Entity(entType.getId());
                }
                
                String[] tags = BSIElementEditor.getEditorTags();
                boolean strict = Activator.getDefault().getPluginPreferences().getBoolean(PreferenceConstants.HUI_TAGS_STRICT);
                
                // no validation here, so empty list is passed
                huiComposite.createView(entity, true, useRules, tags, strict, new ArrayList<String>(0), Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.USE_VALIDATION_GUI_HINTS));
                InputHelperFactory.setInputHelpers(entType, huiComposite);
                return huiComposite;
            } catch (DBException e) {
                ExceptionUtil.log(e, Messages.BulkEditDialog_1);
            }

        } catch (Exception e) {
            LOG.error("Error creating BulkeditDialog", e);
        }
        return null;
    }

    public Entity getEntity() {
        return entity;
    }

}
