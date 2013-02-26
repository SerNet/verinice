package sernet.gs.ui.rcp.main.service.crudcommands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.service.NumericStringComparator;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.Organization;

@SuppressWarnings("serial")
public class LoadReportAllControls extends GenericCommand implements ICachedCommand {

	private int rootElementId;
	
	private Set<Control> result;
	
	private transient Logger logger;
	
    private boolean resultInjectedFromCache = false;
    
    private List<Control> listResult = null;
	
	public LoadReportAllControls(Integer root){
		rootElementId = root;
		logger = Logger.getLogger(LoadReportAllControls.class);
	}
	
	private Set<Control> getControlChildren(CnATreeElement elmt){
		Set<Control> controls = new HashSet<Control>();
		for(CnATreeElement e : elmt.getChildren()){
			if(e instanceof Control){
				controls.add((Control)e);
			}
			controls.addAll(getControlChildren(e));
		}
		return controls;
	}
	
	@Override
	public void execute() {
	    result = new HashSet<Control>(0);
	    listResult = new ArrayList<Control>(0);
	    if(!resultInjectedFromCache){
	        LoadCnAElementById command = new LoadCnAElementById(Organization.TYPE_ID, rootElementId);
	        try {
	            command = getCommandService().executeCommand(command);
	            if(command.getFound() == null){
	                command = new LoadCnAElementById(ITVerbund.TYPE_ID, rootElementId);
	                command = getCommandService().executeCommand(command);
	            }
	            if(command.getFound() != null){
	                result = getControlChildren(command.getFound());
	            }
	            
	            for(Control c : result){
	                if(!c.isChildrenLoaded()){
	                    c = (Control)loadChildren(c);
	                }
	                listResult.add(c);
	            }
	            Collections.sort(listResult, new Comparator<CnATreeElement>() {
	                @Override
	                public int compare(CnATreeElement o1, CnATreeElement o2) {
	                    NumericStringComparator comparator = new NumericStringComparator();
	                    return comparator.compare(o1.getTitle(), o2.getTitle());
	                }
	            });
	        } catch (Exception e) {
	            logger.error("Error while executing command", e);
	        }
	    }
	}
	
	public List<Control> getResult(){
	    if(!resultInjectedFromCache){
	        return listResult;
	    } else {
	        return Arrays.asList(result.toArray(new Control[result.size()]));
	    }
	}
	
    private CnATreeElement loadChildren(CnATreeElement el) {
        if (el.isChildrenLoaded()) {
            return el;
        } 

        LoadChildrenForExpansion command;
        command = new LoadChildrenForExpansion(el);
        try {
            command = getCommandService().executeCommand(command);
            CnATreeElement newElement = command.getElementWithChildren();
            newElement.setChildrenLoaded(true);
            return newElement;
        } catch (CommandException e) {
            logger.error("error while loading children of CnaTreeElment", e);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheID()
     */
    @Override
    public String getCacheID() {
        StringBuilder cacheID = new StringBuilder();
        cacheID.append(this.getClass().getSimpleName());
        cacheID.append(String.valueOf(rootElementId));
        return cacheID.toString();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#injectCacheResult(java.lang.Object)
     */
    @Override
    public void injectCacheResult(Object result) {
        this.result = (HashSet<Control>)result;
        resultInjectedFromCache = true;
        if(getLog().isDebugEnabled()){
            getLog().debug("Result in " + this.getClass().getCanonicalName() + " injected from cache");
        }
    }
    
    public Logger getLog(){
        if(logger == null){
            logger = Logger.getLogger(LoadReportAllControls.class);
        }
        return logger;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheableResult()
     */
    @Override
    public Object getCacheableResult() {
        return result;
    }


}
