package sernet.gs.ui.rcp.main.bsi.model;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.widgets.Display;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.StatusLine;
import sernet.gs.ui.rcp.main.common.model.CascadingTransaction;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.TransactionAbortedException;
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
public class SchutzbedarfAdapter implements ISchutzbedarfProvider {
	private CnATreeElement parent;
	
	private static boolean loopWarning = true;

	private IEntityChangedListener changeListener = new IEntityChangedListener() {
		public void dependencyChanged(IMLPropertyType type,
				IMLPropertyOption opt) {
		}

		public void propertyChanged(PropertyChangedEvent evt) {
			checkSchutzbedarfChanged(evt);
			checkSchutzbedarfBegruendungChanged(evt);
		}

		public void selectionChanged(IMLPropertyType type, IMLPropertyOption opt) {
		}

	};

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

	public void setIntegritaet(int i) {
		EntityType entityType = HUITypeFactory.getInstance().getEntityType(
				parent.getEntity().getEntityType());
		String option = Schutzbedarf.toOption(parent.getTypeId(),
				Schutzbedarf.INTEGRITAET, i);

		parent.getEntity().setSimpleValue(
				entityType.getPropertyType(parent.getTypeId()
						+ Schutzbedarf.INTEGRITAET), option);
	}

	public void setVerfuegbarkeit(int i) {
		EntityType entityType = HUITypeFactory.getInstance().getEntityType(
				parent.getEntity().getEntityType());
		String option = Schutzbedarf.toOption(parent.getTypeId(),
				Schutzbedarf.VERFUEGBARKEIT, i);
		parent.getEntity().setSimpleValue(
				entityType.getPropertyType(parent.getTypeId()
						+ Schutzbedarf.VERFUEGBARKEIT), option);
	}

