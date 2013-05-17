package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.common.CascadingTransaction;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.HydratorUtil;
import sernet.verinice.model.common.TransactionAbortedException;

/**
 * Load all elements of given type linked to the given root element.
 */
public class LoadReportLinkedElements extends GenericCommand implements ICachedCommand{

    private transient Logger log = Logger.getLogger(LoadReportLinkedElements.class);
    
    private boolean resultInjectedFromCache = false;
    
    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(LoadReportLinkedElements.class);
        }
        return log;
    }

    // get these types of elements:
	private String typeId;
	
	// starting element:
    private Integer rootElement;
    
    // the result:
    private List<CnATreeElement> elements;
    
    // recurse down all levels of links to emelents of given type?
    private boolean goDeep = false;
    
    private boolean doUpLinksAlso = true;
    
    //for statistical use only
    private Map<String, Integer> linkTypeIdMap;
    
    public LoadReportLinkedElements(String typeId, Integer rootElement, boolean goDeep) {
	    this.typeId = typeId;
	    this.rootElement = rootElement;
	    this.goDeep= goDeep;
	}

    public LoadReportLinkedElements(String typeId, Integer rootElement, boolean goDeep, boolean doUpLinksAlso){
        this(typeId, rootElement, goDeep);
        this.doUpLinksAlso = doUpLinksAlso;
    }
    public LoadReportLinkedElements(String typeId, Integer rootElement) {
        this(typeId, rootElement, false);
    }
        
    public void execute() {
        if(linkTypeIdMap == null){
            linkTypeIdMap = new HashMap<String, Integer>(0);
        }
        if(!resultInjectedFromCache){
            LoadPolymorphicCnAElementById command = new LoadPolymorphicCnAElementById(new Integer[] {rootElement});
            try {
                command = getCommandService().executeCommand(command);
            } catch (CommandException e) {
                throw new RuntimeCommandException(e);
            }
            if (command.getElements() == null || command.getElements().size()==0) {
                elements = new ArrayList<CnATreeElement>(0);
                return;
            }
            CnATreeElement root = command.getElements().get(0);

            CascadingTransaction ta = new CascadingTransaction();
            ArrayList<CnATreeElement> result = new ArrayList<CnATreeElement>();

            elements = getLinkedElements(ta, root, result, doUpLinksAlso);


            IBaseDao<BSIModel, Serializable> dao = getDaoFactory().getDAO(BSIModel.class);
            RetrieveInfo ri = new RetrieveInfo();
            ri.setProperties(true);
            HydratorUtil.hydrateElements(dao, elements, ri);
        }

    }

    /**
     * @param root
     * @param typeId2
     * @return
     */
    private List<CnATreeElement> getLinkedElements(CascadingTransaction ta, CnATreeElement root, List<CnATreeElement> result, boolean doUpLinksAlso) {
        // FIXME externalize strings in SNCA.xml!
        String identifier = root.getTypeId() + "->" + typeId;
        if(!linkTypeIdMap.containsKey(identifier)){
            linkTypeIdMap.put(identifier, Integer.valueOf(1));
        } else {
            linkTypeIdMap.put(identifier, linkTypeIdMap.get(identifier) + 1);
        }
        for (CnALink link : root.getLinksDown()) {
            if (link.getDependency().getTypeId().equals(this.typeId) &&
                    !ta.hasBeenVisited(link.getDependency())) {
                try {
                    ta.enter(link.getDependency());
                } catch (TransactionAbortedException e) {
                    return result;
                }
                result.add(link.getDependency());
                if (goDeep){
                    getLinkedElements(ta, link.getDependency(), result, doUpLinksAlso);
                }
            }
        }
        if(doUpLinksAlso){
            for (CnALink link : root.getLinksUp()){ 
                if (link.getDependant().getTypeId().equals(this.typeId) &&
                        !ta.hasBeenVisited(link.getDependant())) {
                    try {
                        ta.enter(link.getDependant());
                    } catch (TransactionAbortedException e) {
                        return result;
                    }
                    result.add(link.getDependant());
                    if (goDeep){
                        getLinkedElements(ta, link.getDependant(), result, doUpLinksAlso);
                    }
                }
            }
        }
        return result;
    }

    /**
     * @return the elements
     */
    public List<CnATreeElement> getElements() {
        return elements;
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
        cacheID.append(String.valueOf(goDeep));
        cacheID.append(String.valueOf(doUpLinksAlso));
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
        return elements;
    }
}
