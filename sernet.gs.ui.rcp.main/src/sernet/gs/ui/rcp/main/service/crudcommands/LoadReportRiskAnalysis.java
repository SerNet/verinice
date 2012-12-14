package sernet.gs.ui.rcp.main.service.crudcommands;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.model.bsi.IBSIStrukturElement;
import sernet.verinice.model.bsi.SchutzbedarfAdapter;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysis;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysisLists;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.CnATypeMapper;
import sernet.verinice.service.commands.FindRiskAnalysisListsByParentID;

/**
 * Loads elements for risk analysis report according to BSI 100-3.
 */
public class LoadReportRiskAnalysis extends GenericCommand implements ICachedCommand {

    private Integer raElementID;
    private FinishedRiskAnalysisLists lists = null;
    private FinishedRiskAnalysis riskAnalysis = null;
    private List<List<String>> zielobjektResult = new ArrayList<List<String>>(0);

    private boolean resultInjectedFromCache = false;

    private transient Logger log = Logger.getLogger(LoadReportRiskAnalysis.class);

    private boolean getDbId = false;

    public LoadReportRiskAnalysis(Integer rootElement) {
        this.raElementID = rootElement;
    }

    public LoadReportRiskAnalysis(String rootElement) {
        try {
            this.raElementID = Integer.parseInt(rootElement);
        } catch (Exception e) {
            this.raElementID = -1;
        }

    }

    public LoadReportRiskAnalysis(Integer root, boolean getDbId) {
        this(root);
        this.getDbId = getDbId;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void execute() {
        if (!resultInjectedFromCache) {
            if (this.raElementID == null || this.raElementID == -1) {
                return;
            }

            IBaseDao dao = getDaoFactory().getDAO(FinishedRiskAnalysis.TYPE_ID);
            riskAnalysis = (FinishedRiskAnalysis) dao.findById(this.raElementID);
            if (riskAnalysis == null) {
                return;
            }

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
            CnATreeElement elmt = this.riskAnalysis.getParent();

            // this is necessary to get the correct type of object instead of a
            // hibernate proxy:
            CnATypeMapper cnATypeMapper = new CnATypeMapper();
            if (cnATypeMapper.isStrukturElement(elmt)) {
                IBaseDao dao2 = getDaoFactory().getDAO(elmt.getTypeId());
                IBSIStrukturElement realelmt = (IBSIStrukturElement) dao2.findById(elmt.getDbId());
                row.add(realelmt.getKuerzel());
            }

            row.add(elmt.getTitle());
            SchutzbedarfAdapter adapter = new SchutzbedarfAdapter(elmt);
            row.add(Integer.toString(adapter.getVertraulichkeit()));
            row.add(Integer.toString(adapter.getIntegritaet()));
            row.add(Integer.toString(adapter.getVerfuegbarkeit()));
            if (getDbId) {
                row.add(Integer.toString(elmt.getDbId()));
            }
            this.zielobjektResult.add(row);
        }

    }

    public List<GefaehrdungsUmsetzung> getAllGefaehrdungsUmsetzungen() {
        if (lists == null) {
            return new ArrayList<GefaehrdungsUmsetzung>(0);
        }

        return lists.getAllGefaehrdungsUmsetzungen();
    }

    public List<GefaehrdungsUmsetzung> getAssociatedGefaehrdungen() {
        if (lists == null) {
            return new ArrayList<GefaehrdungsUmsetzung>(0);
        }
        return lists.getAssociatedGefaehrdungen();
    }

    public List<GefaehrdungsUmsetzung> getNotOKGefaehrdungsUmsetzungen() {
        if (lists == null) {
            return new ArrayList<GefaehrdungsUmsetzung>(0);
        }
        return lists.getNotOKGefaehrdungsUmsetzungen();
    }

    public static final String[] ZIELOBJEKT_COLUMNS = new String[] { "abbrev", "title", "confid", "integr", "avail" };

    public List<List<String>> getZielObjekt() {
        return zielobjektResult;
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheID()
     */
    @Override
    public String getCacheID() {
        StringBuilder cacheID = new StringBuilder();
        cacheID.append(this.getClass().getSimpleName());
        cacheID.append(String.valueOf(raElementID));
        return cacheID.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.interfaces.ICachedCommand#injectCacheResult(java.lang
     * .Object)
     */
    @Override
    public void injectCacheResult(Object result) {
        if (result instanceof Object[]) {
            Object[] array = (Object[]) result;
            this.lists = (FinishedRiskAnalysisLists) array[0];
            this.zielobjektResult = (ArrayList<List<String>>) array[1];
            resultInjectedFromCache = true;
            if (getLog().isDebugEnabled()) {
                getLog().debug("Result in " + this.getClass().getCanonicalName() + " injected from cache");
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheableResult()
     */
    @Override
    public Object getCacheableResult() {
        Object[] results = new Object[2];
        results[0] = this.lists;
        results[1] = this.zielobjektResult;
        return results;
    }

    private Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(LoadReportRiskAnalysis.class);
        }
        return log;
    }

}
