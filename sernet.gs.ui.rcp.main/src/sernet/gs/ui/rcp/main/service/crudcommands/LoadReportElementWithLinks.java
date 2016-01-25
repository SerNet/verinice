package sernet.gs.ui.rcp.main.service.crudcommands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.model.bsi.IBSIStrukturElement;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.IISO27kElement;
import sernet.verinice.service.commands.CnATypeMapper;

/**
 * Loads an element with all links from / to it.
 * also includes the dbId of the linked element.
 */
public class LoadReportElementWithLinks extends GenericCommand implements ICachedCommand{

    public static final String[] COLUMNS = new String[] {"relationName", "toAbbrev", "toElement", "riskC", "riskI", "riskA", "dbId"};

	private String typeId;
    private Integer rootElement;
    private List<List<String>> result;
    private List<CnALink> linkList;

    private transient CnATypeMapper cnATypeMapper;
    
    private boolean resultInjectedFromCache = false;
    
    public LoadReportElementWithLinks(String typeId, Integer rootElement) {
	    this.typeId = typeId;
	    this.rootElement = rootElement;
	}

    public LoadReportElementWithLinks(String typeId, String rootElement) {
        this.typeId = typeId;
        try {
            this.rootElement = Integer.parseInt(rootElement);
        } catch(NumberFormatException e) {
            this.rootElement=-1;
        }
    }
	
	public void execute() {
	    if(!resultInjectedFromCache){
	        cnATypeMapper = new CnATypeMapper();
	        linkList = new ArrayList<CnALink>();

	        LoadPolymorphicCnAElementById command = new LoadPolymorphicCnAElementById(new Integer[] {rootElement}); 
	        try {
	            command = getCommandService().executeCommand(command);
	        } catch (CommandException e) {
	            throw new RuntimeCommandException(e);
	        }
	        if (command.getElements() == null || command.getElements().size()==0) {
	            result = new ArrayList<List<String>>(0);
	            return;
	        }
	        CnATreeElement root = command.getElements().get(0);

	        loadLinks(root);
	    }
	    
	}

	/**
     * @param root
     * @param typeId2
     * @return
     */
    private void loadLinks(CnATreeElement root) {
        result = new ArrayList<List<String>>();

        for (CnALink link : getSortedLinkList(root, false, true)) {
            if (typeId == null ){
                result.add(makeRow(root, link));
            } else {
                if (link.getDependency().getTypeId().equals(typeId)){
                    result.add(makeRow(root, link));
                }
            }
        }
        for (CnALink link : getSortedLinkList(root, true, false)) {
            if (typeId == null )
                result.add(makeRow(root, link));
            else {
                if (link.getDependant().getTypeId().equals(typeId)){
                    result.add(makeRow(root, link));
                }
            }
        }
    }
    
    private List<CnALink> getSortedLinkList(final CnATreeElement root, boolean upLinks, boolean downLinks){
        Set<CnALink> list = new HashSet<CnALink>();
        if(upLinks){
            list.addAll(root.getLinksUp());
        } 
        if(downLinks){
            list.addAll(root.getLinksDown());
        }
        ArrayList<CnALink> sortedList = new ArrayList<CnALink>(0);
        for(CnALink link : list){
            sortedList.add(link);
        }
        Collections.sort(sortedList, new Comparator<CnALink>() {
            //sorts list on the basis of the title of the linked element
            @Override
            public int compare(CnALink o1, CnALink o2) {
                return CnALink.getRelationObjectTitle(root, o1).compareTo(CnALink.getRelationObjectTitle(root, o2));
            }
        });
        return sortedList;
    }
    

    /**
     * @param root
     * @param link
     * @return
     */
    private List<String> makeRow(CnATreeElement root, CnALink link) {
        linkList.add(link);
        String relationName = CnALink.getRelationNameReplacingEmptyNames(root, link);
        String toElementTitle = CnALink.getRelationObjectTitle(root, link);
        String toAbbrev = getAbbreviation(link.getRelationObject(root, link));
        String riskC = Integer.toString( link.getRiskConfidentiality()  != null ? link.getRiskConfidentiality() : 0);
        String riskI = Integer.toString(link.getRiskIntegrity()         != null ? link.getRiskIntegrity()       : 0);
        String riskA = Integer.toString(link.getRiskAvailability()      != null ? link.getRiskAvailability()    : 0);
        
        CnATreeElement otherSide = link.getRelationObject(root, link);
        String otherSideDbId = Integer.toString(otherSide.getDbId());
        
        // arrays.aslist returns a non resizeable list (throws UnsupportedOperationExceptions for half of the List interface),
        // to prevent error in use we copy it:
        List<String> asList = Arrays.asList(relationName, toAbbrev, toElementTitle, riskC, riskI, riskA, otherSideDbId);
        ArrayList<String> resizeableList = new ArrayList<String>();
        resizeableList.addAll(asList);
        return resizeableList;
    }


    /**
     * Get abbreviation property for elements.
     * We have to reload them using the correct type because Hibernate otherwise does not include
     * the methods from the interfaces IBSIStrukturElement or IISO27kElement.
     * 
     * @param relationObject
     * @return
     */
    @SuppressWarnings("unchecked")
    private String getAbbreviation(CnATreeElement relationObject) {
        if (cnATypeMapper.isStrukturElement(relationObject) ) {
            IBaseDao dao = getDaoFactory().getDAO(relationObject.getTypeId());
            IBSIStrukturElement elmt = (IBSIStrukturElement) dao.findById(relationObject.getDbId());
            return elmt.getKuerzel();
        }
        if (cnATypeMapper.isIiso27kElement(relationObject)) {
            IBaseDao dao = getDaoFactory().getDAO(relationObject.getTypeId());
            IISO27kElement elmt = (IISO27kElement) dao.findById(relationObject.getDbId());
            return elmt.getAbbreviation();
        }
        return "";
        
    }

    /**
     * @return the result
     */
    public List<List<String>> getResult() {
        return result;
    }

    /**
     * @return the linkList
     */
    public List<CnALink> getLinkList() {
        return linkList;
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
        if(result instanceof Object[]){
            Object[] array = (Object[])result;
            this.result = (ArrayList<List<String>>)array[0];
            this.linkList = (ArrayList<CnALink>)array[1];
            resultInjectedFromCache = true;
        }
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheableResult()
     */
    @Override
    public Object getCacheableResult() {
        Object[] resultForCache = new Object[2];
        resultForCache[0] = this.result;
        resultForCache[1] = this.linkList;
        return resultForCache;
    }

}
