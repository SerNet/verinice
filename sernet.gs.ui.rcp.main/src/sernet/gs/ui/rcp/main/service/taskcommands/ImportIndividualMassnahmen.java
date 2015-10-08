/*******************************************************************************
 * Copyright (c) 2015 Sebastian Hagedorn.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sebastian Hagedorn <sh[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.taskcommands;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import sernet.gs.model.Baustein;
import sernet.gs.model.Massnahme;
import sernet.gs.reveng.ModZobjBstMass;
import sernet.gs.reveng.importData.BausteineMassnahmenResult;
import sernet.gs.reveng.importData.MassnahmeInformationTransfer;
import sernet.gs.ui.rcp.main.bsi.model.GSScraperUtil;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.service.gstoolimport.MassnahmenFactory;

/**
 * Command imports the controls tagged with >I< from the GSTOOL db, which do not exists as children of the parent baustein in the itgs catalogue
 * @author Sebastian Hagedorn <sh[at]sernet[dot]de>
 * 
 */
@SuppressWarnings("serial")
public class ImportIndividualMassnahmen extends GenericCommand {
    
    private static final Logger LOG =  Logger.getLogger(ImportIndividualMassnahmen.class);

    private Map<BausteinUmsetzung, List<BausteineMassnahmenResult>> individualMassnahmenMap;
    private Map<ModZobjBstMass, MassnahmenUmsetzung> alleMassnahmenMap;
    private List<Baustein> allCatalogueBausteine; 
    private Map<BausteineMassnahmenResult, MassnahmeInformationTransfer> massnahmenInfos;
    private Set<BausteinUmsetzung> changedElements;
    
    
    public ImportIndividualMassnahmen(Map<BausteinUmsetzung, List<BausteineMassnahmenResult>> individualBausteinMassnahmenResultMap,
            Map<ModZobjBstMass, MassnahmenUmsetzung> alleMassnahmenMap, List<Baustein> allCatalogueBausteine,
            Map<BausteineMassnahmenResult, MassnahmeInformationTransfer> massnahmenInfos){ 
        this.individualMassnahmenMap = individualBausteinMassnahmenResultMap;
        this.alleMassnahmenMap = alleMassnahmenMap; 
        this.allCatalogueBausteine = allCatalogueBausteine;
        this.massnahmenInfos = massnahmenInfos;
        this.changedElements = new HashSet<BausteinUmsetzung>();
    }
    
    
    @Override
    public void execute() {
        for(BausteinUmsetzung bausteinUmsetzung : individualMassnahmenMap.keySet()){
            if(individualMassnahmenMap.get(bausteinUmsetzung).size() > 0){
                createIndividualMassnahmenForBausteinUmsetzung(bausteinUmsetzung);
            }
        }
    }
    
    /**
     * @param bausteinUmsetzung
     * @param bausteineMassnahmenResult
     */
    private void createIndividualMassnahmenForBausteinMassnahmenResult(BausteinUmsetzung bausteinUmsetzung, BausteineMassnahmenResult bausteineMassnahmenResult) {
        MassnahmenUmsetzung individualMassnahmenUmsetzung = getIndividualMassnahmenUmsetzungFromAlreadyParsedElements(bausteineMassnahmenResult, bausteinUmsetzung);
        if(individualMassnahmenUmsetzung == null){
            Massnahme m = findCatalogMassnahmeByURL(bausteineMassnahmenResult.massnahme.getLink());
            MassnahmenFactory massnahmenFactory = new MassnahmenFactory();
            if(m == null){
                m = getIndividualMassnahmeFromGstoolDb(bausteineMassnahmenResult, m);

            }
            if(m != null){
                individualMassnahmenUmsetzung = massnahmenFactory.createMassnahmenUmsetzung(bausteinUmsetzung, m, GSScraperUtil.getInstance().getModel().getLanguage());
                individualMassnahmenUmsetzung = massnahmenFactory.transferUmsetzungWithDate(individualMassnahmenUmsetzung, bausteineMassnahmenResult.umstxt.getName(), bausteineMassnahmenResult.obm.getUmsDatBis());
                individualMassnahmenUmsetzung = massnahmenFactory.transferRevision(individualMassnahmenUmsetzung, bausteineMassnahmenResult.obm.getRevDat(), bausteineMassnahmenResult.obm.getRevDatNext(), bausteineMassnahmenResult.obm.getRevBeschr());
                changedElements.add(bausteinUmsetzung);
            } else {
                LOG.warn("Massnahme not found for massnahme:\t" + bausteineMassnahmenResult.massnahme.getLink());
            }
        } else {
            changedElements.add(bausteinUmsetzung);
        }
    }
    
