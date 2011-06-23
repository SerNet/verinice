/*******************************************************************************
 * Copyright (c) 2010 Alexander Koderman <ak@sernet.de>.
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
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.iso27k;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyList;
import sernet.verinice.model.bsi.ISchutzbedarfProvider;
import sernet.verinice.model.common.CascadingTransaction;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.TransactionAbortedException;

/**
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
public class AssetValueAdapter implements ISchutzbedarfProvider, Serializable {

    private static final Logger LOG = Logger.getLogger(AssetValueAdapter.class);
    
    private static final InheritLogger LOG_INHERIT = InheritLogger.getLogger(AssetValueAdapter.class);
    
    private CnATreeElement cnaTreeElement;

    public AssetValueAdapter(CnATreeElement parent) {
        this.cnaTreeElement = parent;
    }

    public int getIntegritaet() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("get Integrity for " + cnaTreeElement); //$NON-NLS-1$
        }
        PropertyList properties = cnaTreeElement.getEntity().getProperties(cnaTreeElement.getTypeId() + AssetValueService.INTEGRITY);
        if (properties != null && properties.getProperties() != null && properties.getProperties().size() > 0)
            return properties.getProperty(0).getNumericPropertyValue();
        else
            return AssetValueService.VALUE_UNDEF;
    }

    public int getVerfuegbarkeit() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("get avail. for " + cnaTreeElement); //$NON-NLS-1$
        }
        PropertyList properties = cnaTreeElement.getEntity().getProperties(cnaTreeElement.getTypeId() + AssetValueService.AVAILABILITY);
        if (properties != null && properties.getProperties() != null && properties.getProperties().size() > 0)
            return properties.getProperty(0).getNumericPropertyValue();
        else
            return AssetValueService.VALUE_UNDEF;
    }

    public int getVertraulichkeit() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("get confid. for " + cnaTreeElement); //$NON-NLS-1$
        }
        PropertyList properties = cnaTreeElement.getEntity().getProperties(cnaTreeElement.getTypeId() + AssetValueService.CONFIDENTIALITY);
        if (properties != null && properties.getProperties() != null && properties.getProperties().size() > 0)
            return properties.getProperty(0).getNumericPropertyValue();
        else
            return AssetValueService.VALUE_UNDEF;
    }

    public void setIntegritaet(int i) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("set integrity for " + cnaTreeElement); //$NON-NLS-1$
        }
        EntityType entityType = HUITypeFactory.getInstance().getEntityType(cnaTreeElement.getEntity().getEntityType());
        cnaTreeElement.getEntity().setSimpleValue(entityType.getPropertyType(cnaTreeElement.getTypeId() + AssetValueService.INTEGRITY), Integer.toString(i));
    }

    public void setVerfuegbarkeit(int i) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("set avail. for " + cnaTreeElement); //$NON-NLS-1$
        }
        EntityType entityType = HUITypeFactory.getInstance().getEntityType(cnaTreeElement.getEntity().getEntityType());
        cnaTreeElement.getEntity().setSimpleValue(entityType.getPropertyType(cnaTreeElement.getTypeId() + AssetValueService.AVAILABILITY), Integer.toString(i));
    }

    public void setVertraulichkeit(int i) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("set confd. for " + cnaTreeElement); //$NON-NLS-1$
        }
        EntityType entityType = HUITypeFactory.getInstance().getEntityType(cnaTreeElement.getEntity().getEntityType());
        cnaTreeElement.getEntity().setSimpleValue(entityType.getPropertyType(cnaTreeElement.getTypeId() + AssetValueService.CONFIDENTIALITY), Integer.toString(i));
    }

    /**
     * @param downwardElement
     * @param downwardsTA
     * @param bottomNodes
     * @return
     */
    private void findBottomNodes(CnATreeElement downwardElement, Set<CnATreeElement> bottomNodes, CascadingTransaction downwardsTA) {
        if (downwardsTA.hasBeenVisited(downwardElement)) {
            return;
        }

        try {
            downwardsTA.enter(downwardElement);
        } catch (TransactionAbortedException e) {
            LOG.error("Aborted while determining bottom node for protection requirements on object: " + downwardElement.getTitle(), e); //$NON-NLS-1$
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
        if (countLinks == 0)
            bottomNodes.add(downwardElement);
    }

    public CnATreeElement getParent() {
        return cnaTreeElement;
    }

    public void setParent(CnATreeElement parent) {
        this.cnaTreeElement = parent;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.model.bsi.ISchutzbedarfProvider#updateIntegritaet(sernet.verinice.model.common.CascadingTransaction)
     */
    public void updateIntegritaet(CascadingTransaction ta) {
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
            LOG.debug("Integritätsänderung abgebrochen."); //$NON-NLS-1$
        } catch (RuntimeException e) {
            LOG.error(Messages.AssetValueAdapter_11, e);
            ta.abort();
        }
    }

    /* (non-Javadoc)
     * @see sernet.verinice.model.bsi.ISchutzbedarfProvider#updateVerfuegbarkeit(sernet.verinice.model.common.CascadingTransaction)
     */
    public void updateVerfuegbarkeit(CascadingTransaction ta) {
        try {
            // 1st step: traverse down:
            // find bottom nodes from which to start:
            CascadingTransaction downwardsTA = new CascadingTransaction();
            Set<CnATreeElement> bottomNodes = new HashSet<CnATreeElement>();
            findBottomNodes(cnaTreeElement, bottomNodes, downwardsTA);

            // 2nd step: traverse up:
            for (CnATreeElement bottomNode : bottomNodes) {
                // determine protection level from parents (or keep own
                // depending on settings):
                bottomNode.getLinkChangeListener().determineVerfuegbarkeit(ta);
            }

        } catch (TransactionAbortedException tae) {
            LOG.debug("Verfügbarkeitsänderung abgebrochen."); //$NON-NLS-1$
        } catch (RuntimeException e) {
            LOG.error(Messages.AssetValueAdapter_7, e);
            ta.abort();
        }
    }

    /* (non-Javadoc)
     * @see sernet.verinice.model.bsi.ISchutzbedarfProvider#updateVertraulichkeit(sernet.verinice.model.common.CascadingTransaction)
     */
    public void updateVertraulichkeit(CascadingTransaction ta) {
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
            LOG.debug("Vertraulichkeitsänderung abgebrochen."); //$NON-NLS-1$
        } catch (RuntimeException e) {
            LOG.error(Messages.AssetValueAdapter_9, e);
            ta.abort();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @seesernet.gs.ui.rcp.main.bsi.model.ISchutzbedarfProvider#
     * getIntegritaetDescription()
     */
    public String getIntegritaetDescription() {
        return getDescription();
    }

    /*
     * (non-Javadoc)
     * 
     * @seesernet.gs.ui.rcp.main.bsi.model.ISchutzbedarfProvider#
     * getVerfuegbarkeitDescription()
     */
    public String getVerfuegbarkeitDescription() {
        return getDescription();
    }

    /**
     * @return
     */
    private String getDescription() {
        return cnaTreeElement.getEntity().getSimpleValue(cnaTreeElement.getTypeId() + AssetValueService.EXPLANATION);
    }

    private void setDescription(String text) {
        EntityType entityType = HUITypeFactory.getInstance().getEntityType(cnaTreeElement.getEntity().getEntityType());
        cnaTreeElement.getEntity().setSimpleValue(entityType.getPropertyType(cnaTreeElement.getTypeId() + AssetValueService.EXPLANATION), text);
    }

    /*
     * (non-Javadoc)
     * 
     * @seesernet.gs.ui.rcp.main.bsi.model.ISchutzbedarfProvider#
     * getVertraulichkeitDescription()
     */
    public String getVertraulichkeitDescription() {
        return getDescription();
    }

    /*
     * (non-Javadoc)
     * 
     * @seesernet.gs.ui.rcp.main.bsi.model.ISchutzbedarfProvider#
     * setIntegritaetDescription(java.lang.String)
     */
    public void setIntegritaetDescription(String text) {
        setDescription(text);
    }

    /*
     * (non-Javadoc)
     * 
     * @seesernet.gs.ui.rcp.main.bsi.model.ISchutzbedarfProvider#
     * setVerfuegbarkeitDescription(java.lang.String)
     */
    public void setVerfuegbarkeitDescription(String text) {
        setDescription(text);
    }

    /*
     * (non-Javadoc)
     * 
     * @seesernet.gs.ui.rcp.main.bsi.model.ISchutzbedarfProvider#
     * setVertraulichkeitDescription(java.lang.String)
     */
    public void setVertraulichkeitDescription(String text) {
        setDescription(text);
    }

    /*
     * (non-Javadoc)
     * 
     * @seesernet.gs.ui.rcp.main.bsi.model.ISchutzbedarfProvider#
     * isCalculatedAvailability()
     */
    public boolean isCalculatedAvailability() {
        PropertyList properties = cnaTreeElement.getEntity().getProperties(cnaTreeElement.getTypeId() + AssetValueService.METHOD_AVAILABILITY);
        if (properties != null && properties.getProperties() != null && properties.getProperties().size() > 0)
            return properties.getProperty(0).getNumericPropertyValue() == AssetValueService.METHOD_AUTO;
        else
            return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @seesernet.gs.ui.rcp.main.bsi.model.ISchutzbedarfProvider#
     * isCalculatedConfidentiality()
     */
    public boolean isCalculatedConfidentiality() {
        PropertyList properties = cnaTreeElement.getEntity().getProperties(cnaTreeElement.getTypeId() + AssetValueService.METHOD_CONFIDENTIALITY);
        if (properties != null && properties.getProperties() != null && properties.getProperties().size() > 0)
            return properties.getProperty(0).getNumericPropertyValue() == AssetValueService.METHOD_AUTO;
        else
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
        PropertyList properties = cnaTreeElement.getEntity().getProperties(cnaTreeElement.getTypeId() + AssetValueService.METHOD_INTEGRITY);
        if (properties != null && properties.getProperties() != null && properties.getProperties().size() > 0)
            return properties.getProperty(0).getNumericPropertyValue() == AssetValueService.METHOD_AUTO;
        else
            return false;
    }

}
