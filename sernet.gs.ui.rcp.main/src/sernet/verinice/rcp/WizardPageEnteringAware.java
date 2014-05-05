/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
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
package sernet.verinice.rcp;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;

/**
 * Extends the JFace wizard with ability with methods
 * pageEntered() and pageLeft().
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public abstract class WizardPageEnteringAware extends WizardPage {

    private static final Logger LOG = Logger.getLogger(WizardPageEnteringAware.class);
    
    /**
     * @param pageName
     * @param title
     * @param titleImage
     */
    public WizardPageEnteringAware(String pageName, String title, ImageDescriptor titleImage) {
        super(pageName, title, titleImage);
    }

    /**
     * @param pageName
     */
    public WizardPageEnteringAware(String pageName) {
        super(pageName);
    }

    @Override
    public void setVisible(boolean visible) {
        if(visible) {
            pageEntered();
        } else {
            pageLeft();
        }
        super.setVisible(visible);
    }

    /**
     * Called when this wizuard page is entered.
     * Override this in your subclass.
     */
    protected void pageEntered() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Page entered...");
        }
    }
    
    /**
     * Called when this wizard page is left.
     * Override this in your subclass.
     */
    protected void pageLeft() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Page left...");
        }
    }

}