	public void setVertraulichkeit(int i) {
		EntityType entityType = HUITypeFactory.getInstance().getEntityType(
				parent.getEntity().getEntityType());
		String option = Schutzbedarf.toOption(parent.getTypeId(),
				Schutzbedarf.VERTRAULICHKEIT, i);
		parent.getEntity().setSimpleValue(
				entityType.getPropertyType(parent.getTypeId()
						+ Schutzbedarf.VERTRAULICHKEIT), option);
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

	public void setIntegritaetDescription(String text) {
		EntityType entityType = HUITypeFactory.getInstance().getEntityType(
				parent.getEntity().getEntityType());
		parent.getEntity().setSimpleValue(
				entityType.getPropertyType(parent.getTypeId()
						+ Schutzbedarf.INTEGRITAET_BEGRUENDUNG), text);
	}

	public void setVerfuegbarkeitDescription(String text) {
		EntityType entityType = HUITypeFactory.getInstance().getEntityType(
				parent.getEntity().getEntityType());
		parent.getEntity().setSimpleValue(
				entityType.getPropertyType(parent.getTypeId()
						+ Schutzbedarf.VERFUEGBARKEIT_BEGRUENDUNG), text);
	}

	public void setVertraulichkeitDescription(String text) {
		EntityType entityType = HUITypeFactory.getInstance().getEntityType(
				parent.getEntity().getEntityType());
		parent.getEntity().setSimpleValue(
				entityType.getPropertyType(parent.getTypeId()
						+ Schutzbedarf.VERTRAULICHKEIT_BEGRUENDUNG), text);
	}

	public IEntityChangedListener getChangeListener() {
		return changeListener;
	}

	protected void checkSchutzbedarfBegruendungChanged(PropertyChangedEvent evt) {
		if (evt.getSource()==null)
			return; //not changed by user
		
		boolean integritaetChanged = Schutzbedarf
				.isIntegritaetBegruendung(evt.getProperty());
		boolean vertraulichkeitChanged = Schutzbedarf
				.isVertraulichkeitBegruendung(evt.getProperty());
		boolean verfuegbarkeitChanged = Schutzbedarf
				.isVerfuegbarkeitBegruendung(evt.getProperty());

		// if set to "maximumprinzip", cause update to all listeners:
		if (integritaetChanged) {
			parent.getLinkChangeListener().integritaetChanged();
		}
		if (vertraulichkeitChanged) {
			parent.getLinkChangeListener().vertraulichkeitChanged();
		}
		if (verfuegbarkeitChanged) {
			parent.getLinkChangeListener().verfuegbarkeitChanged();
		}
	}

	protected void checkSchutzbedarfChanged(PropertyChangedEvent evt) {
		boolean verfuegbarkeit = Schutzbedarf.isVerfuegbarkeit(evt.getProperty());
		boolean vertraulichkeit = Schutzbedarf.isVertraulichkeit(evt.getProperty());
		boolean integritaet = Schutzbedarf.isIntegritaet(evt.getProperty());
		
		// if user sets protection level manually, disable "maximumprinzip" setting:
		boolean setByUser =  (evt.getSource() != null);

		if (verfuegbarkeit) {
			fireVerfuegbarkeitChanged();
			if (setByUser && Schutzbedarf.isMaximumPrinzip(getVerfuegbarkeitDescription()))
				setVerfuegbarkeitDescription(""); //$NON-NLS-1$
				
		}
		
		if (vertraulichkeit) {
			fireVertraulichkeitChanged();
			if (setByUser && Schutzbedarf.isMaximumPrinzip(getVertraulichkeitDescription()))
				setVertraulichkeitDescription(""); //$NON-NLS-1$
		}
		
		if (integritaet) {
			fireIntegritaetChanged();
			if (setByUser && Schutzbedarf.isMaximumPrinzip(getIntegritaetDescription()))
				setIntegritaetDescription(""); //$NON-NLS-1$
		}

	}
	
	public void updateAll() {
		fireVerfuegbarkeitChanged();
		fireVertraulichkeitChanged();
		fireIntegritaetChanged();
	}
	
	private void fireVerfuegbarkeitChanged() {
		CascadingTransaction ta = CascadingTransaction.getInstance();
		if (ta.hasBeenVisited(parent)) {
			StatusLine.setErrorMessage(Messages.SchutzbedarfAdapter_3
					+ parent.getTitle());
			Logger.getLogger(this.getClass()).debug("(Verfügbarkeit) Loop on object " + parent.getTitle()); //$NON-NLS-1$

			showLoopWarning(parent.getTitle());
			
			
			return; // we have already been down this path
		}
		
		try {
			ta.enter(parent);
			for (CnALink link : parent.getLinksDown()) {
				link.getDependency().getLinkChangeListener()
				.verfuegbarkeitChanged();
			}
			if (ta.isInitiator(parent)) {
				ta.saveUpdatedItems();
				ta.end(parent);
			}
		} catch (TransactionAbortedException tae) {
			Logger.getLogger(this.getClass()).debug(Messages.SchutzbedarfAdapter_5);
			// try to end properly:
			ta.end(parent);
		} catch (Exception e) {
			ta.abort();
		}
	}

	private void showLoopWarning(String name) {
		if (loopWarning) {
			MessageDialogWithToggle dialog = MessageDialogWithToggle.openError(
					Display.getCurrent().getActiveShell(),
					Messages.SchutzbedarfAdapter_6, 
					Messages.SchutzbedarfAdapter_7+ name + Messages.SchutzbedarfAdapter_8 +
					Messages.SchutzbedarfAdapter_9 +
					Messages.SchutzbedarfAdapter_10 +
					Messages.SchutzbedarfAdapter_11 + name + Messages.SchutzbedarfAdapter_12,
					Messages.SchutzbedarfAdapter_13,
					false/*toggle default*/, null/*preferences container*/, null/*preference key*/);
			loopWarning = !dialog.getToggleState();
		}
	}

	private void fireVertraulichkeitChanged() {
		CascadingTransaction ta = CascadingTransaction.getInstance();
		if (ta.hasBeenVisited(parent)) {
			StatusLine.setErrorMessage(Messages.SchutzbedarfAdapter_14
					+ parent.getTitle());
			Logger.getLogger(this.getClass()).debug("(Vertraulichkeit) Loop on object " + parent.getTitle());			 //$NON-NLS-1$
			showLoopWarning(parent.getTitle());
			return; // we have already been down this path
		}
		try {
			ta.enter(parent);
			for (CnALink link : parent.getLinksDown()) {
				link.getDependency().getLinkChangeListener()
				.vertraulichkeitChanged();
			}
			if (ta.isInitiator(parent)) {
				ta.saveUpdatedItems();
				ta.end(parent);
			}
		} catch (TransactionAbortedException tae) {
			Logger.getLogger(this.getClass()).debug(Messages.SchutzbedarfAdapter_16);
			// try to end properly:
			ta.end(parent);
		} catch (Exception e) {
			ta.abort();
		}
	}

	private void fireIntegritaetChanged() {
		CascadingTransaction ta = CascadingTransaction.getInstance();
		if (ta.hasBeenVisited(parent)) {
			StatusLine.setErrorMessage(Messages.SchutzbedarfAdapter_17
					+ parent.getTitle());
			Logger.getLogger(this.getClass()).debug("(Integrität) Loop on object " + parent.getTitle());			 //$NON-NLS-1$
			showLoopWarning(parent.getTitle());
			return; // we have already been down this path
		}
		try {
			ta.enter(parent);
			for (CnALink link : parent.getLinksDown()) {
				link.getDependency().getLinkChangeListener()
				.integritaetChanged();
			}
			if (ta.isInitiator(parent)) {
				ta.saveUpdatedItems();
				ta.end(parent);
			}
		} catch (TransactionAbortedException tae) {
			Logger.getLogger(this.getClass()).debug(Messages.SchutzbedarfAdapter_19);
			// try to end properly:
			ta.end(parent);
		} catch (Exception e) {
			ta.abort();
		}
	}

}
