package sernet.verinice.rcp;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bp.IBpModelListener;
import sernet.verinice.model.bp.elements.BpModel;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.IBSIModelListener;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.IISO27KModelListener;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.validation.CnAValidation;
import sernet.verinice.service.commands.LoadElementTitles;

public class ElementTitleCache
        implements IBSIModelListener, IISO27KModelListener, IBpModelListener {

    private static final Logger LOG = Logger.getLogger(ElementTitleCache.class);

    private static HashMap<Integer, String> titleMap = new HashMap<>();

    private static ElementTitleCache instance;

    private static Set<String> typeIdSet = new HashSet<>();

    private static ElementTitleCache init() {
        if (instance == null) {
            instance = createInstance();
        }
        return instance;
    }

    private static ElementTitleCache createInstance() {
        instance = new ElementTitleCache();
        CnAElementFactory.getLoadedModel().addBSIModelListener(instance);
        CnAElementFactory.getInstance().getISO27kModel().addISO27KModelListener(instance);
        CnAElementFactory.getInstance().getBpModel().addModITBOModelListener(instance);
        return instance;
    }

    public static String get(Integer dbId) {
        return titleMap.get(dbId);
    }

    public static void load(String[] typeIds) {
        try {
            Activator.inheritVeriniceContextState();
            init();
            LoadElementTitles scopeCommand;
            scopeCommand = new LoadElementTitles(typeIds);
            scopeCommand = ServiceFactory.lookupCommandService().executeCommand(scopeCommand);
            titleMap = scopeCommand.getElements();
            typeIdSet.addAll(Arrays.asList(typeIds));
        } catch (CommandException e) {
            LOG.error("Error while loading element titles.", e);
        }

    }

    private void updateElement(CnATreeElement element) {
        if (element == null) {
            return;
        }
        if (typeIdSet.contains(element.getTypeId())) {
            titleMap.put(element.getDbId(), element.getTitle());
        }
    }

    private void reload() {
        titleMap.clear();
        load(typeIdSet.toArray(new String[typeIdSet.size()]));
    }

    @Override
    public void modelReload(ISO27KModel newModel) {
        reload();
    }

    @Override
    public void childAdded(CnATreeElement category, CnATreeElement child) {
        updateElement(child);
    }

    @Override
    public void childRemoved(CnATreeElement category, CnATreeElement child) {
    }

    @Override
    public void childChanged(CnATreeElement child) {
        updateElement(child);
    }

    @Override
    public void modelRefresh() {
        reload();
    }

    @Override
    public void modelRefresh(Object source) {
    }

    @Override
    public void linkChanged(CnALink oldLink, CnALink newLink, Object source) {
    }

    @Override
    public void linkRemoved(CnALink link) {
    }

    @Override
    public void linkAdded(CnALink link) {
    }

    @Override
    public void databaseChildAdded(CnATreeElement child) {
        updateElement(child);
    }

    @Override
    public void databaseChildRemoved(CnATreeElement child) {
    }

    @Override
    public void databaseChildRemoved(ChangeLogEntry entry) {
    }

    @Override
    public void databaseChildChanged(CnATreeElement child) {
        updateElement(child);
    }

    @Override
    public void modelReload(BSIModel newModel) {
        reload();
    }

    @Override
    public void validationAdded(Integer scopeId) {
    }

    @Override
    public void validationRemoved(Integer scopeId) {
    }

    @Override
    public void validationChanged(CnAValidation oldValidation, CnAValidation newValidation) {
    }

    @Override
    public void modelReload(BpModel newModel) {
        reload();
    }
}
