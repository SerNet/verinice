package sernet.gs.ui.rcp.main.common.model;

import java.sql.Timestamp;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Class to check for model changes made by other clients.
 * 
 * This is a simple synchronisation mechanism to use until
 * we have an application server to notify clients of changes. 
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
// TODO implement notification service for clients
public class ChangeLogWatcher {

	private Date lastUpdate;
	
	private static ChangeLogWatcher instance;
	
	private ChangeLogWatcher() {
		lastUpdate = null;
	}
	
	public static synchronized ChangeLogWatcher getInstance() {
		if (instance == null)
			instance = new ChangeLogWatcher();
		return instance;
	}
	
	
	/**
	 * Get all changes made by another client since our last known
	 * timestamp.
	 * 
	 * @return
	 */
	public List<ChangeLogEntry> getNewChanges() {
		if (lastUpdate == null) {
			lastUpdate = CnAElementHome.getInstance().getCurrentTime();
		}
		
		List<ChangeLogEntry> changes = CnAElementHome.getInstance().loadChangesSince(lastUpdate);
		if (changes.size() > 0)
			lastUpdate = changes.get(changes.size()-1).getChangetime();
		return changes;
	}

	/**
	 * Go through transaction log and refresh every object that has been changed
	 * by another client.
	 * @throws Exception 
	 */
	public void updateChanges(Object watchOutFor) throws ObjectDeletedException {
//		List<ChangeLogEntry> changes = getNewChanges();
//		if (changes.size() < 1)
//			return;
//		
//		for(ChangeLogEntry change : changes) {
//			switch (change.getChange()) {
//			case ChangeLogEntry.UPDATE:
//				refresh(change);
//				break;
//			
//			case ChangeLogEntry.INSERT:
//				CnATreeElement newObject = CnAElementHome
//					.getInstance()
//					.loadById(change.getElementClass(), change.getElementId());
//				CnAElementHome.getInstance().refresh(newObject.getParent());
//				newObject.getParent().childAdded(newObject.getParent(), newObject);
//				CnAElementFactory.getCurrentModel().refreshAllListeners();
//				break;
//			
//			case ChangeLogEntry.DELETE:
//				Object object = CnAElementHome.getInstance()
//					.getElementInSession(change.getElementClass(), change.getElementId());
//				((CnATreeElement)object).remove();
//				if (object.equals(watchOutFor))
//					throw new ObjectDeletedException("Object was deleted.");
//				break;
//			}
//		}
	}
	
	public void refresh(ChangeLogEntry change) {
//		Object object = CnAElementHome.getInstance().getElementInSession(change.getElementClass(), change.getDbId());
//		if (object != null) {
//			Logger.getLogger(this.getClass()).debug("Refreshing updated object " + object);
//			CnAElementHome.getInstance().refresh((CnATreeElement)object);
//		}
	}
}
