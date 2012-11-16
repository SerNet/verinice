/*******************************************************************************
 * Copyright (c) 2012 Sebastian Hagedorn <sh@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.editors;

import org.apache.log4j.Logger;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;

import sernet.hui.swt.widgets.HitroUIComposite;

/**
 *
 */
public class ControlIsSelectedBehaviour implements IEditorBehavior {
    
    private static final Logger LOG = Logger.getLogger(ControlIsSelectedBehaviour.class);
    
    //is Control Selected Elements (from SNCA.xml)
    private static final String CONTROL_IS_SELECTED = "control_selected";
    private static final String CONTROL_SELECTION_REASON = "control_reason_for_selection";
    
    private HitroUIComposite huiComposite;

    public ControlIsSelectedBehaviour(HitroUIComposite composite){
        this.huiComposite = composite;
    }
    
    /**
     * Disables selectionReasonCombo
     */
    private SelectionListener selectionListenerSelectionReason = new SelectionListener() {
        
        @Override
        public void widgetSelected(SelectionEvent e) {
            Button button = (Button) e.getSource();
            if (LOG.isDebugEnabled()) {
                LOG.debug("control isSelected checkbox selected: " + button.getSelection() );
            }
            ControlIsSelectedBehaviour.this.huiComposite.setFieldEnabled(CONTROL_SELECTION_REASON, button.getSelection());
        }
        
        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            widgetSelected(e);
        }
    };
    
    
    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.bsi.editors.IEditorBehavior#init()
     */
    @Override
    public void init() {
        Control field = huiComposite.getField(CONTROL_IS_SELECTED);
        if(field!=null && field instanceof Button) {
            huiComposite.setFieldEnabled(CONTROL_SELECTION_REASON,((Button)field).getSelection());
        }
    }

    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.bsi.editors.IEditorBehavior#addBehavior()
     */
    @Override
    public void addBehavior() {
        huiComposite.addSelectionListener(CONTROL_IS_SELECTED, selectionListenerSelectionReason);
    }

    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.bsi.editors.IEditorBehavior#removeBehavior()
     */
    @Override
    public void removeBehavior() {
        huiComposite.removeSelectionListener(CONTROL_IS_SELECTED, selectionListenerSelectionReason);
    }

}
