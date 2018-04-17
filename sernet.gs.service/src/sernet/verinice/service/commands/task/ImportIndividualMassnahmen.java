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
package sernet.verinice.service.commands.task;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.model.Baustein;
import sernet.gs.model.Massnahme;
import sernet.gs.reveng.ModZobjBstMass;
import sernet.gs.reveng.importData.BausteineMassnahmenResult;
import sernet.gs.reveng.importData.MassnahmeInformationTransfer;
import sernet.gs.service.Retriever;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.service.gstoolimport.MassnahmenFactory;
import sernet.verinice.service.gstoolimport.TransferData;
import sernet.verinice.service.parser.GSScraperUtil;

/**
 * Command imports the controls tagged with >I< from the GSTOOL db, which do not exists as children of the parent baustein in the itgs catalogue
 * @author Sebastian Hagedorn <sh[at]sernet[dot]de>
 *
 */
@SuppressWarnings("serial")
public class ImportIndividualMassnahmen extends GenericCommand {

    private static final Logger LOG =  Logger.getLogger(ImportIndividualMassnahmen.class);
    
    private static final String INDIVIDUAL_CONTROL_IDENTIFIER = "iM ";

    private final Map<BausteinUmsetzung, List<BausteineMassnahmenResult>> individualMassnahmenMap;
    private final Map<ModZobjBstMass, MassnahmenUmsetzung> alleMassnahmenMap;
    private final List<Baustein> allCatalogueBausteine;
    private final Map<String, MassnahmeInformationTransfer> massnahmenInfos;
    private final Set<MassnahmenUmsetzung> changedElements;


    public ImportIndividualMassnahmen(Map<BausteinUmsetzung, List<BausteineMassnahmenResult>> individualBausteinMassnahmenResultMap,
            Map<ModZobjBstMass, MassnahmenUmsetzung> alleMassnahmenMap, List<Baustein> allCatalogueBausteine,
            Map<String, MassnahmeInformationTransfer> massnahmenInfos){
        this.individualMassnahmenMap = individualBausteinMassnahmenResultMap; // bausteinumsetzung, list<bausteinmassnahmeresult>
        this.alleMassnahmenMap = alleMassnahmenMap; // modzobjbstmass, massnahmenumsetzung
        this.allCatalogueBausteine = allCatalogueBausteine; // list baustein
        this.massnahmenInfos = massnahmenInfos; // bausteinmassnahmeresult, massnahmeinformationstransfer
        this.changedElements = new HashSet<>(); // set<bausteinumsetzung>
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
                m = getIndividualMassnahmeFromGstoolDb(TransferData.createBausteineMassnahmenResultIdentifier(bausteineMassnahmenResult), m);
            }
            if(m != null){
                bausteinUmsetzung = (BausteinUmsetzung)Retriever.checkRetrieveElementAndChildren(bausteinUmsetzung);
                m.setId(getIndividualId(m.getId()));
                
                individualMassnahmenUmsetzung = massnahmenFactory.createMassnahmenUmsetzung(bausteinUmsetzung, m, GSScraperUtil.getInstance().getModel().getLanguage());
                individualMassnahmenUmsetzung = massnahmenFactory.transferUmsetzungWithDate(individualMassnahmenUmsetzung, bausteineMassnahmenResult.umstxt.getName(), bausteineMassnahmenResult.obm.getUmsDatBis());
                individualMassnahmenUmsetzung = massnahmenFactory.transferRevision(individualMassnahmenUmsetzung, bausteineMassnahmenResult.obm.getRevDat(), bausteineMassnahmenResult.obm.getRevDatNext(), bausteineMassnahmenResult.obm.getRevBeschr());
            } else {
                if(LOG.isDebugEnabled()) {
                    LOG.debug("Massnahme not found for massnahmenlink:\t" + bausteineMassnahmenResult.massnahme.getLink());
                }
            }
        }
        if(individualMassnahmenUmsetzung != null) {
            changedElements.add(individualMassnahmenUmsetzung);
        }
    }
    
    private static String getIndividualId(String id) {
        StringBuilder sb = new StringBuilder();
        sb.append(INDIVIDUAL_CONTROL_IDENTIFIER);
        for(int i = 0; i < id.length(); i++) {
            char c = id.charAt(i);
            if( c == '.' || Character.isDigit(c)){
                sb.append(c);
            }
        }
        return sb.toString();
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
    private Massnahme getIndividualMassnahmeFromGstoolDb(String bausteineMassnahmenResultIdentifier, Massnahme m) {
        if(massnahmenInfos.containsKey(bausteineMassnahmenResultIdentifier)){
            MassnahmeInformationTransfer massnahmeInformationTransfer = massnahmenInfos.get(bausteineMassnahmenResultIdentifier);
            if(!massnahmeInformationTransfer.getTitel().isEmpty()){
                m = new Massnahme();
                m.setId(massnahmeInformationTransfer.getId());
                m.setTitel((massnahmeInformationTransfer.getTitel() != null) ? massnahmeInformationTransfer.getTitel() : "no name available");
                m.setLebenszyklus((massnahmeInformationTransfer.getZyklus() != null) ? Integer.valueOf(massnahmeInformationTransfer.getZyklus()) : -1);
            }
        }

        return m;
    }

    public Set<MassnahmenUmsetzung> getChangedElements(){
        if(LOG.isDebugEnabled()){
            LOG.debug("Added " + changedElements.size() + " individual controls ");
        }
        return changedElements;
    }

}
