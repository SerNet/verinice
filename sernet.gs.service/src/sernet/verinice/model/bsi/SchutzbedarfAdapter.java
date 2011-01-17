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
public class SchutzbedarfAdapter implements ISchutzbedarfProvider, Serializable {

	private CnATreeElement cnaTreeElement;

	public SchutzbedarfAdapter(CnATreeElement parent) {
		this.cnaTreeElement = parent;
	}

	public int getIntegritaet() {
		PropertyList properties = cnaTreeElement.getEntity().getProperties(
				cnaTreeElement.getTypeId() + Schutzbedarf.INTEGRITAET);
		if (properties != null && properties.getProperties().size() > 0)
			return Schutzbedarf.toInt(properties.getProperty(0)
					.getPropertyValue());
		else
			return Schutzbedarf.UNDEF;
	}

	public int getVerfuegbarkeit() {
		PropertyList properties = cnaTreeElement.getEntity().getProperties(
				cnaTreeElement.getTypeId() + Schutzbedarf.VERFUEGBARKEIT);
		if (properties != null && properties.getProperties().size() > 0)
			return Schutzbedarf.toInt(properties.getProperty(0)
					.getPropertyValue());
		else
			return Schutzbedarf.UNDEF;
	}

	public int getVertraulichkeit() {
		PropertyList properties = cnaTreeElement.getEntity().getProperties(
				cnaTreeElement.getTypeId() + Schutzbedarf.VERTRAULICHKEIT);
		if (properties != null && properties.getProperties().size() > 0)
			return Schutzbedarf.toInt(properties.getProperty(0)
					.getPropertyValue());
		else
			return Schutzbedarf.UNDEF;
	}

	public void setIntegritaet(int i) {
		EntityType entityType = HUITypeFactory.getInstance().getEntityType(
				cnaTreeElement.getEntity().getEntityType());
		String option = Schutzbedarf.toOption(cnaTreeElement.getTypeId(),
				Schutzbedarf.INTEGRITAET, i);

		cnaTreeElement.getEntity().setSimpleValue(
				entityType.getPropertyType(cnaTreeElement.getTypeId()
						+ Schutzbedarf.INTEGRITAET), option);
//		cnaTreeElement.fireIntegritaetChanged(ta);
	}

	public void setVerfuegbarkeit(int i) {
		EntityType entityType = HUITypeFactory.getInstance().getEntityType(
				cnaTreeElement.getEntity().getEntityType());
		String option = Schutzbedarf.toOption(cnaTreeElement.getTypeId(),
				Schutzbedarf.VERFUEGBARKEIT, i);
		cnaTreeElement.getEntity().setSimpleValue(
				entityType.getPropertyType(cnaTreeElement.getTypeId()
						+ Schutzbedarf.VERFUEGBARKEIT), option);
//		cnaTreeElement.fireVerfuegbarkeitChanged(ta);
	}

	public void setVertraulichkeit(int i) {
		EntityType entityType = HUITypeFactory.getInstance().getEntityType(
				cnaTreeElement.getEntity().getEntityType());
		String option = Schutzbedarf.toOption(cnaTreeElement.getTypeId(),
				Schutzbedarf.VERTRAULICHKEIT, i);
		cnaTreeElement.getEntity().setSimpleValue(
				entityType.getPropertyType(cnaTreeElement.getTypeId()
						+ Schutzbedarf.VERTRAULICHKEIT), option);
//		cnaTreeElement.fireVertraulichkeitChanged(ta);
	}

	public String getIntegritaetDescription() {
		return cnaTreeElement.getEntity().getSimpleValue(
				cnaTreeElement.getTypeId() + Schutzbedarf.INTEGRITAET_BEGRUENDUNG);
	}

	public String getVerfuegbarkeitDescription() {
		return cnaTreeElement.getEntity().getSimpleValue(
				cnaTreeElement.getTypeId() + Schutzbedarf.VERFUEGBARKEIT_BEGRUENDUNG);
	}

