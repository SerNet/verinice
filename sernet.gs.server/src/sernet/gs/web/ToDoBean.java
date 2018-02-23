/*******************************************************************************
 * Copyright (c) 2009 Daniel Murygin <dm[at]sernet[dot]de>.
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
 *     Daniel <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.apache.log4j.Logger;

import sernet.gs.service.GSServiceException;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.IConfigurationService;
import sernet.verinice.interfaces.bpm.KeyValue;
import sernet.verinice.model.bpm.TodoViewItem;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.service.commands.SaveElement;
import sernet.verinice.service.commands.crud.LoadCnAElementById;
import sernet.verinice.service.commands.task.LoadChildrenAndMassnahmen;
import sernet.verinice.service.parser.GSScraperUtil;

/**
 * JSF managed bean for view ToDoList, template: todo/todo.xhtml
 * Asset-navigation data and methods are located in {@link AssetNavigationBean}.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@ManagedBean(name = "toDo")
@SessionScoped
public class ToDoBean {

    private static final Logger LOG = Logger.getLogger(ToDoBean.class);

    private static final int MAX_EXECUTIONS = 5;
    private static final int NR_SEAL = 4;

    public static final String BOUNDLE_NAME = "sernet.gs.web.Messages";

    @ManagedProperty("#{assetNavigation}")
    private AssetNavigationBean assetNavigation;

    private List<TodoViewItem> todoList = new ArrayList<TodoViewItem>();

    private TodoViewItem selectedItem;

    private MassnahmenUmsetzung massnahmeUmsetzung;

    private Converter umsetzungConverter = new UmsetzungConverter();

    private List<KeyValue> executionList;

    private boolean showDescription = false;

    private ICommandService commandService;

    // ISO 27001
    private boolean executionPerformed = true;
    private boolean executionManaged = true;
    private boolean executionEstablished = true;
    private boolean executionPredictable = true;
    private boolean executionOptimizing = true;

    private String selectedChapterId;

    private Set<GrundSchutzExecution> grundSchutzExecutionFilter;

    private Set<Seal> sealFilter;

    enum GrundSchutzExecution {
        EXECUTION_NO, EXECUTION_YES, EXECUTION_PARTLY, EXECUTION_DISPENSABLE, EXECUTION_UNTREATED;
    }

    public GrundSchutzExecution getGrundSchutzExecutionNo() {
        return GrundSchutzExecution.EXECUTION_NO;
    }

    public GrundSchutzExecution getGrundSchutzExecutionYes() {
        return GrundSchutzExecution.EXECUTION_YES;
    }

    public GrundSchutzExecution getGrundSchutzExecutionPartly() {
        return GrundSchutzExecution.EXECUTION_PARTLY;
    }

    public GrundSchutzExecution getGrundSchutzExecutionDispensable() {
        return GrundSchutzExecution.EXECUTION_DISPENSABLE;
    }

    public GrundSchutzExecution getGrundSchutzExecutionUntreated() {
        return GrundSchutzExecution.EXECUTION_UNTREATED;
    }

    public enum Seal {
        A, B, C, Z;
    }

    public Seal getSealA() {
        return Seal.A;
    }

    public Seal getSealB() {
        return Seal.B;
    }

    public Seal getSealC() {
        return Seal.C;
    }

    public Seal getSealZ() {
        return Seal.Z;
    }

    public ToDoBean() {
        super();
        // Grundschutz
        executionList = new ArrayList<KeyValue>(MAX_EXECUTIONS);
        executionList.add(new KeyValue(UmsetzungConverter.ENTBERHRLICH, MassnahmenUmsetzung.P_UMSETZUNG_ENTBEHRLICH));
        executionList.add(new KeyValue(UmsetzungConverter.JA, MassnahmenUmsetzung.P_UMSETZUNG_JA));
        executionList.add(new KeyValue(UmsetzungConverter.NEIN, MassnahmenUmsetzung.P_UMSETZUNG_NEIN));
        executionList.add(new KeyValue(UmsetzungConverter.TEILWEISE, MassnahmenUmsetzung.P_UMSETZUNG_TEILWEISE));
        executionList.add(new KeyValue(UmsetzungConverter.UNBEARBEITET, MassnahmenUmsetzung.P_UMSETZUNG_UNBEARBEITET));

        grundSchutzExecutionFilter =  new HashSet<>();
        grundSchutzExecutionFilter.add(GrundSchutzExecution.EXECUTION_UNTREATED);

        sealFilter = new HashSet<>();
    }

    @PostConstruct
    public void init() {
        grundSchutzExecutionFilter.add(GrundSchutzExecution.EXECUTION_UNTREATED);
        sealFilter.addAll(EnumSet.allOf(Seal.class));
    }

    public void loadToDoList() {
        if (getAssetNavigation().getSelectedElementId() != null && getAssetNavigation().getSelectedElementId() > 0) {
            loadToDoListForElement();
        } else {
            loadToDoListForVerbund();
        }
    }

    public void loadToDoListForVerbund() {

        String title = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("itVerbundTitle");
        if (title != null) {
            getAssetNavigation().setSelectedItVerbundTitel(title);
        }

        loadToDoList(AssetNavigationBean.SOURCE_VERBUND);
    }

    public void loadToDosForItNetworkItem() {

        if (getAssetNavigation().getItVerbundItem() != null) {
            getAssetNavigation().setSelectedItVerbundTitel(getAssetNavigation().getItVerbundItem());
        }

        loadToDoList(AssetNavigationBean.SOURCE_VERBUND);
    }

    public void loadToDoListForElement() {
        if (selectedChapterId != null) {
            getAssetNavigation().setSelectedElementId(Integer.valueOf(selectedChapterId));
        }
        loadToDoList(AssetNavigationBean.SOURCE_ELEMENT);
    }

    public void loadToDoList(int source) {
        // create command
        LoadChildrenAndMassnahmen command = new LoadChildrenAndMassnahmen();
        command.setExecutionSet(createExecutionSet());
        command.setSealSet(createSealSet());

        // execute command in AssetNavigationBean
        // to load children together with measures
        getAssetNavigation().loadChildren(command, source);

        // extract measures from command
        Set<TodoViewItem> massnahmenList = command.getMassnahmen();
        getTodoList().clear();
        getTodoList().addAll(massnahmenList);
        Collections.sort(getTodoList());
        MassnahmenUmsetzung selectedMassnahme = getMassnahmeUmsetzung();
        boolean massnahmeInList = false;
        if (selectedMassnahme != null) {
            for (TodoViewItem item : massnahmenList) {
                if (selectedMassnahme.getDbId() == item.getDbId()) {
                    massnahmeInList = true;
                    break;
                }
            }
            if (!massnahmeInList) {
                setSelectedItem(null);
                setMassnahmeUmsetzung(null);
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("ToDo List size: " + getToDoListSize());
        }
    }

    private Set<String> createExecutionSet() {
        Set<String> executionSet = new HashSet<String>(MAX_EXECUTIONS);

        if (grundSchutzExecutionFilter.contains(GrundSchutzExecution.EXECUTION_NO)) {
            executionSet.add(MassnahmenUmsetzung.P_UMSETZUNG_NEIN);
        }

        if (grundSchutzExecutionFilter.contains(GrundSchutzExecution.EXECUTION_YES)) {
            executionSet.add(MassnahmenUmsetzung.P_UMSETZUNG_JA);
        }

        if (grundSchutzExecutionFilter.contains(GrundSchutzExecution.EXECUTION_PARTLY)) {
            executionSet.add(MassnahmenUmsetzung.P_UMSETZUNG_TEILWEISE);
        }

        if (grundSchutzExecutionFilter.contains(GrundSchutzExecution.EXECUTION_DISPENSABLE)) {
            executionSet.add(MassnahmenUmsetzung.P_UMSETZUNG_ENTBEHRLICH);
        }

        if (grundSchutzExecutionFilter.contains(GrundSchutzExecution.EXECUTION_UNTREATED)) {
            executionSet.add(MassnahmenUmsetzung.P_UMSETZUNG_UNBEARBEITET);
        }

        // ISO 27001
        if (isExecutionEstablished()) {
            executionSet.add(MassnahmenUmsetzung.P_UMSETZUNG_ESTABLISHED);
        }
        if (isExecutionManaged()) {
            executionSet.add(MassnahmenUmsetzung.P_UMSETZUNG_MANAGED);
        }
        if (isExecutionOptimizing()) {
            executionSet.add(MassnahmenUmsetzung.P_UMSETZUNG_OPTIMIZING);
        }
        if (isExecutionPerformed()) {
            executionSet.add(MassnahmenUmsetzung.P_UMSETZUNG_PERFORMED);
        }
        if (isExecutionPredictable()) {
            executionSet.add(MassnahmenUmsetzung.P_UMSETZUNG_PREDICTABLE);
        }

        return executionSet;
    }

    private Set<String> createSealSet() {
        Set<String> sealSet = new HashSet<String>(NR_SEAL);
        if (sealFilter.contains(Seal.A)) {
            sealSet.add("A");
        }
        if (sealFilter.contains(Seal.B)) {
            sealSet.add("B");
        }
        if (sealFilter.contains(Seal.C)) {
            sealSet.add("C");
        }
        if (sealFilter.contains(Seal.Z)) {
            sealSet.add("Z");
        }

        return sealSet;
    }

    public void loadToDo() {
        LOG.debug("loadToDo");
        int massnahmeId = -1;
        try {

            if (getSelectedItem() != null) {
                massnahmeId = getSelectedItem().getDbId();
                LoadCnAElementById command = new LoadCnAElementById(MassnahmenUmsetzung.TYPE_ID, massnahmeId);

                getCommandService().executeCommand(command);
                MassnahmenUmsetzung result = (MassnahmenUmsetzung) command.getFound();
                if (result == null) {
                    LOG.warn("No massnahme found with id: " + massnahmeId);
                } else if (LOG.isDebugEnabled()) {
                    LOG.debug("Massnahme loaded, id: " + massnahmeId);
                }
                setMassnahmeUmsetzung(result);
                showDescription = false;
            } else {
                LOG.warn("No todo-item selected. Can not load massnahme.");
            }
        } catch (Exception e) {
            LOG.error("Error while loading todos for id: " + massnahmeId, e);
            Util.addError("toDoTable", Util.getMessage("todo.load.failed"));
        }
    }

    public boolean writeEnabled() {
        boolean enabled = false;
        if(getMassnahmeUmsetzung()!=null) {
            enabled = getConfigurationService().isWriteAllowed(getMassnahmeUmsetzung());
        }

        return enabled;
    }

    public void save() {
        LOG.debug("save called...");
        int massnahmeId = -1;
        try {
            if (getMassnahmeUmsetzung() != null) {
                if (!writeEnabled()) {
                    throw new SecurityException("write is not allowed");
                }
                massnahmeId = getMassnahmeUmsetzung().getDbId();
                SaveElement<MassnahmenUmsetzung> command = new SaveElement<MassnahmenUmsetzung>(getMassnahmeUmsetzung());
                getCommandService().executeCommand(command);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Massnahme saved, id: " + massnahmeId);
                }
                loadToDoListForElement();
                Util.addInfo("submit", Util.getMessage("todo.saved") + ": " + getMassnahmeUmsetzung().getTitle());
            } else {
                LOG.warn("Control is null. Can not save.");
            }
        } catch (Exception e) {
            LOG.error("Error while saving massnahme: " + massnahmeId, e);
            ExceptionHandler.handle(e);
        }
    }

    public String getMassnahmeHtml() {
        final MassnahmenUmsetzung massnahme = getMassnahmeUmsetzung();
        String text = null;
        if (massnahme != null) {
            try {
                text = GSScraperUtil.getInstanceWeb().getModel().getMassnahmeHtml(massnahme.getUrl(), massnahme.getStand());
            } catch (GSServiceException e) {
                LOG.error("Error while loading massnahme description.", e);
                Util.addError("submit", Util.getMessage("todo.load.failed"));
            }
        }
        if (text != null) {
            int start = text.indexOf("<div id=\"content\">");
            int end = text.lastIndexOf("</body>");
            if (start == -1 || end == -1) {
                LOG.error("Can not find content of control description: " + text);
                text = "";
            } else {
                text = text.substring(start, end);
            }
        } else {
            text = "";
        }
        return text;
    }

    public void english() {
        Util.english();
    }

    public void german() {
        Util.german();
    }

    public void toggleDescription() {
        showDescription = !showDescription;
    }

    public AssetNavigationBean getAssetNavigation() {
        return assetNavigation;
    }

    public void setAssetNavigation(AssetNavigationBean assetNavigation) {
        this.assetNavigation = assetNavigation;
    }

    public Locale getLocale() {
        return FacesContext.getCurrentInstance().getViewRoot().getLocale();
    }

    public TimeZone getTimeZone() {
        return TimeZone.getDefault();
    }

    public String getUmsetzung() {
        String umsetzung = null;
        if (getMassnahmeUmsetzung() != null) {
            umsetzung = getMassnahmeUmsetzung().getUmsetzung();
        }
        return umsetzung;
    }

    public void setUmsetzung(String umsetzung) {
        if (getMassnahmeUmsetzung() != null) {
            getMassnahmeUmsetzung().setUmsetzung(umsetzung);
        }
    }

    public int getToDoListSize() {
        return getTodoList().size();
    }

    public List<TodoViewItem> getTodoList() {
        return todoList;
    }

    public void setTodoList(List<TodoViewItem> todoList) {
        this.todoList = todoList;
    }

    public TodoViewItem getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(TodoViewItem selectedItem) {
        this.selectedItem = selectedItem;
    }

    public MassnahmenUmsetzung getMassnahmeUmsetzung() {
        return massnahmeUmsetzung;
    }

    public void setMassnahmeUmsetzung(MassnahmenUmsetzung massnahmeUmsetzung) {
        this.massnahmeUmsetzung = massnahmeUmsetzung;
    }

    public Converter getUmsetzungConverter() {
        return umsetzungConverter;
    }

    public void setUmsetzungConverter(Converter umsetzungConverter) {
        this.umsetzungConverter = umsetzungConverter;
    }

    public List<KeyValue> getExecutionList() {
        return executionList;
    }

    public void setExecutionList(List<KeyValue> umsetzungList) {
        this.executionList = umsetzungList;
    }

    public boolean isExecutionPerformed() {
        return executionPerformed;
    }

    public void setExecutionPerformed(boolean executionPerformed) {
        this.executionPerformed = executionPerformed;
    }

    public boolean isExecutionManaged() {
        return executionManaged;
    }

    public void setExecutionManaged(boolean executionManaged) {
        this.executionManaged = executionManaged;
    }

    public boolean isExecutionEstablished() {
        return executionEstablished;
    }

    public void setExecutionEstablished(boolean executionEstablished) {
        this.executionEstablished = executionEstablished;
    }

    public boolean isExecutionPredictable() {
        return executionPredictable;
    }

    public void setExecutionPredictable(boolean executionPredictable) {
        this.executionPredictable = executionPredictable;
    }

    public boolean isExecutionOptimizing() {
        return executionOptimizing;
    }

    public void setExecutionOptimizing(boolean executionOptimizing) {
        this.executionOptimizing = executionOptimizing;
    }

    public int getSize() {
        return getTodoList() == null ? 0 : getTodoList().size();
    }

    public boolean getShowDescription() {
        return showDescription;
    }

    private ICommandService getCommandService() {
        if (commandService == null) {
            commandService = createCommandService();
        }
        return commandService;
    }

    private ICommandService createCommandService() {
        return (ICommandService) VeriniceContext.get(VeriniceContext.COMMAND_SERVICE);
    }
    
    private IConfigurationService getConfigurationService() {
        return (IConfigurationService) VeriniceContext.get(VeriniceContext.CONFIGURATION_SERVICE);
    }

    public static String getcurrentLanguageTag() {
        return Util.getcurrentLanguageTag();
    }

    public Set<GrundSchutzExecution> getGrundSchutzExecutionFilter() {
        return grundSchutzExecutionFilter;
    }

    public void setGrundSchutzExecutionFilter(Set<GrundSchutzExecution> grundSchutzExecutionFilter) {
        this.grundSchutzExecutionFilter = grundSchutzExecutionFilter;
    }

    public Set<Seal> getSealFilter() {
        return sealFilter;
    }

    public void setSealFilter(Set<Seal> sealFilter) {
        this.sealFilter = sealFilter;
    }

    public String getSelectedChapterId() {
        return selectedChapterId;
    }

    public void setSelectedChapterId(String selectedChapterId) {
        this.selectedChapterId = selectedChapterId;
    }

}
