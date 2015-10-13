/*******************************************************************************
 * Copyright (c) 2010 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public
 * License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.taskcommands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.model.Baustein;
import sernet.gs.reveng.MbBaust;
import sernet.gs.reveng.ModZobjBst;
import sernet.gs.reveng.importData.BausteineMassnahmenResult;
import sernet.gs.service.RuntimeCommandException;
import sernet.gs.ui.rcp.gsimport.TransferData;
import sernet.gs.ui.rcp.main.bsi.model.BSIMassnahmenModel;
import sernet.gs.ui.rcp.main.bsi.model.GSScraperUtil;
import sernet.gs.ui.rcp.main.bsi.model.IBSIConfig;
import sernet.gs.ui.rcp.main.common.model.IProgress;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.grundschutzparser.LoadBausteine;
import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.HuiRelation;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.CreateLink;
import sernet.verinice.service.commands.LoadCnAElementByExternalID;

/**
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 *
 */
public class ImportCreateBausteinReferences extends GenericCommand {

    private transient Logger log = Logger.getLogger(ImportCreateBausteinReferences.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(ImportCreateBausteinReferences.class);
        }
        return log;
    }

    private CnATreeElement element;
    private List<Baustein> bausteine;
    private final Map<MbBaust, List<BausteineMassnahmenResult>> bausteineMassnahmenMap;
    private final Map<MbBaust, ModZobjBst> bausteinMap;
    private final Map<MbBaust, Baustein> gstool2VeriniceBausteinMap;
    private final String sourceId;
    private IBSIConfig bsiConfig;
    private static final String NO_COMMENT = "";

    public ImportCreateBausteinReferences(String sourceId, CnATreeElement element, Map<MbBaust, List<BausteineMassnahmenResult>> bausteineMassnahmenMap, Map<MbBaust, ModZobjBst> bausteinMap, Map<MbBaust, Baustein> gstool2VeriniceBausteinMap) {
        this.element = element;
        this.bausteineMassnahmenMap = bausteineMassnahmenMap;
        this.sourceId = sourceId;
        this.bausteinMap = bausteinMap;
        this.gstool2VeriniceBausteinMap = gstool2VeriniceBausteinMap;
    }

    public ImportCreateBausteinReferences(String sourceId, CnATreeElement element, Map<MbBaust, List<BausteineMassnahmenResult>> bausteineMassnahmenMap, IBSIConfig bsiConfig, Map<MbBaust, ModZobjBst> bausteinMap, Map<MbBaust, Baustein> gstool2VeriniceBausteinMap) {
        this.element = element;
        this.bausteineMassnahmenMap = bausteineMassnahmenMap;
        this.sourceId = sourceId;
        this.bsiConfig = bsiConfig;
        this.bausteinMap = bausteinMap;
        this.gstool2VeriniceBausteinMap = gstool2VeriniceBausteinMap;
    }

    /*
     * (non-Javadoc)
     *
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        try {
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
                createBausteinReference(element, mbBaust, bausteineMassnahmenMap.get(mbBaust));
            }

        } catch (Exception e) {
            throw new RuntimeCommandException(e);
        }

    }


    /*
     * (non-Javadoc)
     *
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    public void createBausteinReference(CnATreeElement element, MbBaust mbBaust, List<BausteineMassnahmenResult> list) throws CommandException {

        String bausteinId = TransferData.getId(mbBaust);
        Baustein baustein = findBausteinForId(bausteinId); // catalogue-baustein

        Integer refZobId = null;
        if(baustein==null && mbBaust.getId().getBauImpId() == 1) { // no baustein found, so it mbBaust has to be userdefined (not in catalogue existant)
            baustein = getVeriniceBaustein(mbBaust);// bst is userdefined, so create own instance of Baustein
        }
        refZobId = getRefZobIdFromDBResult(list, refZobId);
        if (refZobId != null && baustein != null) {
            createBausteinReferences(element, baustein, refZobId);
        }
    }

    /**
     * @param list
     * @param refZobId
     * @return
     */
    private Integer getRefZobIdFromDBResult(List<BausteineMassnahmenResult> list, Integer refZobId) {
        isReference: for (BausteineMassnahmenResult bausteineMassnahmenResult : list) {
            refZobId = bausteineMassnahmenResult.zoBst.getRefZobId();

            if (refZobId != null) {
                break isReference;
            }
        }
        return refZobId;
    }

    /**
     * @param element
     * @param baustein
     * @param refZobId
     * @throws CommandException
     */
    private void createBausteinReferences(CnATreeElement element, Baustein baustein, Integer refZobId) throws CommandException {
        if(getLog().isDebugEnabled()){
            getLog().debug("Looking for previously created baustein by sourceId, extId: " + sourceId + ", " + createExtId(baustein, refZobId));
        }
        LoadCnAElementByExternalID cmd = new LoadCnAElementByExternalID(sourceId, createExtId(baustein, refZobId));
        cmd = getCommandService().executeCommand(cmd);
        List<CnATreeElement> elements = cmd.getElements();
        if (elements != null && elements.iterator().hasNext()) {
            CnATreeElement previousBaustein = elements.iterator().next();
            ArrayList bausteinAsList = new ArrayList();
            bausteinAsList.add(previousBaustein);

            Set<HuiRelation> possibleRelations = HitroUtil.getInstance().getTypeFactory().getPossibleRelations(previousBaustein.getEntityType().getId(), element.getEntityType().getId());
            if (!possibleRelations.isEmpty()) {
                CreateLink cmd2 = new CreateLink(previousBaustein, element, possibleRelations.iterator().next().getId(), NO_COMMENT);
                getCommandService().executeCommand(cmd2);
            }
        }
    }

    private String createExtId(Baustein baustein, Integer refZobId) {
        return baustein.getId() + "-" + String.valueOf(refZobId);
    }

    private Baustein findBausteinForId(String id) {
        for (Baustein baustein : bausteine) {
            if (baustein.getId().equals(id)){
                return baustein;
            }
        }
        return null;
    }

    private boolean mbBausteinEquals(MbBaust mbB1, MbBaust mbB2){
        return mbB1.getNr().equals(mbB2.getNr()) && mbB1.getId().getBauId().equals(mbB2.getId().getBauId());
    }

    private Baustein getVeriniceBaustein(MbBaust mbBaust){
        for(MbBaust mbBKey : gstool2VeriniceBausteinMap.keySet()){
            if(mbBausteinEquals(mbBKey, mbBaust)){
                return gstool2VeriniceBausteinMap.get(mbBKey);
            }
        }
        return null;
    }

}