	public String getVertraulichkeitDescription() {
		return cnaTreeElement.getEntity().getSimpleValue(
				cnaTreeElement.getTypeId() + Schutzbedarf.VERTRAULICHKEIT_BEGRUENDUNG);
	}

	public void setIntegritaetDescription(String text) {
		EntityType entityType = HUITypeFactory.getInstance().getEntityType(
				cnaTreeElement.getEntity().getEntityType());
		cnaTreeElement.getEntity().setSimpleValue(
				entityType.getPropertyType(cnaTreeElement.getTypeId()
						+ Schutzbedarf.INTEGRITAET_BEGRUENDUNG), text);
//		cnaTreeElement.fireIntegritaetChanged(ta);
	}

	public void setVerfuegbarkeitDescription(String text) {
		EntityType entityType = HUITypeFactory.getInstance().getEntityType(
				cnaTreeElement.getEntity().getEntityType());
		cnaTreeElement.getEntity().setSimpleValue(
				entityType.getPropertyType(cnaTreeElement.getTypeId()
						+ Schutzbedarf.VERFUEGBARKEIT_BEGRUENDUNG), text);
//		cnaTreeElement.fireVerfuegbarkeitChanged(ta);
	}

	public void setVertraulichkeitDescription(String text) {
		EntityType entityType = HUITypeFactory.getInstance().getEntityType(
				cnaTreeElement.getEntity().getEntityType());
		cnaTreeElement.getEntity().setSimpleValue(
				entityType.getPropertyType(cnaTreeElement.getTypeId()
						+ Schutzbedarf.VERTRAULICHKEIT_BEGRUENDUNG), text);
//		cnaTreeElement.fireVertraulichkeitChanged(ta);
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
				// determine protection level from parents (or keep own depending on description):
				bottomNode.getLinkChangeListener().determineVerfuegbarkeit(ta);
				
			}
			
		} catch (TransactionAbortedException tae) {
			Logger.getLogger(this.getClass()).debug(
					"Verfügbarkeitsänderung abgebrochen."); //$NON-NLS-1$
		} catch (Exception e) {
			ta.abort();
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
				// determine protection level from parents (or keep own depending on description):
				bottomNode.getLinkChangeListener().determineVertraulichkeit(ta);
				
			}
			
		} catch (TransactionAbortedException tae) {
			Logger.getLogger(this.getClass()).debug(
					"Vertraulichkeitsänderung abgebrochen."); //$NON-NLS-1$
		} catch (Exception e) {
			ta.abort();
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
				// determine protection level from parents (or keep own depending on description):
				bottomNode.getLinkChangeListener().determineIntegritaet(ta);
				
			}
			
		} catch (TransactionAbortedException tae) {
			Logger.getLogger(this.getClass()).debug(
					"Integritätsänderung abgebrochen."); //$NON-NLS-1$
		} catch (Exception e) {
			ta.abort();
		}
	}

	/**
	 * @param downwardElement
	 * @param downwardsTA 
	 * @param bottomNodes 
	 * @return
	 */
	private void findBottomNodes(CnATreeElement downwardElement, Set<CnATreeElement> bottomNodes, CascadingTransaction downwardsTA) {
		if (downwardsTA.hasBeenVisited(downwardElement))
			return;
		
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
		if (countLinks == 0)
			bottomNodes.add(downwardElement);
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

   

    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.bsi.model.ISchutzbedarfProvider#isCalculatedAvailability()
     */
    public boolean isCalculatedAvailability() {
        return false;
    }

    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.bsi.model.ISchutzbedarfProvider#isCalculatedConfidentiality()
     */
    public boolean isCalculatedConfidentiality() {
        return false;
    }

    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.bsi.model.ISchutzbedarfProvider#isCalculatedIntegrity()
     */
    public boolean isCalculatedIntegrity() {
        return false;
    }

}
