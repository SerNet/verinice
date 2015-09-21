/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.taskcommands;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.model.Baustein;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.model.Massnahme;
import sernet.gs.reveng.MbBaust;
import sernet.gs.reveng.MbMassn;
import sernet.gs.reveng.MbZeiteinheitenTxt;
import sernet.gs.reveng.ModZobjBst;
import sernet.gs.reveng.ModZobjBstMass;
import sernet.gs.reveng.importData.BausteinInformationTransfer;
import sernet.gs.reveng.importData.BausteineMassnahmenResult;
import sernet.gs.reveng.importData.GefaehrdungInformationTransfer;
import sernet.gs.reveng.importData.MassnahmeInformationTransfer;
import sernet.gs.scraper.GSScraper;
import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.RuntimeCommandException;
import sernet.gs.ui.rcp.gsimport.ImportKostenUtil;
import sernet.gs.ui.rcp.gsimport.TransferData;
import sernet.gs.ui.rcp.main.bsi.model.BSIMassnahmenModel;
import sernet.gs.ui.rcp.main.bsi.model.GSScraperUtil;
import sernet.gs.ui.rcp.main.bsi.model.IBSIConfig;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzungFactory;
import sernet.gs.ui.rcp.main.common.model.CnATreeElementBuildException;
import sernet.gs.ui.rcp.main.common.model.IProgress;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.CreateBaustein;
import sernet.gs.ui.rcp.main.service.grundschutzparser.LoadBausteine;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.OwnGefaehrdung;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.LoadAncestors;

