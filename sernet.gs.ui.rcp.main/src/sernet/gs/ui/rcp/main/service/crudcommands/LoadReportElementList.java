package sernet.gs.ui.rcp.main.service.crudcommands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Load lsit of elements and their databse ids for further use in reports.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class LoadReportElementList extends GenericCommand implements ICachedCommand{

    private transient Logger log = Logger.getLogger(LoadReportElementList.class);
    
    public Logger getLog() {
        if(log==null) {
            log = Logger.getLogger(LoadReportElementList.class);
        }
        return log;
    }

	private String typeId;
    private Integer rootElement;
    private List<CnATreeElement> elements;
    
    private boolean resultInjectedFromCache = false;
    
    public LoadReportElementList(String typeId, Integer rootElement) {
	    this.typeId = typeId;
	    this.rootElement = rootElement;
	}
	
	public void execute() {
	    if(!resultInjectedFromCache){
	        getLog().debug("LoadReportElements for root_object " + rootElement);

	        LoadPolymorphicCnAElementById command = new LoadPolymorphicCnAElementById(new Integer[] {rootElement});
	        try {
	            command = getCommandService().executeCommand(command);
	        } catch (CommandException e) {
	            throw new RuntimeCommandException(e);
	        }
	        CnATreeElement root = command.getElements().get(0);

	        //if typeId is that of the root object, just return it itself. else look for children:
	        ArrayList<CnATreeElement> items = new ArrayList<CnATreeElement>();
	        if (this.typeId.equals(root.getTypeId())) {
	            this.elements = items;
	            this.elements.add(root);
	        }
	        else {
	            try {
	                LoadReportElements elementLoader = new LoadReportElements(typeId, root.getDbId(), true);
	                elementLoader = getCommandService().executeCommand(elementLoader);
	                this.elements.addAll(elementLoader.getElements());
	            } catch (CommandException e) {
	                getLog().error("Error while retrieving elements", e);
	            }
	        }

	        // load lazy fields:
	        getResult();
	    }
	}
	
	public static final String[] COLUMNS = new String[] {"elmt_id", "elmt_name"};
	/**
     * @return the result
     */
    public List<List<String>> getResult() {
        List<List<String>> result = new ArrayList<List<String>>();
        for (CnATreeElement elmt : elements) {
            List<String> row = Arrays.asList(elmt.getDbId().toString(), elmt.getTitle());
            result.add(row);
        }
        return result;
    }

    public void getElements(String typeFilter, List<CnATreeElement> items, CnATreeElement parent) {
        for (CnATreeElement child : parent.getChildren()) {
            if (typeFilter != null && typeFilter.length()>0) {
                if (child.getTypeId().equals(typeFilter)) {
                    items.add(child);
                    child.getParent().getTitle();
                }
            } else {
                items.add(child);
                child.getParent().getTitle();
            }
            getElements(typeFilter, items, child);
        }
        
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheID()
     */
    @Override
    public String getCacheID() {
        StringBuilder cacheID = new StringBuilder();
        cacheID.append(this.getClass().getSimpleName());
        cacheID.append(typeId);
        cacheID.append(String.valueOf(rootElement));
        return cacheID.toString();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#injectCacheResult(java.lang.Object)
     */
    @Override
    public void injectCacheResult(Object result) {
        this.elements = (ArrayList<CnATreeElement>)result;
        resultInjectedFromCache = true;
        if(getLog().isDebugEnabled()){
            getLog().debug("Result in " + this.getClass().getCanonicalName() + " injected from cache");
        }
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheableResult()
     */
    @Override
    public Object getCacheableResult() {
        return this.elements;
    }
	
}
