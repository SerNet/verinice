package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.CnATypeMapper;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.taskcommands.riskanalysis.FindRiskAnalysisListsByParentID;
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
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.NetzKomponente;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.bsi.Raum;
import sernet.verinice.model.bsi.SchutzbedarfAdapter;
import sernet.verinice.model.bsi.Server;
import sernet.verinice.model.bsi.SonstIT;
import sernet.verinice.model.bsi.TelefonKomponente;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysis;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysisLists;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.RisikoMassnahmenUmsetzung;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.HydratorUtil;

/**
 * Loads elements for risk analysis report according to BSI 100-3.
 */
public class LoadReportRiskAnalysis extends GenericCommand {

    private Integer raElementID;
    private FinishedRiskAnalysisLists lists = null;
    private FinishedRiskAnalysis riskAnalysis = null;
    private List<List<String>> zielobjektResult = new ArrayList<List<String>>(0);
    
    public LoadReportRiskAnalysis(Integer rootElement) {
	    this.raElementID = rootElement;
	}

    public LoadReportRiskAnalysis(String rootElement) {
        try {
            this.raElementID = Integer.parseInt(rootElement);
        } catch(Exception e) {
            this.raElementID=-1;
        }
        
       
    }
	
	@SuppressWarnings("unchecked")
    public void execute() {
	    if (this.raElementID == null || this.raElementID == -1)
	        return;
	    
	    IBaseDao dao = getDaoFactory().getDAO(FinishedRiskAnalysis.TYPE_ID);
	    riskAnalysis = (FinishedRiskAnalysis) dao.findById(this.raElementID);
	    if (riskAnalysis == null)
	        return;
	    
	    FindRiskAnalysisListsByParentID command = new FindRiskAnalysisListsByParentID(raElementID);
	    try {
	        command = getCommandService().executeCommand(command);
        } catch (CommandException e) {
            throw new RuntimeCommandException(e);
        }
        if (command.getFoundLists() == null) {
            return;
        }

        lists = command.getFoundLists();
        // hydrate collections:
        lists.getAllGefaehrdungsUmsetzungen();
        lists.getAssociatedGefaehrdungen();
        lists.getNotOKGefaehrdungsUmsetzungen();

        
        // load zielobjekt:
        List<String> row = new ArrayList<String>();
        CnATreeElement elmt = (CnATreeElement) this.riskAnalysis.getParent();
        
        // this is necessary to get the correct type of object instead of a hibernate proxy:
        CnATypeMapper cnATypeMapper = new CnATypeMapper();
        if (cnATypeMapper.isStrukturElement(elmt) ) {
            IBaseDao dao2 = getDaoFactory().getDAO(elmt.getTypeId());
            IBSIStrukturElement realelmt = (IBSIStrukturElement) dao2.findById(elmt.getDbId());
            row.add(realelmt.getKuerzel());
        }
        
        row.add(elmt.getTitle());
        SchutzbedarfAdapter adapter = new SchutzbedarfAdapter(elmt);
        row.add(Integer.toString(adapter.getVertraulichkeit()));
        row.add(Integer.toString(adapter.getIntegritaet()));
        row.add(Integer.toString(adapter.getVerfuegbarkeit()));
        this.zielobjektResult.add(row);
	 
	}
	
	public List<GefaehrdungsUmsetzung> getAllGefaehrdungsUmsetzungen() {
	    if (lists == null)
	        return new ArrayList<GefaehrdungsUmsetzung>(0);
	    
	    return lists.getAllGefaehrdungsUmsetzungen();
	}
	
	public List<GefaehrdungsUmsetzung> getAssociatedGefaehrdungen() {
	    if (lists == null)
	        return new ArrayList<GefaehrdungsUmsetzung>(0);
	    return lists.getAssociatedGefaehrdungen();
	}

	public List<GefaehrdungsUmsetzung> getNotOKGefaehrdungsUmsetzungen() {
	    if (lists == null)
	        return new ArrayList<GefaehrdungsUmsetzung>(0);
	    return lists.getNotOKGefaehrdungsUmsetzungen();
	}
	
	public static final String[] ZIELOBJEKT_COLUMNS = new String[] {
	    "abbrev",
	    "title",
	    "confid",
	    "integr",
	    "avail"
	};
	
	public List<List<String>>  getZielObjekt() {
	   return zielobjektResult;
	}
	
  
   
}