/**
 * Create BausteinUmsetzung objects during import for given target object and
 * assigned Bausteine from source database.
 * 
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
public class ImportCreateBausteine extends GenericCommand {

    private transient Logger log = Logger.getLogger(ImportCreateBausteine.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(ImportCreateBausteine.class);
        }
        return log;
    }

    private CnATreeElement element;
    private Map<MbBaust, List<BausteineMassnahmenResult>> bausteineMassnahmenMap;
    private Map<MbBaust, BausteinInformationTransfer> udBausteineTxtMap;
    private Map<MbMassn, MassnahmeInformationTransfer> udBstMassTxtMap;
    private Map<MbBaust, List<GefaehrdungInformationTransfer>> udBaustGefMap;
    private boolean importUmsetzung;
    private boolean kosten;
    private Map<MbBaust, BausteinUmsetzung> alleBausteineToBausteinUmsetzungMap;
    private Map<MbBaust, ModZobjBst> alleBausteineToZoBstMap;
    
    private List<MbZeiteinheitenTxt> zeiten;
    private Map<ModZobjBstMass, MassnahmenUmsetzung> alleMassnahmen;
    private List<Baustein> bausteine;
    private String sourceId;
    private IBSIConfig bsiConfig;

    private static final short BST_BEARBEITET_ENTBEHRLICH = 3;

  
    public ImportCreateBausteine(String sourceId, CnATreeElement element, 
            Map<MbBaust, List<BausteineMassnahmenResult>> bausteineMassnahmenMap,
            List<MbZeiteinheitenTxt> zeiten, boolean kosten, boolean importUmsetzung,
            IBSIConfig bsiConfig, Map<MbBaust, BausteinInformationTransfer> udBstTxtMap, Map<MbMassn, MassnahmeInformationTransfer> udBstMassTxtMap,
            Map<MbBaust, List<GefaehrdungInformationTransfer>> udBaustGefMap) {
        this.element = element;
        this.bausteineMassnahmenMap = bausteineMassnahmenMap;
        this.kosten = kosten;
        this.importUmsetzung = importUmsetzung;
        this.zeiten = zeiten;
        this.sourceId = sourceId;
        this.bsiConfig = bsiConfig;
        this.udBausteineTxtMap = udBstTxtMap;
        this.udBstMassTxtMap = udBstMassTxtMap;
        this.udBaustGefMap = udBaustGefMap; 
    }

    public ImportCreateBausteine(String sourceId, CnATreeElement element, 
            Map<MbBaust, List<BausteineMassnahmenResult>> bausteineMassnahmenMap, List<MbZeiteinheitenTxt> zeiten, 
            boolean kosten, boolean importUmsetzung, Map<MbBaust, BausteinInformationTransfer> udBstTxtMap, 
            Map<MbMassn, MassnahmeInformationTransfer> udBstMassTxtMap, Map<MbBaust, List<GefaehrdungInformationTransfer>> udBaustGefMap) {
        this.element = element;
        this.bausteineMassnahmenMap = bausteineMassnahmenMap;
        this.kosten = kosten;
        this.importUmsetzung = importUmsetzung;
        this.zeiten = zeiten;
        this.sourceId = sourceId;
        this.udBausteineTxtMap = udBstTxtMap;
        this.udBstMassTxtMap = udBstMassTxtMap;
        this.udBaustGefMap = udBaustGefMap;
    }
    
    

    @Override
    public void execute() {
        try {
            if (getLog().isDebugEnabled()) {
                getLog().debug("Reloading element " + element.getTitle());
            }
            IBaseDao<Object, Serializable> dao = getDaoFactory().getDAOforTypedElement(element);
            element = (CnATreeElement) dao.findById(element.getDbId());

            if (this.bsiConfig == null) {
                // load bausteine from default config:
                LoadBausteine command = new LoadBausteine();
                command = ServiceFactory.lookupCommandService().executeCommand(command);
                this.bausteine = command.getBausteine();
            } else {
                // load bausteine from given config:
                BSIMassnahmenModel model = GSScraperUtil.getInstance().getModel();
                model.setBSIConfig(bsiConfig);
                this.bausteine = model.loadBausteine(new IProgress() {

                    @Override
                    public void beginTask(String name, int totalWork) {
                    }

                    @Override
                    public void done() {
                    }

                    @Override
                    public void setTaskName(String string) {
                    }

                    @Override
                    public void subTask(String string) {
                    }

                    @Override
                    public void worked(int work) {
                    }
                });
            }

            Set<MbBaust> keySet = bausteineMassnahmenMap.keySet();
            for (MbBaust mbBaust : keySet) {
                createBaustein(element, mbBaust, bausteineMassnahmenMap.get(mbBaust));
            }
            dao.flush();

        } catch (Exception e) {
            getLog().error("Error while importing: ", e);
            throw new RuntimeCommandException(e);
        }

    }

    private BausteinUmsetzung createBaustein(CnATreeElement element, MbBaust mbBaust, List<BausteineMassnahmenResult> list) throws Exception {
        Baustein baustein = findBausteinForId(TransferData.getId(mbBaust));
        // TODO AK if none found it ma be ben.def. baustein
        // check here, if user defined baustn is existant, if thats the case, skip bstn from catalogue
        Integer refZobId = null;
        isReference: for (BausteineMassnahmenResult bausteineMassnahmenResult : list) {
            refZobId = bausteineMassnahmenResult.zoBst.getRefZobId();
            if (refZobId != null) {
                break isReference;
            }
        }

        if (baustein != null && refZobId == null) { // if baustein != null, baustein is found in bsi catalogue
            // BSIKatalogInvisibleRoot.getInstance().getLanguage() caused a classNotFound Exception here, fixed 
            // but import now only works for German.
            // this should be loaded from BSIMassnahmenModel which is the ITGS main model class

            // skipping user defined bausteine
            if(mbBaust.getId().getBauImpId() != 1){ // should always be != 1
                CreateBaustein command = new CreateBaustein(element, baustein, GSScraper.CATALOG_LANGUAGE_GERMAN);
                command = ServiceFactory.lookupCommandService().executeCommand(command);
                BausteinUmsetzung bausteinUmsetzung = command.getNewElement();

                if(bausteinUmsetzung != null){
                    if (list.iterator().hasNext()) {
                        BausteineMassnahmenResult queryresult = list.iterator().next();
                        transferBaustein(baustein, bausteinUmsetzung, queryresult);
                        transferMassnahmen(bausteinUmsetzung, list, false);
                    }
                }
                return bausteinUmsetzung;
            } else if(mbBaust.getId().getBauImpId() == 1) { // user defined but in catalogue existant
                // import as userdefined
            }
        } else { // baustein is null if mbBaust.getId().getBauImpId() == 1, baustein not found in catalogue, lets assume its userdefined
            if(mbBaust.getId().getBauImpId() == 1 && mbBaust.getNrNum() == null){ // NrNum != null is RA 
                return createBstUms(element, mbBaust, list, importUserDefinedBaustein(mbBaust, list));
            } 
        }
        return null;
    }

    /**
     * @param element
     * @param mbBaust
     * @param list
     * @param baustein
     * @return
     * @throws CommandException
     * @throws CnATreeElementBuildException
     * @throws SQLException
     * @throws IOException
     */
    private BausteinUmsetzung createBstUms(CnATreeElement element, MbBaust mbBaust, List<BausteineMassnahmenResult> list, Baustein baustein) throws CommandException, CnATreeElementBuildException, SQLException, IOException {
        CreateBaustein command = new CreateBaustein(element, baustein, GSScraper.CATALOG_LANGUAGE_GERMAN);
        command = ServiceFactory.lookupCommandService().executeCommand(command);
        BausteinUmsetzung bausteinUmsetzung = command.getNewElement();
        if(bausteinUmsetzung != null){
            if (list.iterator().hasNext()) {
                BausteinInformationTransfer bit = udBausteineTxtMap.get(mbBaust);
                transferUserDefinedBaustein(baustein, bit, bausteinUmsetzung);
                transferMassnahmen(bausteinUmsetzung, list, true);
                transferGefForUDBst(baustein, bausteinUmsetzung);
            }
        }
        return bausteinUmsetzung;
    }

    /**
     * @param baustein
     * @param bausteinUmsetzung
     * @throws SQLException
     * @throws IOException
     * @throws CommandException
     */
    @SuppressWarnings("unchecked")
    private void transferGefForUDBst(Baustein baustein, BausteinUmsetzung bausteinUmsetzung) throws SQLException, IOException, CommandException {
        for(Gefaehrdung g : baustein.getGefaehrdungen()){
            getDaoFactory().getDAO(GefaehrdungsUmsetzung.TYPE_ID).saveOrUpdate(createGefaehrdung((OwnGefaehrdung)g, bausteinUmsetzung));
        }
    }
    
    
    
    private Baustein importUserDefinedBaustein(MbBaust mbBaust, List<BausteineMassnahmenResult> list) throws CommandException{
        Baustein baustein = createBasicBaustein(mbBaust);
        baustein = createMassnForBst(list, baustein);
        baustein = createGefForBst(mbBaust, baustein);
        return baustein;
    }
    
    private Baustein createBasicBaustein(MbBaust mbBaust){
        Baustein baustein = new Baustein();
        BausteinInformationTransfer bInfo = udBausteineTxtMap.get(mbBaust);
        baustein.setEncoding((bInfo.getEncoding() != null) ? bInfo.getEncoding() : "UTF-8");
        baustein.setId(mbBaust.getNr());
        baustein.setSchicht((bInfo.getSchicht() != null) ? Integer.valueOf(bInfo.getSchicht()) : -1);
//        baustein.setStand(stand); // TODO
        baustein.setTitel((bInfo != null) ? bInfo.getTitel() : "no name available");
        return baustein;
    }

    /**
     * @param list
     * @param baustein
     */
    private Baustein createMassnForBst(List<BausteineMassnahmenResult> list, Baustein baustein) {
        Iterator<BausteineMassnahmenResult> iter = list.iterator();
        List<Massnahme> massnahmen = new ArrayList<Massnahme>();
        while(iter.hasNext()){
            BausteineMassnahmenResult bmr = iter.next();
            Massnahme m = new Massnahme();
            m.setLebenszyklus(bmr.obm.getZykId());
            
            MassnahmeInformationTransfer mTxt = udBstMassTxtMap.get(bmr.massnahme);
            m.setId(mTxt.getId());
            if(mTxt != null){
                m.setTitel((mTxt.getTitel() != null) ? mTxt.getTitel() : "no name available");
                m.setLebenszyklus((mTxt.getZyklus() != null) ? Integer.valueOf(mTxt.getZyklus()) : -1);
//                char siegel = (mTxt.getSiegelstufe() != '') ? String.valueOf(mTxt.getSiegelstufe()) : "";
//                m.setSiegelstufe(siegel);
            }
            massnahmen.add(m);
//            getLog().error(bmr.massnahme.getErfasstDurch());
//            getLog().error(bmr.massnahme.getId());
//            getLog().error(bmr.massnahme.getLink());
//            getLog().error(bmr.massnahme.getNr());
//            getLog().error(bmr.obm.getUmsDatVon());
//            getLog().error(bmr.obm.getUmsDatBis());
//            getLog().error(bmr.obm.getUmsBeschr());
//            getLog().error(bmr.obm.getErfasstDurch());
//            getLog().error(bmr.obm.getKostPersFix());
//            getLog().error(bmr.obm.getKostPersVar());
//            getLog().error(bmr.obm.getKostSachFix());
//            getLog().error(bmr.obm.getKostSachVar());
//            getLog().error(bmr.obm.getKostPersZeiId().intValue());
//            getLog().error(bmr.obm.getKostSachZeiId().intValue());
//            getLog().error(bmr.obm.getRevBeschr());
//            getLog().error(bmr.obm.getRevDat());
//            getLog().error(bmr.obm.getRevDatNext());
//            getLog().error(bmr.obm.getZykId());
//            getLog().error(bmr.massnahme.getLink());
            
        }
        baustein.setMassnahmen(massnahmen);
        return baustein;
    }

    /**
     * @param mbBaust
     * @param baustein
     */
    private Baustein createGefForBst(MbBaust mbBaust, Baustein baustein) {
        if(udBaustGefMap.containsKey(mbBaust)){
            List<Gefaehrdung> gefs = new ArrayList<Gefaehrdung>();
            for(GefaehrdungInformationTransfer git : udBaustGefMap.get(mbBaust)){
                if(git.getTitel() != null){
                    OwnGefaehrdung g = new OwnGefaehrdung();
                    g.setEncoding(GSScraperUtil.getInstance().getModel().getEncoding());
                    g.setId(git.getId());
                    g.setKategorie(git.getKategorie());
                    g.setStand(git.getStand());
                    g.setTitel(git.getTitel());
                    g.setBeschreibung(git.getDescription());
                    gefs.add(g);
                }
            }
            baustein.setGefaehrdungen(gefs);
        } else {
            baustein.setGefaehrdungen(Collections.EMPTY_LIST);
        }
        return baustein;
    }
    
    /**
     * @return
     */
    private String createExtId(Baustein baustein, Integer refZobId) {
        return baustein.getId() + "-" + Integer.toString(refZobId);
    }

    private Baustein findBausteinForId(String id) {
        for (Baustein baustein : bausteine) {
            if (baustein.getId().equals(id)) {
                return baustein;
            }
        }
        return null;
    }

    private void transferUserDefinedBaustein(Baustein baustein, BausteinInformationTransfer bit, BausteinUmsetzung bstUms ){
        if(bit != null && baustein != null && bstUms != null){
            bstUms.setSimpleProperty(BausteinUmsetzung.P_ERLAEUTERUNG, bit.getDescription());
            bstUms.setSimpleProperty(BausteinUmsetzung.P_BAUSTEIN_BESCHREIBUNG, bit.getDescription());
            bstUms.setSimpleProperty(BausteinUmsetzung.P_ERFASSTAM, parseDate(bit.getErfasstAm()));
            bstUms.setSimpleProperty(BausteinUmsetzung.P_NR, bit.getNr());
        }
        
        if(bstUms == null){
            getLog().error("Bausteinumsetzung für " + baustein.getTitel() + " war null");
        }
        
        if(bstUms != null){
            bstUms.setSourceId(sourceId);
            bstUms.setExtId(createExtId(baustein, bit.getZobId()));
            if (getLog().isDebugEnabled()) {
                getLog().debug("Creating baustein with sourceId and extId: " + sourceId + ", " + bstUms.getExtId());
            }
        }
        
        // remember baustein for later:
        if (alleBausteineToBausteinUmsetzungMap == null) {
            alleBausteineToBausteinUmsetzungMap = new HashMap<MbBaust, BausteinUmsetzung>();
        }

        if (alleBausteineToZoBstMap == null) {
            alleBausteineToZoBstMap = new HashMap<MbBaust, ModZobjBst>();
        }
        alleBausteineToBausteinUmsetzungMap.put(bit.getMzb().getMbBaust(), bstUms);
        alleBausteineToZoBstMap.put(bit.getBaust(), bit.getMzb());
        
    }
    
    
    private void transferBaustein(Baustein baustein, BausteinUmsetzung bausteinUmsetzung, BausteineMassnahmenResult vorlage) {
        if(bausteinUmsetzung != null && vorlage != null && vorlage.zoBst != null && vorlage.zoBst.getBegruendung() != null){

            bausteinUmsetzung.setSimpleProperty(BausteinUmsetzung.P_ERLAEUTERUNG, vorlage.zoBst.getBegruendung());
            bausteinUmsetzung.setSimpleProperty(BausteinUmsetzung.P_ERFASSTAM, parseDate(vorlage.zoBst.getDatum()));
            
        }
        
        if(bausteinUmsetzung == null){
            getLog().error("Bausteinumsetzung für " + baustein.getTitel() + " war null");
        }

        // set zobID as extId to find baustein references linking to it later
        // on:
        if(bausteinUmsetzung != null){
            bausteinUmsetzung.setSourceId(sourceId);
            bausteinUmsetzung.setExtId(createExtId(baustein, vorlage.obm.getId().getZobId()));
            if (getLog().isDebugEnabled()) {
                getLog().debug("Creating baustein with sourceId and extId: " + sourceId + ", " + bausteinUmsetzung.getExtId());
            }
        }

        // remember baustein for later:
        if (alleBausteineToBausteinUmsetzungMap == null) {
            alleBausteineToBausteinUmsetzungMap = new HashMap<MbBaust, BausteinUmsetzung>();
        }

        if (alleBausteineToZoBstMap == null) {
            alleBausteineToZoBstMap = new HashMap<MbBaust, ModZobjBst>();
        }

        alleBausteineToBausteinUmsetzungMap.put(vorlage.baustein, bausteinUmsetzung);
        alleBausteineToZoBstMap.put(vorlage.baustein, vorlage.zoBst);
    }

    private String parseDate(Date date) {
        if (date != null) {
            return Long.toString(date.getTime());
        }
        return "";
    }

    private void transferUserDefinedMassnahmen(){
        
        MassnahmenUmsetzung mNums = new MassnahmenUmsetzung();
//        String massnahmeNr = "bM " + mBMass.getMskId() + "." + mBMass.getNr();
        
//        mNums.setSimpleProperty("mnums_id", massnahmeNr);
//        mnUms.setName(ragmResult.getMassnahmeTxt().getName());
//        mnUms.setDescription(convertClobToStringEncodingSave(ragmResult.getMassnahmeTxt().getBeschreibung(), GSScraperUtil.getInstance().getModel().getEncoding()));
//        mnUms.setErlaeuterung(ragmResult.getMzbm().getUmsBeschr());
//        mnUms.setSimpleProperty(MassnahmenUmsetzung.P_UMSETZUNGBIS, parseDate(ragmResult.getMzbm().getUmsDatBis()));
//        mnUms.setSimpleProperty(MassnahmenUmsetzung.P_LETZTEREVISIONAM, parseDate(ragmResult.getMzbm().getRevDat()));
//        mnUms.setSimpleProperty(MassnahmenUmsetzung.P_NAECHSTEREVISIONAM, parseDate(ragmResult.getMzbm().getRevDatNext()));
//        mnUms.setRevisionBemerkungen(ragmResult.getMzbm().getRevBeschr());
//        mnUms.setUrl(transferUrl(ragmResult.getMassnahme().getLink()));
//        char siegel = convertToChar(ragmResult.getSiegelTxt().getKurzname());
//        if(siegel!=KEIN_SIEGEL) {
//            mnUms.setStufe(siegel);
//        }
//        transferUmsetzung(mnUms, ragmResult.getUmsTxt().getName());
        
        
    }


    private void transferMassnahmen(BausteinUmsetzung bausteinUmsetzung, List<BausteineMassnahmenResult> list, boolean isUserDefinedBaustein) throws CommandException, CnATreeElementBuildException  {
        List<MassnahmenUmsetzung> massnahmenUmsetzungen = null;
        if(bausteinUmsetzung != null){
            massnahmenUmsetzungen = bausteinUmsetzung.getMassnahmenUmsetzungen();
        } else {
            massnahmenUmsetzungen = Collections.EMPTY_LIST;
        }
        for (MassnahmenUmsetzung massnahmenUmsetzung : massnahmenUmsetzungen) {
            BausteineMassnahmenResult vorlage = null;
                    if(!isUserDefinedBaustein){
                        vorlage = TransferData.findMassnahmenVorlageBaustein(massnahmenUmsetzung, list);
                    } else {
                        vorlage = list.get(0);
                    }
            if(vorlage != null){
                createMassnahmenForBaustein(list, massnahmenUmsetzung, vorlage);
            }
        }

    }

    /**
     * @param list
     * @param massnahmenUmsetzung
     * @param vorlage
     */
    private void createMassnahmenForBaustein(List<BausteineMassnahmenResult> list, MassnahmenUmsetzung massnahmenUmsetzung, BausteineMassnahmenResult vorlage) {
        if (vorlage != null){
            if (importUmsetzung) {
                transferMassnahmeUmsetzungsStatus(massnahmenUmsetzung, vorlage);
            }

            // copy fields:
            transferMassnahme(massnahmenUmsetzung, vorlage);

        } else {
            // wenn diese massnahme unbearbeitet ist und keine vorlage
            // existiert,
            // kann trotzdem der gesamte baustein auf entbehrlich gesetzt
            // sein:
            if (importUmsetzung && list.iterator().hasNext()) {
                BausteineMassnahmenResult result = list.iterator().next();
                if (result.zoBst.getBearbeitetOrg() == BST_BEARBEITET_ENTBEHRLICH) {
                    massnahmenUmsetzung.setUmsetzung(MassnahmenUmsetzung.P_UMSETZUNG_ENTBEHRLICH);
                }
            }
        }
    }

    /**
     * @param massnahmenUmsetzung
     * @param vorlage
     */
    private void transferMassnahmeUmsetzungsStatus(MassnahmenUmsetzung massnahmenUmsetzung, BausteineMassnahmenResult vorlage) {
        // copy umsetzung:
        Short bearbeitet = vorlage.zoBst.getBearbeitetOrg();
        if (bearbeitet == BST_BEARBEITET_ENTBEHRLICH) {
            massnahmenUmsetzung.setUmsetzung(MassnahmenUmsetzung.P_UMSETZUNG_ENTBEHRLICH);
        } else {
            TransferData.transferUmsetzung(massnahmenUmsetzung, vorlage.umstxt.getName());
        }
    }

    private void transferMassnahme(MassnahmenUmsetzung massnahmenUmsetzung, BausteineMassnahmenResult vorlage) {
        if (importUmsetzung) {
            // erlaeuterung und termin:
            massnahmenUmsetzung.setSimpleProperty(MassnahmenUmsetzung.P_ERLAEUTERUNG, vorlage.obm.getUmsBeschr());
            massnahmenUmsetzung.setSimpleProperty(MassnahmenUmsetzung.P_UMSETZUNGBIS, parseDate(vorlage.obm.getUmsDatBis()));
            massnahmenUmsetzung.setSimpleProperty(MassnahmenUmsetzung.P_LETZTEREVISIONAM, parseDate(vorlage.obm.getRevDat()));
            massnahmenUmsetzung.setSimpleProperty(MassnahmenUmsetzung.P_NAECHSTEREVISIONAM, parseDate(vorlage.obm.getRevDatNext()));
            massnahmenUmsetzung.setRevisionBemerkungen(vorlage.obm.getRevBeschr());         
        }

        // transfer kosten:
        if (kosten) {
            ImportKostenUtil.importKosten(massnahmenUmsetzung, vorlage, zeiten);
        }

        // remember massnahme for later:
        if (alleMassnahmen == null) {
            alleMassnahmen = new HashMap<ModZobjBstMass, MassnahmenUmsetzung>();
        }
        alleMassnahmen.put(vorlage.obm, massnahmenUmsetzung);
    }
    
    private GefaehrdungsUmsetzung createGefaehrdung(OwnGefaehrdung oGef, BausteinUmsetzung bstUms) throws SQLException, IOException, CommandException {
        
        GefaehrdungsUmsetzung gefUms = GefaehrdungsUmsetzungFactory.build(bstUms, oGef, GSScraper.CATALOG_LANGUAGE_GERMAN);
               
        return gefUms;
    }

    public Map<MbBaust, BausteinUmsetzung> getAlleBausteineToBausteinUmsetzungMap() {
        return alleBausteineToBausteinUmsetzungMap;
    }

    public Map<MbBaust, ModZobjBst> getAlleBausteineToZoBstMap() {
        return alleBausteineToZoBstMap;
    }

    public Map<ModZobjBstMass, MassnahmenUmsetzung> getAlleMassnahmen() {
        return alleMassnahmen;
    }

    @Override
    public void clear() {
        // empty elements for transfer to client:
        element = null;
        bausteineMassnahmenMap = null;
        zeiten = null;
        bausteine = null;
    }
    
    private String getElementPath(String uuid, String typeId){
        RetrieveInfo ri = RetrieveInfo.getPropertyInstance();

        LoadAncestors command = new LoadAncestors(typeId, uuid, ri);
        try {
            command = ServiceFactory.lookupCommandService().executeCommand(command);
        } catch (CommandException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        CnATreeElement current = command.getElement();

        // build object path
        StringBuilder sb = new StringBuilder();
        sb.insert(0, current.getTitle());

        while (current.getParent() != null) {
            current = current.getParent();
            sb.insert(0, "/");
            sb.insert(0, current.getTitle());
        }



        // crop the root element, which is always ISO .. or BSI ...
        String[] p = sb.toString().split("/");
        sb = new StringBuilder();
        for (int i = 1; i < p.length; i++) {
            sb.append("/").append(p[i]);
        }
        return sb.toString();
    }

}
