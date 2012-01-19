/*******************************************************************************
 * Copyright (c) 2011 Daniel Murygin.
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
package sernet.gs.ui.rcp.main.bsi.editors;

import org.apache.log4j.Logger;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;

import sernet.hui.swt.widgets.HitroUIComposite;

/**
 * Adds (and removes) inheritance behavior
 * to an {@link HitroUIComposite}.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class InheritanceBehavior implements IEditorBehavior {

    private static final Logger LOG = Logger.getLogger(InheritanceBehavior.class);

    // values is taken from asset definition in SNCA.xml
    public static final  String ASSET_METHOD_CONFIDEBTIALITY =  "asset_value_method_confidentiality"; //$NON-NLS-1$
    public static final  String ASSET_CONFIDEBTIALITY =  "asset_value_confidentiality"; //$NON-NLS-1$
    public static final  String ASSET_METHOD_INTEGRITY =  "asset_value_method_integrity"; //$NON-NLS-1$
    public static final  String ASSET_INTEGRITY =  "asset_value_integrity"; //$NON-NLS-1$
    public static final  String ASSET_METHOD_AVAILABILITY =  "asset_value_method_availability"; //$NON-NLS-1$
    public static final  String ASSET_AVAILABILITY =  "asset_value_availability"; //$NON-NLS-1$
    
    // values is taken from process definition in SNCA.xml
    public static final  String PROCESS_METHOD_CONFIDEBTIALITY =  "process_value_method_confidentiality"; //$NON-NLS-1$
    public static final  String PROCESS_CONFIDEBTIALITY =  "process_value_confidentiality"; //$NON-NLS-1$
    public static final  String PROCESS_METHOD_INTEGRITY =  "process_value_method_integrity"; //$NON-NLS-1$
    public static final  String PROCESS_INTEGRITY =  "process_value_integrity"; //$NON-NLS-1$
    public static final  String PROCESS_METHOD_AVAILABILITY =  "process_value_method_availability"; //$NON-NLS-1$
    public static final  String PROCESS_AVAILABILITY =  "process_value_availability"; //$NON-NLS-1$
    
    
    private HitroUIComposite huiComposite;

    /**
     * Disables confidentiality field if inheritance checkbox is enabled
     */
    SelectionListener selectionListenerConfidentiality = new SelectionListener() {
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
            InheritanceBehavior.this.huiComposite.setFieldEnabled(ASSET_CONFIDEBTIALITY,!button.getSelection());
            InheritanceBehavior.this.huiComposite.setFieldEnabled(PROCESS_CONFIDEBTIALITY,!button.getSelection());
        }
    };

    /**
     * Disables integrity field if inheritance checkbox is enabled
     */
    SelectionListener selectionListenerIntegrity = new SelectionListener() {
        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            // empty
        }
        @Override
        public void widgetSelected(SelectionEvent e) {      
            Button button = (Button) e.getSource();
            if (LOG.isDebugEnabled()) {
                LOG.debug("integrity checkbox selected: " + button.getSelection() );
            }
            InheritanceBehavior.this.huiComposite.setFieldEnabled(ASSET_INTEGRITY,!button.getSelection());
            InheritanceBehavior.this.huiComposite.setFieldEnabled(PROCESS_INTEGRITY,!button.getSelection());
        }
    };
    
    /**
     * Disables availability field if inheritance checkbox is enabled
     */
    SelectionListener selectionListenerAvailability = new SelectionListener() {
        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            // empty
        }
        @Override
        public void widgetSelected(SelectionEvent e) {      
            Button button = (Button) e.getSource();
            if (LOG.isDebugEnabled()) {
                LOG.debug("availability checkbox selected: " + button.getSelection() );
            }
            InheritanceBehavior.this.huiComposite.setFieldEnabled(ASSET_AVAILABILITY,!button.getSelection());
            InheritanceBehavior.this.huiComposite.setFieldEnabled(PROCESS_AVAILABILITY,!button.getSelection());
        }
    };
    
    public InheritanceBehavior(HitroUIComposite huiComposite) {
        this.huiComposite = huiComposite;
    }
    
    /**
     * If deduce confidentiality-, integrity- or availability-checkbox is selected 
     * fields to set confidentiality-, integrity- and availability-values are disabled and vice versa.
     * 
     * @see sernet.gs.ui.rcp.main.bsi.editors.IEditorBehavior#init()
     */
    @Override
    public void init() {
        Control field = huiComposite.getField(ASSET_METHOD_CONFIDEBTIALITY);
        if(field!=null && field instanceof Button) {
            huiComposite.setFieldEnabled(ASSET_CONFIDEBTIALITY,!((Button)field).getSelection());
        }
        field = huiComposite.getField(ASSET_METHOD_INTEGRITY);
        if(field!=null && field instanceof Button) {
            huiComposite.setFieldEnabled(ASSET_INTEGRITY,!((Button)field).getSelection());
        }
        field = huiComposite.getField(ASSET_METHOD_AVAILABILITY);
        if(field!=null && field instanceof Button) {
            huiComposite.setFieldEnabled(ASSET_AVAILABILITY,!((Button)field).getSelection());
        }
    }

    /**
     * Adds selection listeners to deduce confidentiality-, integrity- and availability-checkboxes.
     * If checkbox is selected fields to set confidentiality-, integrity- and availability-values
     * are disabled and vice versa.
     * 
     * @see sernet.gs.ui.rcp.main.bsi.editors.IEditorBehavior#addBehavior()
     */
    @Override
    public void addBehavior() {
        // If editor field id does not exits in huiComposite
        // nothing happens. Each listener is added only once 
        // because element is an asset OR a process
        huiComposite.addSelectionListener(ASSET_METHOD_CONFIDEBTIALITY,selectionListenerConfidentiality);
        huiComposite.addSelectionListener(PROCESS_METHOD_CONFIDEBTIALITY,selectionListenerConfidentiality);
        huiComposite.addSelectionListener(ASSET_METHOD_INTEGRITY,selectionListenerIntegrity);
        huiComposite.addSelectionListener(PROCESS_METHOD_INTEGRITY,selectionListenerIntegrity);
        huiComposite.addSelectionListener(ASSET_METHOD_AVAILABILITY,selectionListenerAvailability);
        huiComposite.addSelectionListener(PROCESS_METHOD_AVAILABILITY,selectionListenerAvailability);  
    }

    /**
     * Removes the listeners added in addBehavior()
     * 
     * @see sernet.gs.ui.rcp.main.bsi.editors.IEditorBehavior#removeBehavior()
     */
    @Override
    public void removeBehavior() {
        huiComposite.removeSelectionListener(ASSET_METHOD_CONFIDEBTIALITY,selectionListenerConfidentiality);
        huiComposite.removeSelectionListener(PROCESS_METHOD_CONFIDEBTIALITY,selectionListenerConfidentiality);
        huiComposite.removeSelectionListener(ASSET_METHOD_INTEGRITY,selectionListenerIntegrity);
        huiComposite.removeSelectionListener(PROCESS_METHOD_INTEGRITY,selectionListenerIntegrity);
        huiComposite.removeSelectionListener(ASSET_METHOD_AVAILABILITY,selectionListenerAvailability);
        huiComposite.removeSelectionListener(PROCESS_METHOD_AVAILABILITY,selectionListenerAvailability);
    }

}
