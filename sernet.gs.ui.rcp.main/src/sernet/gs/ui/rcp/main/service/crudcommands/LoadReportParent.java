package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.IBSIStrukturElement;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.HydratorUtil;

/**
 * Loads parent of given element.
 */
public class LoadReportParent extends GenericCommand implements ICachedCommand{


    private Integer rootElement;
    private List<CnATreeElement> elements;
    
    private transient Logger log = Logger.getLogger(LoadReportParent.class);
    
    private boolean resultInjectedFromCache = false;
    
    public LoadReportParent(Integer rootElement) {
	    this.rootElement = rootElement;
	}

    public LoadReportParent(String rootElement) {
        Integer dbid = -1;
        try {
            dbid = Integer.parseInt(rootElement);
        } catch (NumberFormatException e) {
            // ignore
        }
        this.rootElement = dbid;
    }

    public void execute() {
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

            elements = getParentElement(root);

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
    private List<CnATreeElement> getParentElement(CnATreeElement root) {
        ArrayList<CnATreeElement> result = new ArrayList<CnATreeElement>();
        if (root.getParent() != null) {
            result.add(root.getParent());
        }
        return result; 
    }

    /**
     * @return the elements
     */
    public List<CnATreeElement> getElements() {
        return elements;
    }
    
    public String[][] getRow() {
        if (elements == null || elements.size()==0) {
            return new String[0][0];
        }
        String[][] row = new String[1][2];
        CnATreeElement elmt = elements.get(0);
        if (elmt instanceof IBSIStrukturElement) {
            row[0][0] = ((IBSIStrukturElement)elmt).getKuerzel();
        }
        row[0][1] = elements.get(0).getTitle();
        return row;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheID()
     */
    @Override
    public String getCacheID() {
        StringBuilder cacheID = new StringBuilder();
        cacheID.append(this.getClass().getSimpleName());
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

    private Logger getLog(){
        if(log == null){
            log = Logger.getLogger(LoadReportParent.class);
        }
        return log;
    }
}
