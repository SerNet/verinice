/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
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
package sernet.gs.ui.rcp.main.bsi.model;

import java.io.Serializable;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.common.model.CascadingTransaction;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.TransactionAbortedException;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.WhereAmIUtil;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.IEntityChangedListener;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyChangedEvent;
import sernet.hui.common.connect.PropertyList;
import sernet.hui.common.multiselectionlist.IMLPropertyOption;
import sernet.hui.common.multiselectionlist.IMLPropertyType;

/**
 * Adapter for elements that provide or receive protection levels.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
public class SchutzbedarfAdapter implements ISchutzbedarfProvider, Serializable {

	private CnATreeElement parent;

	public SchutzbedarfAdapter(CnATreeElement parent) {
		this.parent = parent;
	}

	public int getIntegritaet() {
		PropertyList properties = parent.getEntity().getProperties(
				parent.getTypeId() + Schutzbedarf.INTEGRITAET);
		if (properties != null && properties.getProperties().size() > 0)
			return Schutzbedarf.toInt(properties.getProperty(0)
					.getPropertyValue());
		else
			return Schutzbedarf.UNDEF;
	}

	public int getVerfuegbarkeit() {
		PropertyList properties = parent.getEntity().getProperties(
				parent.getTypeId() + Schutzbedarf.VERFUEGBARKEIT);
		if (properties != null && properties.getProperties().size() > 0)
			return Schutzbedarf.toInt(properties.getProperty(0)
					.getPropertyValue());
		else
			return Schutzbedarf.UNDEF;
	}

	public int getVertraulichkeit() {
		PropertyList properties = parent.getEntity().getProperties(
				parent.getTypeId() + Schutzbedarf.VERTRAULICHKEIT);
		if (properties != null && properties.getProperties().size() > 0)
			return Schutzbedarf.toInt(properties.getProperty(0)
					.getPropertyValue());
		else
			return Schutzbedarf.UNDEF;
	}

	public void setIntegritaet(int i, CascadingTransaction ta) {
		EntityType entityType = HUITypeFactory.getInstance().getEntityType(
				parent.getEntity().getEntityType());
		String option = Schutzbedarf.toOption(parent.getTypeId(),
				Schutzbedarf.INTEGRITAET, i);

		parent.getEntity().setSimpleValue(
				entityType.getPropertyType(parent.getTypeId()
						+ Schutzbedarf.INTEGRITAET), option);
		parent.fireIntegritaetChanged(ta);
	}

	public void setVerfuegbarkeit(int i, CascadingTransaction ta) {
		EntityType entityType = HUITypeFactory.getInstance().getEntityType(
				parent.getEntity().getEntityType());
		String option = Schutzbedarf.toOption(parent.getTypeId(),
				Schutzbedarf.VERFUEGBARKEIT, i);
		parent.getEntity().setSimpleValue(
				entityType.getPropertyType(parent.getTypeId()
						+ Schutzbedarf.VERFUEGBARKEIT), option);
		parent.fireVerfuegbarkeitChanged(ta);
	}

	public void setVertraulichkeit(int i, CascadingTransaction ta) {
		EntityType entityType = HUITypeFactory.getInstance().getEntityType(
				parent.getEntity().getEntityType());
		String option = Schutzbedarf.toOption(parent.getTypeId(),
				Schutzbedarf.VERTRAULICHKEIT, i);
		parent.getEntity().setSimpleValue(
				entityType.getPropertyType(parent.getTypeId()
						+ Schutzbedarf.VERTRAULICHKEIT), option);
		parent.fireVertraulichkeitChanged(ta);
	}

	public String getIntegritaetDescription() {
		return parent.getEntity().getSimpleValue(
				parent.getTypeId() + Schutzbedarf.INTEGRITAET_BEGRUENDUNG);
	}

	public String getVerfuegbarkeitDescription() {
		return parent.getEntity().getSimpleValue(
				parent.getTypeId() + Schutzbedarf.VERFUEGBARKEIT_BEGRUENDUNG);
	}

	public String getVertraulichkeitDescription() {
		return parent.getEntity().getSimpleValue(
				parent.getTypeId() + Schutzbedarf.VERTRAULICHKEIT_BEGRUENDUNG);
	}

	public void setIntegritaetDescription(String text, CascadingTransaction ta) {
		EntityType entityType = HUITypeFactory.getInstance().getEntityType(
				parent.getEntity().getEntityType());
		parent.getEntity().setSimpleValue(
				entityType.getPropertyType(parent.getTypeId()
						+ Schutzbedarf.INTEGRITAET_BEGRUENDUNG), text);
		parent.fireIntegritaetChanged(ta);
	}

	public void setVerfuegbarkeitDescription(String text, CascadingTransaction ta) {
		EntityType entityType = HUITypeFactory.getInstance().getEntityType(
				parent.getEntity().getEntityType());
		parent.getEntity().setSimpleValue(
				entityType.getPropertyType(parent.getTypeId()
						+ Schutzbedarf.VERFUEGBARKEIT_BEGRUENDUNG), text);
		parent.fireVerfuegbarkeitChanged(ta);
	}

	public void setVertraulichkeitDescription(String text, CascadingTransaction ta) {
		EntityType entityType = HUITypeFactory.getInstance().getEntityType(
				parent.getEntity().getEntityType());
		parent.getEntity().setSimpleValue(
				entityType.getPropertyType(parent.getTypeId()
						+ Schutzbedarf.VERTRAULICHKEIT_BEGRUENDUNG), text);
		parent.fireVertraulichkeitChanged(ta);
	}

	private void fireVerfuegbarkeitChanged(CascadingTransaction ta) {
		if (ta.hasBeenVisited(parent)) {
			Logger.getLogger(this.getClass()).debug(
					"(Verfügbarkeit) Loop on object " + parent.getTitel()); //$NON-NLS-1$

			return; // we have already been down this path
		}

		try {
			ta.enter(parent);
			for (CnALink link : parent.getLinksDown()) {
				link.getDependency().getLinkChangeListener()
						.verfuegbarkeitChanged(ta);
			}
			// FIXME server: vererbung not working try this: fire for both item and dependency
			if (ta.isInitiator(parent)) { 
				ta.end(parent);
			}
		} catch (TransactionAbortedException tae) {
			Logger.getLogger(this.getClass()).debug(
					"Verfuegbarkeit-Änderung abgebrochen.");
			// try to end properly:
			ta.end(parent);
		} catch (Exception e) {
			ta.abort();
		}
	}

	private void fireVertraulichkeitChanged(CascadingTransaction ta) {
		if (ta.hasBeenVisited(parent)) {
			Logger.getLogger(this.getClass()).debug(
					"(Vertraulichkeit) Loop on object " + parent.getTitel()); //$NON-NLS-1$
			return; // we have already been down this path
		}
		try {
			ta.enter(parent);
			for (CnALink link : parent.getLinksDown()) {
				link.getDependency().getLinkChangeListener()
						.vertraulichkeitChanged(ta);
			}
			if (ta.isInitiator(parent)) {
				ta.end(parent);
			}
		} catch (TransactionAbortedException tae) {
			Logger.getLogger(this.getClass()).debug(
					"Vertraulichkeitsänderung abgebrochen..");
			// try to end properly:
			ta.end(parent);
		} catch (Exception e) {
			ta.abort();
		}
	}
	
	private void fireIntegritaetChanged(CascadingTransaction ta) {
		if (ta.hasBeenVisited(parent)) {
			Logger.getLogger(this.getClass()).debug(
					"(Integrität) Loop on object " + parent.getTitel()); //$NON-NLS-1$
			return; // we have already been down this path
		}
		try {
			ta.enter(parent);
			for (CnALink link : parent.getLinksDown()) {
				link.getDependency().getLinkChangeListener()
						.integritaetChanged(ta);
			}
			if (ta.isInitiator(parent)) {
				ta.end(parent);
			}
		} catch (TransactionAbortedException tae) {
			Logger.getLogger(this.getClass()).debug(
					"Integritätsänderung abgebrochen.");
			// try to end properly:
			ta.end(parent);
		} catch (Exception e) {
			ta.abort();
		}
	}

	public CnATreeElement getParent() {
		return parent;
	}

	public void setParent(CnATreeElement parent) {
		this.parent = parent;
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

}
