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
package sernet.verinice.model.bsi;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyList;
import sernet.verinice.model.common.CascadingTransaction;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.TransactionAbortedException;

/**
 * Adapter for elements that provide or receive protection levels.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
@SuppressWarnings("serial")
public class SchutzbedarfAdapter implements ISchutzbedarfProvider, Serializable {

    
    private transient Logger log = Logger.getLogger(SchutzbedarfAdapter.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(SchutzbedarfAdapter.class);
        }
        return log;
    }
    
    private CnATreeElement cnaTreeElement;

    public SchutzbedarfAdapter(CnATreeElement parent) {
        this.cnaTreeElement = parent;
    }

    public int getIntegritaet() {
        PropertyList properties = cnaTreeElement.getEntity().getProperties(cnaTreeElement.getTypeId() + Schutzbedarf.INTEGRITAET);
        if (hasValue(properties)){
            return Schutzbedarf.toInt(properties.getProperty(0).getPropertyValue());
        } else {
            return Schutzbedarf.UNDEF;
        }
    }
    
    public int getVerfuegbarkeit() {
        PropertyList properties = cnaTreeElement.getEntity().getProperties(cnaTreeElement.getTypeId() + Schutzbedarf.VERFUEGBARKEIT);
        if (hasValue(properties)){
            return Schutzbedarf.toInt(properties.getProperty(0).getPropertyValue());
        } else {
            return Schutzbedarf.UNDEF;
        }
    }

    public int getVertraulichkeit() {
        PropertyList properties = cnaTreeElement.getEntity().getProperties(cnaTreeElement.getTypeId() + Schutzbedarf.VERTRAULICHKEIT);
        if (hasValue(properties)){
            return Schutzbedarf.toInt(properties.getProperty(0).getPropertyValue());
        } else {
            return Schutzbedarf.UNDEF;
        }
    }
    
    public boolean hasValue(PropertyList properties) {      
        return properties != null && 
               !properties.getProperties().isEmpty() &&
               properties.getProperty(0).getPropertyValue() !=null;
    }

    public void setIntegritaet(int i) {
        EntityType entityType = HUITypeFactory.getInstance().getEntityType(cnaTreeElement.getEntity().getEntityType());
        String option = Schutzbedarf.toOption(cnaTreeElement.getTypeId(), Schutzbedarf.INTEGRITAET, i);

        cnaTreeElement.getEntity().setSimpleValue(entityType.getPropertyType(cnaTreeElement.getTypeId() + Schutzbedarf.INTEGRITAET), option);
    }

    public void setVerfuegbarkeit(int i) {
        EntityType entityType = HUITypeFactory.getInstance().getEntityType(cnaTreeElement.getEntity().getEntityType());
        String option = Schutzbedarf.toOption(cnaTreeElement.getTypeId(), Schutzbedarf.VERFUEGBARKEIT, i);
        cnaTreeElement.getEntity().setSimpleValue(entityType.getPropertyType(cnaTreeElement.getTypeId() + Schutzbedarf.VERFUEGBARKEIT), option);
    }

    public void setVertraulichkeit(int i) {
        EntityType entityType = HUITypeFactory.getInstance().getEntityType(cnaTreeElement.getEntity().getEntityType());
        String option = Schutzbedarf.toOption(cnaTreeElement.getTypeId(), Schutzbedarf.VERTRAULICHKEIT, i);
        cnaTreeElement.getEntity().setSimpleValue(entityType.getPropertyType(cnaTreeElement.getTypeId() + Schutzbedarf.VERTRAULICHKEIT), option);
    }

    public String getIntegritaetDescription() {
        return cnaTreeElement.getEntity().getSimpleValue(cnaTreeElement.getTypeId() + Schutzbedarf.INTEGRITAET_BEGRUENDUNG);
    }

    public String getVerfuegbarkeitDescription() {
        return cnaTreeElement.getEntity().getSimpleValue(cnaTreeElement.getTypeId() + Schutzbedarf.VERFUEGBARKEIT_BEGRUENDUNG);
    }

    public String getVertraulichkeitDescription() {
        return cnaTreeElement.getEntity().getSimpleValue(cnaTreeElement.getTypeId() + Schutzbedarf.VERTRAULICHKEIT_BEGRUENDUNG);
    }

    public void setIntegritaetDescription(String text) {
        EntityType entityType = HUITypeFactory.getInstance().getEntityType(cnaTreeElement.getEntity().getEntityType());
        cnaTreeElement.getEntity().setSimpleValue(entityType.getPropertyType(cnaTreeElement.getTypeId() + Schutzbedarf.INTEGRITAET_BEGRUENDUNG), text);
    }

    public void setVerfuegbarkeitDescription(String text) {
        EntityType entityType = HUITypeFactory.getInstance().getEntityType(cnaTreeElement.getEntity().getEntityType());
        cnaTreeElement.getEntity().setSimpleValue(entityType.getPropertyType(cnaTreeElement.getTypeId() + Schutzbedarf.VERFUEGBARKEIT_BEGRUENDUNG), text);
    }

    public void setVertraulichkeitDescription(String text) {
        EntityType entityType = HUITypeFactory.getInstance().getEntityType(cnaTreeElement.getEntity().getEntityType());
        cnaTreeElement.getEntity().setSimpleValue(entityType.getPropertyType(cnaTreeElement.getTypeId() + Schutzbedarf.VERTRAULICHKEIT_BEGRUENDUNG), text);
    }

    private void fireVerfuegbarkeitChanged(CascadingTransaction ta) {

        try {
            // 1st step: traverse down:
            // find bottom nodes from which to start:
            CascadingTransaction downwardsTA = new CascadingTransaction();
            Set<CnATreeElement> bottomNodes = new HashSet<CnATreeElement>();
            findBottomNodes(cnaTreeElement, bottomNodes, downwardsTA);

            // 2nd step: traverse up:
            for (CnATreeElement bottomNode : bottomNodes) {
                // determine protection level from parents (or keep own
                // depending on description):
                bottomNode.getLinkChangeListener().determineVerfuegbarkeit(ta);

            }

        } catch (TransactionAbortedException tae) {
            getLog().debug("Verfügbarkeitsänderung abgebrochen."); //$NON-NLS-1$
            throw new RuntimeException(tae);
        } catch (RuntimeException e) {
            ta.abort();
            throw e;
        } catch (Exception e) {
            ta.abort();
            throw new RuntimeException(e);
        }
    }

    private void fireVertraulichkeitChanged(CascadingTransaction ta) {

        try {
            // 1st step: traverse down:
            // find bottom nodes from which to start:
            CascadingTransaction downwardsTA = new CascadingTransaction();
            Set<CnATreeElement> bottomNodes = new HashSet<CnATreeElement>();
            findBottomNodes(cnaTreeElement, bottomNodes, downwardsTA);

            // 2nd step: traverse up:
            for (CnATreeElement bottomNode : bottomNodes) {
                // determine protection level from parents (or keep own
                // depending on description):
                bottomNode.getLinkChangeListener().determineVertraulichkeit(ta);

            }

        } catch (TransactionAbortedException tae) {
            getLog().debug("Vertraulichkeitsänderung abgebrochen."); //$NON-NLS-1$
            throw new RuntimeException(tae);
        } catch (RuntimeException e) {
            ta.abort();
            throw e;
        } catch (Exception e) {
            ta.abort();
            throw new RuntimeException(e);
        }
    }

    private void fireIntegritaetChanged(CascadingTransaction ta) {

        try {
            // 1st step: traverse down:
            // find bottom nodes from which to start:
            CascadingTransaction downwardsTA = new CascadingTransaction();
            Set<CnATreeElement> bottomNodes = new HashSet<CnATreeElement>();
            findBottomNodes(cnaTreeElement, bottomNodes, downwardsTA);

            // 2nd step: traverse up:
            for (CnATreeElement bottomNode : bottomNodes) {
                // determine protection level from parents (or keep own
                // depending on description):
                bottomNode.getLinkChangeListener().determineIntegritaet(ta);

            }

        } catch (TransactionAbortedException tae) {
            getLog().debug("Integritätsänderung abgebrochen."); //$NON-NLS-1$
            throw new RuntimeException(tae);
        } catch (RuntimeException e) {
            ta.abort();
            throw e;
        } catch (Exception e) {
            ta.abort();
            throw new RuntimeException(e);
        }
    }

    /**
     * @param downwardElement
     * @param downwardsTA
     * @param bottomNodes
     * @return
     */
    private void findBottomNodes(CnATreeElement downwardElement, Set<CnATreeElement> bottomNodes, CascadingTransaction downwardsTA) {
        if (downwardsTA.hasBeenVisited(downwardElement)){
            return;
        }

        try {
            downwardsTA.enter(downwardElement);
        } catch (TransactionAbortedException e) {
            Logger.getLogger(this.getClass()).error(Messages.SchutzbedarfAdapter_3 + downwardElement.getTitle(), e);
            return;
        }

        int countLinks = 0;
        for (CnALink link : downwardElement.getLinksDown()) {
            if (link.getDependency().isSchutzbedarfProvider()) {
                countLinks++;
                findBottomNodes(link.getDependency(), bottomNodes, downwardsTA);
            }
        }

        // could not go further down, so add this node:
        if (countLinks == 0){
            bottomNodes.add(downwardElement);
        }
    }

    public CnATreeElement getParent() {
        return cnaTreeElement;
    }

    public void setParent(CnATreeElement parent) {
        this.cnaTreeElement = parent;
    }

    public void updateIntegritaet(CascadingTransaction ta) {
        fireIntegritaetChanged(ta);
    }

    public void updateVerfuegbarkeit(CascadingTransaction ta) {
        fireVerfuegbarkeitChanged(ta);
    }

    public void updateVertraulichkeit(CascadingTransaction ta) {
        fireVertraulichkeitChanged(ta);
    }

    /*
     * (non-Javadoc)
     * 
     * @seesernet.gs.ui.rcp.main.bsi.model.ISchutzbedarfProvider#
     * isCalculatedAvailability()
     */
    public boolean isCalculatedAvailability() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @seesernet.gs.ui.rcp.main.bsi.model.ISchutzbedarfProvider#
     * isCalculatedConfidentiality()
     */
    public boolean isCalculatedConfidentiality() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.gs.ui.rcp.main.bsi.model.ISchutzbedarfProvider#isCalculatedIntegrity
     * ()
     */
    public boolean isCalculatedIntegrity() {
        return false;
    }

}
