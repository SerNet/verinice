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
 *     Robert Schuster <r.schuster@tarent.de> - streamline catalog (re)-loading
 ******************************************************************************/
/**
 * 
 */
package sernet.gs.ui.rcp.main.bsi.views;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;

import sernet.gs.model.Baustein;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.bsi.model.BSIConfigurationRCPLocal;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;

public final class BSIKatalogInvisibleRoot {
	
	private static final Logger LOG = Logger.getLogger(BSIKatalogInvisibleRoot.class); 

	private static Pattern kapitelPattern = Pattern.compile("(\\d+)\\.(\\d+)"); //$NON-NLS-1$
	
	private static final int DEFAULT_LISTENER_AMOUNT = 5;
	
	private static final int WHOLE_FACTOR = 1000;

	/**
	 * Listen for preference changes and update model if necessary:
	 */
	private final IPropertyChangeListener prefChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(PreferenceConstants.BSIZIPFILE)
					|| event.getProperty().equals(PreferenceConstants.BSIDIR)
					|| event.getProperty().equals(PreferenceConstants.GSACCESS)
					|| event.getProperty()
							.equals(PreferenceConstants.DSZIPFILE))
			{
				LOG.debug("Reloading catalogues since catalogue properties changed: " + event.getProperty()); //$NON-NLS-1$
				try {
					// Load the catalogues using a configuration object which points
					// to local files.
					WorkspaceJob job = new OpenCataloguesJob(
							Messages.BSIMassnahmenView_0,
							new BSIConfigurationRCPLocal());
					job.setUser(true);
					job.schedule();
				} catch (Exception e) {
					Logger.getLogger(this.getClass()).error(
							Messages.BSIMassnahmenView_2, e);
				}
			}
		}
	};

	public interface ISelectionListener {
		void cataloguesChanged();
	}

	private class NullBaustein extends Baustein {
		private static final long serialVersionUID = -399972333143198070L;

		@Override
		public String toString() {
			return Messages.BSIKatalogInvisibleRoot_2;
		}
	}

	private static volatile BSIKatalogInvisibleRoot instance;
	private List<Baustein> bausteine = new ArrayList<Baustein>();

	private List<ISelectionListener> listeners = new ArrayList<ISelectionListener>(
			DEFAULT_LISTENER_AMOUNT);

	public void addListener(ISelectionListener listener) {
		synchronized (listeners) {
			if (!listeners.contains(listener)){
				listeners.add(listener);
			}
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
		if (bausteine.size() < 1){
			bausteine.add(new NullBaustein());
		}
		return bausteine;
	}

	void setBausteine(List<Baustein> bst) {
		if (bst == null) {
			bausteine = new ArrayList<Baustein>();
		} else {
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
		if (instance == null){
			instance = new BSIKatalogInvisibleRoot();
		}
		return instance;
	}

	public Baustein getBaustein(String id) {
		for (Baustein baustein : bausteine) {
			if (baustein.getId().equals(id)){
				return baustein;
			}
		}
		return null;
	}

	public Baustein getBausteinByKapitel(String id) {
		Matcher m = kapitelPattern.matcher(id);
		if (m.find()) {
			int whole = Integer.parseInt(m.group(1));
			int radix = Integer.parseInt(m.group(2));
			int kapitelValue = whole * WHOLE_FACTOR + radix;

			for (Baustein baustein : bausteine) {
				if (baustein.getKapitelValue() == kapitelValue){
					return baustein;
				}
			}
		}
		return null;
	}

}
