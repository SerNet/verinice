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
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.model.Baustein;
import sernet.gs.model.Massnahme;
import sernet.gs.reveng.ModZobjBstMass;
import sernet.gs.reveng.ModZobjBstMassId;
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
 * Command imports the controls tagged with >I< from the GSTOOL db, which do not
 * exist as children of the parent baustein in the itgs catalogue
 * 
 * @author Sebastian Hagedorn <sh[at]sernet[dot]de>
 *
 */
@SuppressWarnings("serial")
public class ImportIndividualMassnahmen extends GenericCommand {

    private static final Logger LOG = Logger.getLogger(ImportIndividualMassnahmen.class);

    private static final String INDIVIDUAL_CONTROL_IDENTIFIER = "iM ";

    private final Map<BausteinUmsetzung, List<BausteineMassnahmenResult>> individualMassnahmenMap;
    private final Map<ModZobjBstMass, MassnahmenUmsetzung> alleMassnahmenMap;
    private final List<Baustein> allCatalogueBausteine;
    private final Map<String, MassnahmeInformationTransfer> massnahmenInfos;
    private final Set<MassnahmenUmsetzung> changedElements;

    public ImportIndividualMassnahmen(
            Map<BausteinUmsetzung, List<BausteineMassnahmenResult>> individualBausteinMassnahmenResultMap,
            Map<ModZobjBstMass, MassnahmenUmsetzung> alleMassnahmenMap,
            List<Baustein> allCatalogueBausteine,
            Map<String, MassnahmeInformationTransfer> massnahmenInfos) {
        this.individualMassnahmenMap = individualBausteinMassnahmenResultMap; // bausteinumsetzung,
                                                                              // list<bausteinmassnahmeresult>
        this.alleMassnahmenMap = alleMassnahmenMap; // modzobjbstmass,
                                                    // massnahmenumsetzung
        this.allCatalogueBausteine = allCatalogueBausteine; // list baustein
        this.massnahmenInfos = massnahmenInfos; // bausteinmassnahmeresult,
                                                // massnahmeinformationstransfer
        this.changedElements = new HashSet<>(); // set<bausteinumsetzung>
    }

    @Override
    public void execute() {
        for (Entry<BausteinUmsetzung, List<BausteineMassnahmenResult>> entry : individualMassnahmenMap
                .entrySet()) {
            BausteinUmsetzung bausteinUmsetzung = entry.getKey();
            List<BausteineMassnahmenResult> individualMassnahmenForBaustein = entry.getValue();
            if (!individualMassnahmenForBaustein.isEmpty()) {
                createIndividualMassnahmenForBausteinUmsetzung(bausteinUmsetzung,
                        individualMassnahmenForBaustein);
            }
        }
    }

