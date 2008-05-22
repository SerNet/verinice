/**
 * 
 */
package sernet.gs.ui.rcp.main.bsi.views;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;

import sernet.gs.model.Baustein;
import sernet.gs.service.GSServiceException;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.bsi.model.BSIMassnahmenModel;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;

public class BSIKatalogInvisibleRoot {
	
	/**
	 * Listen for preference changes and update model if necessary:
	 */
	private final IPropertyChangeListener prefChangeListener
		= new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(
						PreferenceConstants.BSIZIPFILE)
						|| event.getProperty().equals(
								PreferenceConstants.BSIDIR)
						|| event.getProperty().equals(
								PreferenceConstants.GSACCESS))

					try {
						WorkspaceJob job = new OpenCataloguesJob(Messages.BSIMassnahmenView_0);
					    job.setUser(true);
					    job.schedule();
					 } catch (Exception e) {
						Logger.getLogger(this.getClass())
						.error(Messages.BSIMassnahmenView_2, e);
					}
				
			}
	};
	
	public interface ISelectionListener {
		public void cataloguesChanged();
	}
	

	private class NullBaustein extends Baustein {
		@Override
		public String toString() {
			return "GS-Kataloge nicht geladen.";
		}
	}
	
	private static BSIKatalogInvisibleRoot instance;
	List<Baustein> bausteine = new ArrayList<Baustein>();
	
	private List<ISelectionListener> listeners = new ArrayList<ISelectionListener>(5);
	
	public void addListener(ISelectionListener listener) {
		synchronized (listeners) {
			if (! listeners.contains(listener))
				listeners.add(listener);
		}
	}
	
	public void removeListener(ISelectionListener lst) {
		synchronized (listeners) {
			listeners.remove(lst);
		}
	}
	
	private void fireChanged() {
		synchronized (listeners) {
			for (ISelectionListener listener : listeners) {
				listener.cataloguesChanged();
			}
		}
	}

	public List<Baustein> getBausteine() {
		if (bausteine.size() < 1)		
			bausteine.add(new NullBaustein());
		return bausteine;
	}
	
	
	
	public void setBausteine(List<Baustein> bst) {
		if (bst == null) {
			bausteine= new ArrayList<Baustein>();
		}
		else {
			this.bausteine = bst;
		}
		fireChanged();
	}
	
	@Override
	protected void finalize() throws Throwable {
		Activator.getDefault().getPluginPreferences()
		.removePropertyChangeListener(this.prefChangeListener);
	}
	
	private BSIKatalogInvisibleRoot() {
		Activator.getDefault().getPluginPreferences()
		.addPropertyChangeListener(this.prefChangeListener);
	}

	public static BSIKatalogInvisibleRoot getInstance() {
		if (instance == null)
			instance = new BSIKatalogInvisibleRoot();
		return instance;
	}

	public void loadModel(IProgressMonitor monitor) throws GSServiceException, IOException {
		setBausteine(
				BSIMassnahmenModel.loadBausteine(monitor));
	}

	public Baustein getBaustein(String id) {
		for (Baustein baustein : bausteine) {
			if (baustein.getId().equals(id))
				return baustein;
		}
		return null;
	}
}