    private void createIndividualMassnahmenForBausteinUmsetzung(BausteinUmsetzung bausteinUmsetzung) {
        if(LOG.isDebugEnabled()){
            LOG.debug("creating " + individualMassnahmenMap.get(bausteinUmsetzung).size() + " individual massnahmen for bausteinUmsetzung:\t" + bausteinUmsetzung.getTitle() + "<" + bausteinUmsetzung.getUuid() + ">" );
        }
        List<BausteineMassnahmenResult> bausteineMassnahmenResultList = individualMassnahmenMap.get(bausteinUmsetzung);
        for(BausteineMassnahmenResult bausteineMassnahmenResult : bausteineMassnahmenResultList){
            createIndividualMassnahmenForBausteinMassnahmenResult(bausteinUmsetzung, bausteineMassnahmenResult);
        }
    }
    
    private MassnahmenUmsetzung getIndividualMassnahmenUmsetzungFromAlreadyParsedElements(BausteineMassnahmenResult bausteinMassnahmenResult, BausteinUmsetzung parent){
        MassnahmenUmsetzung existingSourceMassnahmenUmsetzung = getMassnahmeFromAlleMassnahmenMap(bausteinMassnahmenResult);
        MassnahmenUmsetzung individualMassnahmenUmsetzung = null;
        if(existingSourceMassnahmenUmsetzung != null){
            individualMassnahmenUmsetzung = new MassnahmenUmsetzung(parent);
            individualMassnahmenUmsetzung.getEntity().copyEntity(existingSourceMassnahmenUmsetzung.getEntity());
            individualMassnahmenUmsetzung.setTitel("bM " + individualMassnahmenUmsetzung.getTitle());
        } 
        return individualMassnahmenUmsetzung;
    }
    
    private MassnahmenUmsetzung getMassnahmeFromAlleMassnahmenMap(BausteineMassnahmenResult bausteineMassnahmenResult){
        ModZobjBstMass modZobjBstMass = bausteineMassnahmenResult.obm;
        for(ModZobjBstMass key : alleMassnahmenMap.keySet()){
            if(key.getId().getZobId().equals(modZobjBstMass.getId().getZobId())){
                if(key.getId().getBauId().equals(modZobjBstMass.getId().getBauId())){
                    if(key.getId().getMasId().equals(modZobjBstMass.getId().getMasId())){
                        return alleMassnahmenMap.get(key);
                    }
                }
            }
        }
        return null;
    }
    
    private Massnahme findCatalogMassnahmeByURL(String url){
        if(url.contains("\\")){
            url = url.substring(url.lastIndexOf("\\")+1);
        }
        if(url.contains(".")){
            url = url.substring(0, url.lastIndexOf("."));
        }
        for(Baustein b : allCatalogueBausteine){
            for(Massnahme m : b.getMassnahmen()){
                if(url.equals(m.getUrl())){
                    return m;
                }
            }
        }
        return null;
    }
    
    /**
     * @param bausteineMassnahmenResult
     * @param m
     * @return
     */
    private Massnahme getIndividualMassnahmeFromGstoolDb(BausteineMassnahmenResult bausteineMassnahmenResult, Massnahme m) {
        if(massnahmenInfos.containsKey(bausteineMassnahmenResult)){
            MassnahmeInformationTransfer massnahmeInformationTransfer = massnahmenInfos.get(bausteineMassnahmenResult);
            if(StringUtils.isNotEmpty(massnahmeInformationTransfer.getTitel())){
                m = new Massnahme();
                m.setLebenszyklus(bausteineMassnahmenResult.obm.getZykId());
                m.setId(massnahmeInformationTransfer.getId());
                m.setTitel((massnahmeInformationTransfer.getTitel() != null) ? massnahmeInformationTransfer.getTitel() : "no name available");
                m.setLebenszyklus((massnahmeInformationTransfer.getZyklus() != null) ? Integer.valueOf(massnahmeInformationTransfer.getZyklus()) : -1);
            }
        }

        return m;
    }
    
    public Set<BausteinUmsetzung> getChangedElements(){
        if(LOG.isDebugEnabled()){
            LOG.debug("Added individual controls to " + changedElements.size() + " itgs modules");
        }
        return changedElements;
    }

}
