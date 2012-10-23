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
public class ControlgroupRoomNetworkBehaviour implements IEditorBehavior {
    
    private static final Logger LOG = Logger.getLogger(ControlgroupRoomNetworkBehaviour.class);
    
    //roomElements (from SNCA.xml)
    public static final String ROOM_INSPECTIONDATE = "controlgroup_isroom_dateofinspection";
    public static final String ROOM_CATEGORISATION = "controlgroup_isroom_categorisation";
    public static final String ROOM_GOTCONCEPT = "controlgroup_is_room_gotConcept";
    public static final String CONTROLGROUP_ISROOM = "controlgroup_is_room";
    
    private HitroUIComposite huiComposite;
    
    public ControlgroupRoomNetworkBehaviour(HitroUIComposite huiComposite) {
        this.huiComposite = huiComposite;
    }
    
    /**
     * Disables confidentiality field if inheritance checkbox is enabled
     */
    SelectionListener selectionListenerRoomInspectionDate = new SelectionListener() {
        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            widgetSelected(e);
        }
        @Override
        public void widgetSelected(SelectionEvent e) {      
            Button button = (Button) e.getSource();
            if (LOG.isDebugEnabled()) {
                LOG.debug("confidentiality checkbox selected: " + button.getSelection() );
            }
            ControlgroupRoomNetworkBehaviour.this.huiComposite.setFieldEnabled(ROOM_INSPECTIONDATE,button.getSelection());
        }
    };

    /**
     * Disables confidentiality field if inheritance checkbox is enabled
     */
    SelectionListener selectionListenerRoomCategorisation = new SelectionListener() {
        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            // empty
        }
        @Override
        public void widgetSelected(SelectionEvent e) {      
            Button button = (Button) e.getSource();
            if (LOG.isDebugEnabled()) {
                LOG.debug("confidentiality checkbox selected: " + button.getSelection() );
            }
            ControlgroupRoomNetworkBehaviour.this.huiComposite.setFieldEnabled(ROOM_CATEGORISATION,button.getSelection());
        }
    };
    
    /**
     * Disables confidentiality field if inheritance checkbox is enabled
     */
    SelectionListener selectionListenerRoomGotConcept = new SelectionListener() {
        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            // empty
        }
        @Override
        public void widgetSelected(SelectionEvent e) {      
            Button button = (Button) e.getSource();
            if (LOG.isDebugEnabled()) {
                LOG.debug("confidentiality checkbox selected: " + button.getSelection() );
            }
            ControlgroupRoomNetworkBehaviour.this.huiComposite.setFieldEnabled(ROOM_GOTCONCEPT,button.getSelection());
        }
    };    
    
    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.bsi.editors.IEditorBehavior#init()
     */
    @Override
    public void init() {
        Control field = huiComposite.getField(CONTROLGROUP_ISROOM);
        if(field!=null && field instanceof Button) {
            huiComposite.setFieldEnabled(ROOM_CATEGORISATION,((Button)field).getSelection());
        }
        field = huiComposite.getField(CONTROLGROUP_ISROOM);
        if(field!=null && field instanceof Button) {
            huiComposite.setFieldEnabled(ROOM_GOTCONCEPT,((Button)field).getSelection());
        }
        field = huiComposite.getField(CONTROLGROUP_ISROOM);
        if(field!=null && field instanceof Button) {
            huiComposite.setFieldEnabled(ROOM_INSPECTIONDATE,((Button)field).getSelection());
        }
    }

    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.bsi.editors.IEditorBehavior#addBehavior()
     */
    @Override
    public void addBehavior() {
        huiComposite.addSelectionListener(CONTROLGROUP_ISROOM,selectionListenerRoomCategorisation);
        huiComposite.addSelectionListener(CONTROLGROUP_ISROOM,selectionListenerRoomGotConcept);
        huiComposite.addSelectionListener(CONTROLGROUP_ISROOM,selectionListenerRoomInspectionDate);
    }

    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.bsi.editors.IEditorBehavior#removeBehavior()
     */
    @Override
    public void removeBehavior() {
        huiComposite.removeSelectionListener(CONTROLGROUP_ISROOM,selectionListenerRoomCategorisation);
        huiComposite.removeSelectionListener(CONTROLGROUP_ISROOM,selectionListenerRoomGotConcept);
        huiComposite.removeSelectionListener(CONTROLGROUP_ISROOM,selectionListenerRoomInspectionDate);
    }

}
