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
public class LikelihoodBehaviour implements IEditorBehavior {

    private static final Logger LOG = Logger.getLogger(InheritanceBehavior.class);
    
    public static final String PROP_SCENARIO_METHOD = "incscen_likelihoodmethod";
    public static final String PROP_SCENARIO_THREAT_PROBABILITY = "incscen_threat_likelihood";
    public static final String PROP_SCENARIO_VULN_PROBABILITY = "incscen_vuln_level";
    
    private HitroUIComposite huiComposite;
    
    SelectionListener likelihoodListener = new SelectionListener() {
        
        @Override
        public void widgetSelected(SelectionEvent e) {
            Button button = (Button)e.getSource();
            if (LOG.isDebugEnabled()) {
                LOG.debug("likelihood checkbox selected: " + button.getSelection() );
            }
            LikelihoodBehaviour.this.huiComposite.setFieldEnabled(PROP_SCENARIO_THREAT_PROBABILITY, !button.getSelection());
            LikelihoodBehaviour.this.huiComposite.setFieldEnabled(PROP_SCENARIO_VULN_PROBABILITY, !button.getSelection());
        }
        
        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
                //  empty
        }
    };
    
    public LikelihoodBehaviour(HitroUIComposite composite){
        this.huiComposite = composite;
    }

    
    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.bsi.editors.IEditorBehavior#init()
     */
    @Override
    public void init() {
        Control field = huiComposite.getField(PROP_SCENARIO_METHOD);
        if(field!=null && field instanceof Button) {
            huiComposite.setFieldEnabled(PROP_SCENARIO_THREAT_PROBABILITY,!((Button)field).getSelection());
            huiComposite.setFieldEnabled(PROP_SCENARIO_VULN_PROBABILITY,!((Button)field).getSelection());
        }
    }

    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.bsi.editors.IEditorBehavior#addBehavior()
     */
    @Override
    public void addBehavior() {
        huiComposite.addSelectionListener(PROP_SCENARIO_METHOD, likelihoodListener);
    }

    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.bsi.editors.IEditorBehavior#removeBehavior()
     */
    @Override
    public void removeBehavior() {
        huiComposite.removeSelectionListener(PROP_SCENARIO_METHOD, likelihoodListener);
    }

}
