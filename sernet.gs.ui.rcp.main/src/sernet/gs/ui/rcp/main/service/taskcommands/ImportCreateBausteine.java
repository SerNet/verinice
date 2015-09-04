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

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.model.Baustein;
import sernet.gs.reveng.MbBaust;
import sernet.gs.reveng.MbZeiteinheitenTxt;
import sernet.gs.reveng.ModZobjBst;
import sernet.gs.reveng.ModZobjBstMass;
import sernet.gs.reveng.importData.BausteineMassnahmenResult;
import sernet.gs.scraper.GSScraper;
import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.RuntimeCommandException;
import sernet.gs.ui.rcp.gsimport.ImportKostenUtil;
import sernet.gs.ui.rcp.gsimport.TransferData;
import sernet.gs.ui.rcp.main.bsi.model.BSIMassnahmenModel;
import sernet.gs.ui.rcp.main.bsi.model.GSScraperUtil;
import sernet.gs.ui.rcp.main.bsi.model.IBSIConfig;
import sernet.gs.ui.rcp.main.common.model.IProgress;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.CreateBaustein;
import sernet.gs.ui.rcp.main.service.grundschutzparser.LoadBausteine;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
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

  
    public ImportCreateBausteine(String sourceId, CnATreeElement element, Map<MbBaust, List<BausteineMassnahmenResult>> bausteineMassnahmenMap, List<MbZeiteinheitenTxt> zeiten, boolean kosten, boolean importUmsetzung, IBSIConfig bsiConfig) {
        this.element = element;
        this.bausteineMassnahmenMap = bausteineMassnahmenMap;
        this.kosten = kosten;
        this.importUmsetzung = importUmsetzung;
        this.zeiten = zeiten;
        this.sourceId = sourceId;
        this.bsiConfig = bsiConfig;
    }

    public ImportCreateBausteine(String sourceId, CnATreeElement element, Map<MbBaust, List<BausteineMassnahmenResult>> bausteineMassnahmenMap, List<MbZeiteinheitenTxt> zeiten, boolean kosten, boolean importUmsetzung) {
        this.element = element;
        this.bausteineMassnahmenMap = bausteineMassnahmenMap;
        this.kosten = kosten;
        this.importUmsetzung = importUmsetzung;
        this.zeiten = zeiten;
        this.sourceId = sourceId;
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

        if (baustein != null && refZobId == null) {
            // BSIKatalogInvisibleRoot.getInstance().getLanguage() caused a classNotFound Exception here, fixed 
            // but import now only works for German.
            // this should be loaded from BSIMassnahmenModel which is the ITGS main model class
            
            // skipping user defined bausteine
            if(mbBaust.getId().getBauImpId() != 1){
                CreateBaustein command = new CreateBaustein(element, baustein, GSScraper.CATALOG_LANGUAGE_GERMAN);
                command = ServiceFactory.lookupCommandService().executeCommand(command);
                BausteinUmsetzung bausteinUmsetzung = command.getNewElement();

                if(bausteinUmsetzung != null){
                    if (list.iterator().hasNext()) {
                        BausteineMassnahmenResult queryresult = list.iterator().next();
                        transferBaustein(baustein, bausteinUmsetzung, queryresult);
                        transferMassnahmen(bausteinUmsetzung, list);
                    }
                }
                return bausteinUmsetzung;
            } 
            return null;
        } else {
            getLog().error("Baustein with id:\t" + mbBaust.getId().getBauId() + " and nr:\t" + mbBaust.getNr() + " on ZOB:\t " + getElementPath(element.getUuid(), element.getTypeId()) + " is userdefined. userdefined bausteine are not considered from this import");
        }
        // TODO AK else create ben.def. baustein and transfer content from mbBaust to it instead
        return null;
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

   

    private void transferMassnahmen(BausteinUmsetzung bausteinUmsetzung, List<BausteineMassnahmenResult> list) {
        List<MassnahmenUmsetzung> massnahmenUmsetzungen = null;
        if(bausteinUmsetzung != null){
            massnahmenUmsetzungen = bausteinUmsetzung.getMassnahmenUmsetzungen();
        } else {
            massnahmenUmsetzungen = Collections.EMPTY_LIST;
        }
        for (MassnahmenUmsetzung massnahmenUmsetzung : massnahmenUmsetzungen) {
            BausteineMassnahmenResult vorlage = TransferData.findMassnahmenVorlageBaustein(massnahmenUmsetzung, list);
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
