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
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.model.bsi.IReevaluator;
import sernet.verinice.model.common.CascadingTransaction;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.TransactionAbortedException;

/**
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 *
 */
public class AssetValueAdapter implements IReevaluator, Serializable {

    private static final Logger LOG = Logger.getLogger(AssetValueAdapter.class);

    private CnATreeElement cnaTreeElement;

    public AssetValueAdapter(CnATreeElement parent) {
        this.cnaTreeElement = parent;
    }

    @Override
    public int getIntegrity() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("get Integrity for " + cnaTreeElement); //$NON-NLS-1$
        }
        PropertyList properties = cnaTreeElement.getEntity().getProperties(cnaTreeElement.getTypeId() + AssetValueService.INTEGRITY);
        if (properties != null && properties.getProperties() != null && properties.getProperties().size() > 0){
            return properties.getProperty(0).getNumericPropertyValue();
        } else {
            return AssetValueService.VALUE_UNDEF;
        }
    }

    @Override
    public int getAvailability() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("get avail. for " + cnaTreeElement); //$NON-NLS-1$
        }
        PropertyList properties = cnaTreeElement.getEntity().getProperties(cnaTreeElement.getTypeId() + AssetValueService.AVAILABILITY);
        if (properties != null && properties.getProperties() != null && properties.getProperties().size() > 0){
            return properties.getProperty(0).getNumericPropertyValue();
        } else {
            return AssetValueService.VALUE_UNDEF;
        }
    }

    @Override
    public int getConfidentiality() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("get confid. for " + cnaTreeElement); //$NON-NLS-1$
        }
        PropertyList properties = cnaTreeElement.getEntity().getProperties(cnaTreeElement.getTypeId() + AssetValueService.CONFIDENTIALITY);
        if (properties != null && properties.getProperties() != null && properties.getProperties().size() > 0){
            return properties.getProperty(0).getNumericPropertyValue();
        } else {
            return AssetValueService.VALUE_UNDEF;
        }
    }

    @Override
    public void setIntegrity(int i) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("set integrity for " + cnaTreeElement); //$NON-NLS-1$
        }
        EntityType entityType = HUITypeFactory.getInstance()
                .getEntityType(cnaTreeElement.getEntity().getEntityType());
        String id = cnaTreeElement.getTypeId() + AssetValueService.INTEGRITY;
        PropertyType propertyType = entityType.getPropertyType(id);
        if (propertyType != null) {
            cnaTreeElement.getEntity().setSimpleValue(propertyType, Integer.toString(i));
        }
    }

    @Override
    public void setAvailability(int i) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("set avail. for " + cnaTreeElement); //$NON-NLS-1$
        }
        EntityType entityType = HUITypeFactory.getInstance()
                .getEntityType(cnaTreeElement.getEntity().getEntityType());
        PropertyType propertyType = entityType
                .getPropertyType(cnaTreeElement.getTypeId() + AssetValueService.AVAILABILITY);
        if (propertyType != null) {
            cnaTreeElement.getEntity().setSimpleValue(propertyType, Integer.toString(i));
        }
    }

    @Override
    public void setConfidentiality(int i) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("set confd. for " + cnaTreeElement); //$NON-NLS-1$
        }
        EntityType entityType = HUITypeFactory.getInstance()
                .getEntityType(cnaTreeElement.getEntity().getEntityType());
        PropertyType propertyType = entityType
                .getPropertyType(cnaTreeElement.getTypeId() + AssetValueService.CONFIDENTIALITY);
        if (propertyType != null)
            cnaTreeElement.getEntity().setSimpleValue(propertyType, Integer.toString(i));
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
            if (link.getDependency().isProtectionRequirementsProvider()) {
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

    /* (non-Javadoc)
     * @see sernet.verinice.model.bsi.IProtectionRequirementsProvider#updateIntegritaet(sernet.verinice.model.common.CascadingTransaction)
     */
    @Override
    public void updateIntegrity(CascadingTransaction ta) {
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
                bottomNode.getLinkChangeListener().determineIntegrity(ta);

            }

        } catch (TransactionAbortedException tae) {
            LOG.debug("Integritätsänderung abgebrochen."); //$NON-NLS-1$
        } catch (RuntimeException e) {
            LOG.error(Messages.AssetValueAdapter_11, e);
            ta.abort();
            throw e;
        } catch (java.lang.Exception e) {
            LOG.error(Messages.AssetValueAdapter_11, e);
            ta.abort();
            throw new RuntimeException(e);
        }
    }

    /* (non-Javadoc)
     * @see sernet.verinice.model.bsi.IProtectionRequirementsProvider#updateVerfuegbarkeit(sernet.verinice.model.common.CascadingTransaction)
     */
    @Override
    public void updateAvailability(CascadingTransaction ta) {
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
                bottomNode.getLinkChangeListener().determineAvailability(ta);
            }

        } catch (TransactionAbortedException tae) {
            LOG.debug("Verfügbarkeitsänderung abgebrochen."); //$NON-NLS-1$
        } catch (RuntimeException e) {
            LOG.error(Messages.AssetValueAdapter_7, e);
            ta.abort();
            throw e;
        } catch (java.lang.Exception e) {
            LOG.error(Messages.AssetValueAdapter_11, e);
            ta.abort();
            throw new RuntimeException(e);
        }
    }

    /* (non-Javadoc)
     * @see sernet.verinice.model.bsi.IProtectionRequirementsProvider#updateVertraulichkeit(sernet.verinice.model.common.CascadingTransaction)
     */
    @Override
    public void updateConfidentiality(CascadingTransaction ta) {
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
                bottomNode.getLinkChangeListener().determineConfidentiality(ta);
            }
        } catch (TransactionAbortedException tae) {
            LOG.debug("Vertraulichkeitsänderung abgebrochen."); //$NON-NLS-1$
        } catch (RuntimeException e) {
            LOG.error(Messages.AssetValueAdapter_9, e);
            ta.abort();
            throw e;
        } catch (java.lang.Exception e) {
            LOG.error(Messages.AssetValueAdapter_11, e);
            ta.abort();
            throw new RuntimeException(e);
        }
    }

    /*
     * (non-Javadoc)
     * getIntegritaetDescription()
     */
    @Override
    public String getIntegrityDescription() {
        return getDescription();
    }

    /*
     * (non-Javadoc)
     * getVerfuegbarkeitDescription()
     */
    @Override
    public String getAvailabilityDescription() {
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
<<<<<<< HEAD
     *
     * @seesernet.gs.ui.rcp.main.bsi.model.ISchutzbedarfProvider#
||||||| merged common ancestors
     * 
     * @seesernet.gs.ui.rcp.main.bsi.model.ISchutzbedarfProvider#
=======
     * 
     * @seesernet.gs.ui.rcp.main.bsi.model.IProtectionRequirementsProvider#
>>>>>>> Rename Schutzbedarf to ProtectionRequirements
     * getVertraulichkeitDescription()
     */
    @Override
    public String getConfidentialityDescription() {
        return getDescription();
    }

    /*
     * (non-Javadoc)
<<<<<<< HEAD
     *
     * @seesernet.gs.ui.rcp.main.bsi.model.ISchutzbedarfProvider#
||||||| merged common ancestors
     * 
     * @seesernet.gs.ui.rcp.main.bsi.model.ISchutzbedarfProvider#
=======
     * 
     * @seesernet.gs.ui.rcp.main.bsi.model.IProtectionRequirementsProvider#
>>>>>>> Rename Schutzbedarf to ProtectionRequirements
     * setIntegritaetDescription(java.lang.String)
     */
    @Override
    public void setIntegrityDescription(String text) {
        setDescription(text);
    }

    /*
     * (non-Javadoc)
<<<<<<< HEAD
     *
     * @seesernet.gs.ui.rcp.main.bsi.model.ISchutzbedarfProvider#
||||||| merged common ancestors
     * 
     * @seesernet.gs.ui.rcp.main.bsi.model.ISchutzbedarfProvider#
=======
     * 
     * @seesernet.gs.ui.rcp.main.bsi.model.IProtectionRequirementsProvider#
>>>>>>> Rename Schutzbedarf to ProtectionRequirements
     * setVerfuegbarkeitDescription(java.lang.String)
     */
    @Override
    public void setAvailabilityDescription(String text) {
        setDescription(text);
    }

    /*
     * (non-Javadoc)
<<<<<<< HEAD
     *
     * @seesernet.gs.ui.rcp.main.bsi.model.ISchutzbedarfProvider#
||||||| merged common ancestors
     * 
     * @seesernet.gs.ui.rcp.main.bsi.model.ISchutzbedarfProvider#
=======
     * 
     * @seesernet.gs.ui.rcp.main.bsi.model.IProtectionRequirementsProvider#
>>>>>>> Rename Schutzbedarf to ProtectionRequirements
     * setVertraulichkeitDescription(java.lang.String)
     */
    @Override
    public void setConfidentialityDescription(String text) {
        setDescription(text);
    }

    /*
     * (non-Javadoc)
<<<<<<< HEAD
     *
     * @seesernet.gs.ui.rcp.main.bsi.model.ISchutzbedarfProvider#
||||||| merged common ancestors
     * 
     * @seesernet.gs.ui.rcp.main.bsi.model.ISchutzbedarfProvider#
=======
     * 
     * @seesernet.gs.ui.rcp.main.bsi.model.IProtectionRequirementsProvider#
>>>>>>> Rename Schutzbedarf to ProtectionRequirements
     * isCalculatedAvailability()
     */
    @Override
    public boolean isCalculatedAvailability() {
        PropertyList properties = cnaTreeElement.getEntity().getProperties(cnaTreeElement.getTypeId() + AssetValueService.METHOD_AVAILABILITY);
        if (properties != null && properties.getProperties() != null && properties.getProperties().size() > 0){
            return properties.getProperty(0).getNumericPropertyValue() == AssetValueService.METHOD_AUTO;
        } else {
            return false;
        }
    }

    /*
     * (non-Javadoc)
<<<<<<< HEAD
     *
     * @seesernet.gs.ui.rcp.main.bsi.model.ISchutzbedarfProvider#
||||||| merged common ancestors
     * 
     * @seesernet.gs.ui.rcp.main.bsi.model.ISchutzbedarfProvider#
=======
     * 
     * @seesernet.gs.ui.rcp.main.bsi.model.IProtectionRequirementsProvider#
>>>>>>> Rename Schutzbedarf to ProtectionRequirements
     * isCalculatedConfidentiality()
     */
    @Override
    public boolean isCalculatedConfidentiality() {
        PropertyList properties = cnaTreeElement.getEntity().getProperties(cnaTreeElement.getTypeId() + AssetValueService.METHOD_CONFIDENTIALITY);
        if (properties != null && properties.getProperties() != null && properties.getProperties().size() > 0){
            return properties.getProperty(0).getNumericPropertyValue() == AssetValueService.METHOD_AUTO;
        } else {
            return false;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * sernet.gs.ui.rcp.main.bsi.model.IProtectionRequirementsProvider#isCalculatedIntegrity
     * ()
     */
    @Override
    public boolean isCalculatedIntegrity() {
        PropertyList properties = cnaTreeElement.getEntity().getProperties(cnaTreeElement.getTypeId() + AssetValueService.METHOD_INTEGRITY);
        if (properties != null && properties.getProperties() != null && properties.getProperties().size() > 0){
            return properties.getProperty(0).getNumericPropertyValue() == AssetValueService.METHOD_AUTO;
        } else {
            return false;
        }
    }


    /*
     * (non-Javadoc)
     *
     * @see
     * sernet.verinice.model.bsi.IProtectionRequirementsProvider#updateValue(
     * sernet.verinice.model.common.CascadingTransaction)
     */
    @Override
    public void updateValue(CascadingTransaction ta) {
      //override to introduce new behavior
    }

    @Override
    public void setValue(CascadingTransaction ta, String properyName, Object value) {
        //override to introduce new behavior
    }

}