    /**
     * @param bausteinUmsetzung
     * @param bausteineMassnahmenResult
     */
    private void createIndividualMassnahmenForBausteinMassnahmenResult(
            BausteinUmsetzung bausteinUmsetzung,
            BausteineMassnahmenResult bausteineMassnahmenResult) {
        MassnahmenUmsetzung individualMassnahmenUmsetzung = getIndividualMassnahmenUmsetzungFromAlreadyParsedElements(
                bausteineMassnahmenResult, bausteinUmsetzung);
        if (individualMassnahmenUmsetzung == null) {
            Massnahme m = findCatalogMassnahmeByURL(bausteineMassnahmenResult.massnahme.getLink());
            if (m == null) {
                m = getIndividualMassnahmeFromGstoolDb(TransferData
                        .createBausteineMassnahmenResultIdentifier(bausteineMassnahmenResult), m);
            }
            if (m != null) {
                bausteinUmsetzung = (BausteinUmsetzung) Retriever
                        .checkRetrieveElementAndChildren(bausteinUmsetzung);
                m.setId(getIndividualId(m.getId()));

                MassnahmenFactory massnahmenFactory = new MassnahmenFactory();

                individualMassnahmenUmsetzung = massnahmenFactory.createMassnahmenUmsetzung(
                        bausteinUmsetzung, m, GSScraperUtil.getInstance().getModel().getLanguage());
                individualMassnahmenUmsetzung = massnahmenFactory.transferUmsetzungWithDate(
                        individualMassnahmenUmsetzung, bausteineMassnahmenResult.umstxt.getName(),
                        bausteineMassnahmenResult.obm.getUmsDatBis());
                individualMassnahmenUmsetzung = massnahmenFactory.transferRevision(
                        individualMassnahmenUmsetzung, bausteineMassnahmenResult.obm.getRevDat(),
                        bausteineMassnahmenResult.obm.getRevDatNext(),
                        bausteineMassnahmenResult.obm.getRevBeschr());
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Massnahme not found for massnahmenlink:\t"
                            + bausteineMassnahmenResult.massnahme.getLink());
                }
            }
        }
        if (individualMassnahmenUmsetzung != null) {
            changedElements.add(individualMassnahmenUmsetzung);
        }
    }

    private static String getIndividualId(String id) {
        StringBuilder sb = new StringBuilder();
        sb.append(INDIVIDUAL_CONTROL_IDENTIFIER);
        for (int i = 0; i < id.length(); i++) {
            char c = id.charAt(i);
            if (c == '.' || Character.isDigit(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private void createIndividualMassnahmenForBausteinUmsetzung(BausteinUmsetzung bausteinUmsetzung,
            List<BausteineMassnahmenResult> individualMassnahmenForBaustein) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating " + individualMassnahmenForBaustein.size()
                    + " individual massnahmen for bausteinUmsetzung:\t"
                    + bausteinUmsetzung.getTitle() + "<" + bausteinUmsetzung.getUuid() + ">");
        }
        for (BausteineMassnahmenResult bausteineMassnahmenResult : individualMassnahmenForBaustein) {
            createIndividualMassnahmenForBausteinMassnahmenResult(bausteinUmsetzung,
                    bausteineMassnahmenResult);
        }
    }

    private MassnahmenUmsetzung getIndividualMassnahmenUmsetzungFromAlreadyParsedElements(
            BausteineMassnahmenResult bausteinMassnahmenResult, BausteinUmsetzung parent) {
        MassnahmenUmsetzung existingSourceMassnahmenUmsetzung = getMassnahmeFromAlleMassnahmenMap(
                bausteinMassnahmenResult);
        MassnahmenUmsetzung individualMassnahmenUmsetzung = null;
        if (existingSourceMassnahmenUmsetzung != null) {
            individualMassnahmenUmsetzung = new MassnahmenUmsetzung(parent);
            individualMassnahmenUmsetzung.getEntity()
                    .copyEntity(existingSourceMassnahmenUmsetzung.getEntity());
        }
        return individualMassnahmenUmsetzung;
    }

    private MassnahmenUmsetzung getMassnahmeFromAlleMassnahmenMap(
            BausteineMassnahmenResult bausteineMassnahmenResult) {
        ModZobjBstMassId soughtId = bausteineMassnahmenResult.obm.getId();
        for (Entry<ModZobjBstMass, MassnahmenUmsetzung> entry : alleMassnahmenMap.entrySet()) {
            ModZobjBstMassId currentEntryId = entry.getKey().getId();
            if (currentEntryId.getZobId().equals(soughtId.getZobId())
                    && currentEntryId.getBauId().equals(soughtId.getBauId())
                    && currentEntryId.getMasId().equals(soughtId.getMasId())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private Massnahme findCatalogMassnahmeByURL(String url) {

        int lastIndexOfBackslash = url.lastIndexOf('\\');
        if (lastIndexOfBackslash != -1) {
            url = url.substring(lastIndexOfBackslash + 1);
        }
        int lastIndexOfPeriod = url.lastIndexOf('.');

        if (lastIndexOfPeriod != -1) {
            url = url.substring(0, lastIndexOfPeriod);
        }
        for (Baustein b : allCatalogueBausteine) {
            for (Massnahme m : b.getMassnahmen()) {
                if (url.equals(m.getUrl())) {
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
    private Massnahme getIndividualMassnahmeFromGstoolDb(String bausteineMassnahmenResultIdentifier,
            Massnahme m) {
        if (massnahmenInfos.containsKey(bausteineMassnahmenResultIdentifier)) {
            MassnahmeInformationTransfer massnahmeInformationTransfer = massnahmenInfos
                    .get(bausteineMassnahmenResultIdentifier);
            if (!massnahmeInformationTransfer.getTitel().isEmpty()) {
                m = new Massnahme();
                m.setId(massnahmeInformationTransfer.getId());
                m.setTitel((massnahmeInformationTransfer.getTitel() != null)
                        ? massnahmeInformationTransfer.getTitel()
                        : "no name available");
                m.setLebenszyklus((massnahmeInformationTransfer.getZyklus() != null)
                        ? Integer.valueOf(massnahmeInformationTransfer.getZyklus())
                        : -1);
            }
        }

        return m;
    }

    public Set<MassnahmenUmsetzung> getChangedElements() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Added " + changedElements.size() + " individual controls ");
        }
        return changedElements;
    }

}
