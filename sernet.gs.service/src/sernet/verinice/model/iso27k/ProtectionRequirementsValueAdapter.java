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
import sernet.verinice.interfaces.AbstractReevaluator;
import sernet.verinice.model.common.CascadingTransaction;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.TransactionAbortedException;

/**
 * Let the contained {@link CnATreeElement} reeavluate the values for the protection requirements.
 * By convention property identifiers for the protection requirements are build by the following rules:
 * <pre>
 * CnATreeElement.getTypeId()+ '_value_confidentiality'
 * CnATreeElement.getTypeId()+ '_value_integrity'
 * CnATreeElement.getTypeId()+ '_value_availability'
 * </pre>
 * for the value and
 * <pre>
 * CnATreeElement.getTypeId()+ '_value_method_confidentiality'
 * CnATreeElement.getTypeId()+ '_value_method_integrity'
 * CnATreeElement.getTypeId()+ '_value_method_availability'
 * </pre>
 * for the flag, indication the deduction.<br/>
 *
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 *
 */
@SuppressWarnings("serial")
public class ProtectionRequirementsValueAdapter  extends AbstractReevaluator implements Serializable {

    private static final Logger LOG = Logger.getLogger(ProtectionRequirementsValueAdapter.class);

    private CnATreeElement cnaTreeElement;

    public ProtectionRequirementsValueAdapter(CnATreeElement parent) {
        this.cnaTreeElement = parent;
    }

    @Override
    public int getIntegrity() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("get Integrity for " + cnaTreeElement); //$NON-NLS-1$
        }
        PropertyList properties = cnaTreeElement.getEntity().getProperties(cnaTreeElement.getTypeId() + AssetValueService.INTEGRITY);
        if (properties != null && properties.getProperties() != null
                && !properties.getProperties().isEmpty()) {
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
        if (properties != null && properties.getProperties() != null
                && !properties.getProperties().isEmpty()) {
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
        if (properties != null && properties.getProperties() != null
                && !properties.getProperties().isEmpty()) {
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
        if (propertyType != null) {
            cnaTreeElement.getEntity().setSimpleValue(propertyType, Integer.toString(i));
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
            Set<CnATreeElement> bottomNodes = new HashSet<>();
            findBottomNodes(cnaTreeElement, bottomNodes, downwardsTA);

            // 2nd step: traverse up:
            for (CnATreeElement bottomNode : bottomNodes) {
                // determine protection level from parents (or keep own
                // depending on description):
                bottomNode.getLinkChangeListener().determineIntegrity(ta);

            }

        } catch (TransactionAbortedException tae) {
            LOG.debug("Reevaluation of integrity aborted."); //$NON-NLS-1$
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
            Set<CnATreeElement> bottomNodes = new HashSet<>();
            findBottomNodes(cnaTreeElement, bottomNodes, downwardsTA);

            // 2nd step: traverse up:
            for (CnATreeElement bottomNode : bottomNodes) {
                // determine protection level from parents (or keep own
                // depending on settings):
                bottomNode.getLinkChangeListener().determineAvailability(ta);
            }

        } catch (TransactionAbortedException tae) {
            LOG.debug("Reevaluation of availability aborted."); //$NON-NLS-1$
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
            Set<CnATreeElement> bottomNodes = new HashSet<>();
            findBottomNodes(cnaTreeElement, bottomNodes, downwardsTA);

            // 2nd step: traverse up:
            for (CnATreeElement bottomNode : bottomNodes) {
                // determine protection level from parents (or keep own
                // depending on description):
                bottomNode.getLinkChangeListener().determineConfidentiality(ta);
            }
        } catch (TransactionAbortedException tae) {
            LOG.debug("Reevaluation of confidentiality aborted."); //$NON-NLS-1$
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
     *
     * @seesernet.gs.ui.rcp.main.bsi.model.IProtectionRequirementsProvider#
     * getIntegritaetDescription()
     */
    @Override
    public String getIntegrityDescription() {
        return getDescription();
    }

    /*
     * (non-Javadoc)
     *
     * @seesernet.gs.ui.rcp.main.bsi.model.IProtectionRequirementsProvider#
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
     *
     * @seesernet.gs.ui.rcp.main.bsi.model.IProtectionRequirementsProvider#
     * getVertraulichkeitDescription()
     */
    @Override
    public String getConfidentialityDescription() {
        return getDescription();
    }

    /*
     * (non-Javadoc)
     *
     * @seesernet.gs.ui.rcp.main.bsi.model.IProtectionRequirementsProvider#
     * setIntegritaetDescription(java.lang.String)
     */
    @Override
    public void setIntegrityDescription(String text) {
        setDescription(text);
    }

    /*
     * (non-Javadoc)
     *
     * @seesernet.gs.ui.rcp.main.bsi.model.IProtectionRequirementsProvider#
     * setVerfuegbarkeitDescription(java.lang.String)
     */
    @Override
    public void setAvailabilityDescription(String text) {
        setDescription(text);
    }

    /*
     * (non-Javadoc)
     *
     * @seesernet.gs.ui.rcp.main.bsi.model.IProtectionRequirementsProvider#
     * setVertraulichkeitDescription(java.lang.String)
     */
    @Override
    public void setConfidentialityDescription(String text) {
        setDescription(text);
    }

    /*
     * (non-Javadoc)
     *
     * @seesernet.gs.ui.rcp.main.bsi.model.IProtectionRequirementsProvider#
     * isCalculatedAvailability()
     */
    @Override
    public boolean isCalculatedAvailability() {
        PropertyList properties = cnaTreeElement.getEntity().getProperties(cnaTreeElement.getTypeId() + AssetValueService.METHOD_AVAILABILITY);
        if (properties != null && properties.getProperties() != null
                && !properties.getProperties().isEmpty()) {
            return properties.getProperty(0).getNumericPropertyValue() == AssetValueService.METHOD_AUTO;
        } else {
            return false;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @seesernet.gs.ui.rcp.main.bsi.model.IProtectionRequirementsProvider#
     * isCalculatedConfidentiality()
     */
    @Override
    public boolean isCalculatedConfidentiality() {
        PropertyList properties = cnaTreeElement.getEntity().getProperties(cnaTreeElement.getTypeId() + AssetValueService.METHOD_CONFIDENTIALITY);
        if (properties != null && properties.getProperties() != null
                && !properties.getProperties().isEmpty()) {
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
        if (properties != null && properties.getProperties() != null
                && !properties.getProperties().isEmpty()) {
            return properties.getProperty(0).getNumericPropertyValue() == AssetValueService.METHOD_AUTO;
        } else {
            return false;
        }
    }
}
