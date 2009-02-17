package sernet.gs.ui.rcp.main.common.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.editors.EditorRegistry;

/**
 * A transaction that can only be stopped by the object that started it.
 * 
 * Represents a unit of work started by a particular
 * object and ended by that object. Can be used to detect loops 
 * when traversing object graphs.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class CascadingTransaction {

	private Object initiator;
	private Set<CnATreeElement> visited = new HashSet<CnATreeElement>();
	private boolean aborted;
	
	private Object loopObject = "";
	private boolean loopDetected = false;

	
	public CascadingTransaction() {}
	
	
	
	/**
	 * The given objects enters the transaction.
	 * 
	 * @param obj The object which should run inside the transaction
	 * @return true if a new transation was created, false if object was added to existing transaction
	 * @throws TransactionAbortedException if this transaction was aborted by one of the participants
	 */
	public synchronized boolean enter(CnATreeElement obj) throws TransactionAbortedException {
		if (aborted) // cannot enter aborted transaction:
			throw new TransactionAbortedException();
		
		// keep track of entered objects:
		visited.add(obj);
		if (this.initiator != null)
			return false;
		
		// create new transaction:
		this.initiator = obj;
		aborted = false;
		return true;
	}
	
	public synchronized boolean isInitiator(CnATreeElement o) {
		return o.equals(initiator);
	}
	
	public synchronized boolean hasBeenVisited(Object o) {
		boolean loop = visited.contains(o) || aborted;
		if (loop) {
			loopDetected = true;
			loopObject = o;
		}
		return loop;
	}
	
	public void abort() {
		this.aborted = true;
	}
	
	public boolean isAborted() {
		return aborted;
	}
	
	public synchronized boolean end(CnATreeElement o) {
		if (!o.equals(initiator))
			return false;
		
		visited = new HashSet<CnATreeElement>();
		initiator = null;
		aborted = false;
		return true;
	}

	public void saveUpdatedItems() throws Exception {
		final List tosave = new ArrayList(visited.size());
		for(CnATreeElement item: visited) {
			if (EditorRegistry.getInstance().getOpenEditor(item.getId()) == null)
				// no editor open, save item silently:
				// only save items that were changed as result of the cascade:
				if (!item.equals(initiator))
					tosave.add(item);
		}
		
		if (tosave.size() == 0)
			return;
		
		CnAElementHome.getInstance().update(tosave);
	}



	public boolean hasLooped() {
		return this.loopDetected;
	}
	
	public Object getLoopedObject() {
		return this.loopObject;
	}
	

}
