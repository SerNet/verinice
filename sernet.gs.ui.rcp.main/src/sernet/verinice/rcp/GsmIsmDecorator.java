/*******************************************************************************
 * Copyright (c) 2015 Moritz Reiter.
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
 *     Moritz Reiter <mr[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp;

import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.model.iso27k.Vulnerability;

/**
 * Creates an overlay Decorator (blue, yellow or red dot) for Vulnerabilities and Scenarios
 * based on values coming from the Greenbone OMM API (see
 * http://greenbone.net/technology/omp.html#type_threat).
 * 
 * @author Moritz Reiter <mr[at]sernet[dot]de>
 */
public class GsmIsmDecorator extends LabelProvider implements ILightweightLabelDecorator {
    private static final String GSM_ISM_LEVEL_LOW = "low";
    private static final String GSM_ISM_LEVEL_MEDIUM = "medium";
    private static final String GSM_ISM_LEVEL_HIGH = "high";
    
    private static final String IMAGE_PATH_BLUE = "overlays/dot_blue.png";
    private static final String IMAGE_PATH_YELLOW = "overlays/dot_yellow.png";
    private static final String IMAGE_PATH_RED = "overlays/dot_red.png";
    private static final String IMAGE_PATH_EMPTY = "overlays/empty.png";
    
    private CnATreeElement treeElement;
    private String gsmIsmLevel;
    private String imagePath;
    
    @Override
    public void decorate(Object element, IDecoration decoration) {
        Activator.inheritVeriniceContextState();
        treeElement = (CnATreeElement) element;
        if (prefEnabled() && isApplicable()) {
            setGsmIsmLevel();
            setImagePath();
            decoration.addOverlay(ImageCache.getInstance().getImageDescriptor(imagePath));
        }
    }    

    private boolean prefEnabled() {
        return Activator.getDefault().getPreferenceStore()
                .getBoolean(PreferenceConstants.SHOW_GSMISM_DECORATOR);
    }
    
    private boolean isApplicable() {
        if (treeElement instanceof IncidentScenario || treeElement instanceof Vulnerability) {
            return true;
        }
        return false;
    }
    
    private void setGsmIsmLevel() {
        if (treeElement instanceof IncidentScenario) {
            gsmIsmLevel = ((IncidentScenario) treeElement).getEntity()
                    .getSimpleValue("gsm_ism_scenario_level").toLowerCase();
        } else if (treeElement instanceof Vulnerability) {
            gsmIsmLevel = ((Vulnerability) treeElement).getEntity()
                    .getSimpleValue("gsm_ism_vulnerability_level").toLowerCase();
        }
    }
    
    private void setImagePath() {
        if (gsmIsmLevel.equals(GSM_ISM_LEVEL_LOW)) {
            imagePath = IMAGE_PATH_BLUE;
        } else if (gsmIsmLevel.equals(GSM_ISM_LEVEL_MEDIUM)) {
            imagePath = IMAGE_PATH_YELLOW;
        } else if (gsmIsmLevel.equals(GSM_ISM_LEVEL_HIGH)) {
            imagePath = IMAGE_PATH_RED;
        } else {
            imagePath = IMAGE_PATH_EMPTY;
        }
    }
}
