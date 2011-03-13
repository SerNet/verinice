package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.hibernate.dialect.function.CastFunction;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.RuntimeCommandException;
import sernet.gs.ui.rcp.main.bsi.views.Messages;
import sernet.gs.ui.rcp.main.service.CnATypeMapper;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.Anwendung;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.Client;
import sernet.verinice.model.bsi.Gebaeude;
import sernet.verinice.model.bsi.IBSIStrukturElement;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.NetzKomponente;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.bsi.Raum;
import sernet.verinice.model.bsi.Server;
import sernet.verinice.model.bsi.SonstIT;
import sernet.verinice.model.bsi.TelefonKomponente;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.HydratorUtil;
import sernet.verinice.model.iso27k.IISO27kElement;

/**
 * Loads an element with all links from / to it.
 * also includes the dbId of the linked element.
 */
public class LoadReportElementWithLinks extends GenericCommand {

    public static final String[] COLUMNS = new String[] {"relationName", "toAbbrev", "toElement", "riskC", "riskI", "riskA", "dbId"};

	private String typeId;
    private Integer rootElement;
    List<List<String>> result;
    private List<CnALink> linkList;

    private transient CnATypeMapper cnATypeMapper;
    
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

	/**
     * @param root
     * @param typeId2
     * @return
     */
    private void loadLinks(CnATreeElement root) {
        result = new ArrayList<List<String>>();
        for (CnALink link : root.getLinksDown()) {
            if (typeId == null )
                result.add(makeRow(root, link));
            else {
                if (link.getDependency().getTypeId().equals(typeId))
                    result.add(makeRow(root, link));
            }
        }
        for (CnALink link : root.getLinksUp()) {
            if (typeId == null )
                result.add(makeRow(root, link));
            else {
                if (link.getDependant().getTypeId().equals(typeId))
                    result.add(makeRow(root, link));
            }
        }
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
    
    
    

   

  
	


}
