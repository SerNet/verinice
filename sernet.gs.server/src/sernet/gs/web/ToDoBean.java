/**
 * License Agreement.
 *
 * Rich Faces - Natural Ajax for Java Server Faces (JSF)
 *
 * Copyright (C) 2007 Exadel, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */

package sernet.gs.web;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TimeZone;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.apache.log4j.Logger;
import org.richfaces.component.html.HtmlExtendedDataTable;
import org.richfaces.model.selection.Selection;
import org.richfaces.model.selection.SimpleSelection;

import sernet.gs.model.Massnahme;
import sernet.gs.server.ServerInitializer;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.TodoViewItem;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.service.ICommandService;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementById;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementByType;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveElement;
import sernet.gs.ui.rcp.main.service.taskcommands.FindMassnahmenForITVerbund;
import sernet.gs.ui.rcp.main.service.taskcommands.LoadChildrenAndMassnahmen;
import sernet.hui.common.VeriniceContext;

/**
 * @author $Autor$
 *
 */
public class ToDoBean {

	final static Logger LOG = Logger.getLogger(ToDoBean.class);
	
	public static final String BOUNDLE_NAME = "sernet.gs.web.Messages";
	
	final static int SOURCE_VERBUND = 1;
	
	final static int SOURCE_ELEMENT = 2;
	
	List<ITVerbund> itVerbundList;
	
	private Selection selection = new SimpleSelection(); 
	
	private HtmlExtendedDataTable table;
	
	ITVerbund selectedItVerbund;
	
	String selectedItVerbundTitel;
	
	Integer selectedElementId;
	
	List<TodoViewItem> todoList = new ArrayList<TodoViewItem>();
	
	private List<CnATreeElement> gebaeudeList = new ArrayList<CnATreeElement>(10);
	private List<CnATreeElement> raumList = new ArrayList<CnATreeElement>(10);
	private List<CnATreeElement> clienteList = new ArrayList<CnATreeElement>(10);
	private List<CnATreeElement> serverList = new ArrayList<CnATreeElement>(10);
	private List<CnATreeElement> netzList = new ArrayList<CnATreeElement>(10);
	private List<CnATreeElement> anwendungList = new ArrayList<CnATreeElement>(10);
	private List<CnATreeElement> personList = new ArrayList<CnATreeElement>(10);
	
	TodoViewItem selectedItem;
	
	MassnahmenUmsetzung massnahmeUmsetzung;
	
	Converter umsetzungConverter = new UmsetzungConverter();
	
	Converter itVerbundConverter = new ItVerbundConverter(this);
	
	List<String> executionList;
	
	// Grundschutz
	boolean executionNo = false;
	boolean executionYes = false;
	boolean executionPartly = false;
	boolean executionDispensable = false;					    
	boolean executionUntreated = true;
	
	// ISO 27001
	boolean executionPerformed = true;
	boolean executionManaged = true;
	boolean executionEstablished = true;
	boolean executionPredictable = true;
	boolean executionOptimizing = true;
	
	boolean sealA = true;
	boolean sealB = true;
	boolean sealC = true;
	boolean sealZ = true;
		
	public ToDoBean() {
		super();
		// Grundschutz
		executionList = Arrays.asList(MassnahmenUmsetzung.P_UMSETZUNG_ENTBEHRLICH,MassnahmenUmsetzung.P_UMSETZUNG_JA,MassnahmenUmsetzung.P_UMSETZUNG_NEIN,MassnahmenUmsetzung.P_UMSETZUNG_TEILWEISE,MassnahmenUmsetzung.P_UMSETZUNG_UNBEARBEITET);
		// ISO 27001
		//executionList = Arrays.asList(MassnahmenUmsetzung.P_UMSETZUNG_ESTABLISHED,MassnahmenUmsetzung.P_UMSETZUNG_MANAGED,MassnahmenUmsetzung.P_UMSETZUNG_OPTIMIZING,MassnahmenUmsetzung.P_UMSETZUNG_PERFORMED,MassnahmenUmsetzung.P_UMSETZUNG_PREDICTABLE);
		loadItVerbundList();
	}
	
	public void loadItVerbundList() {
		//ServerInitializer.inheritVeriniceContextState();
		ICommandService service = (ICommandService) VeriniceContext.get(VeriniceContext.COMMAND_SERVICE);
		LoadCnAElementByType<ITVerbund> command = new LoadCnAElementByType<ITVerbund>(ITVerbund.class);
		try {
			service.executeCommand(command);
			setItVerbundList(command.getElements());
		} catch (CommandException e) {
			LOG.error("Error while loading IT-Verbuende", e);
		}	
	}
	
	public void loadToDoList() {
		if(getSelectedElementId()!=null && getSelectedElementId()>0) {
			loadToDoListForElement();
		} else {
			loadToDoListForVerbund();
		}
	}
	
	public void loadToDoListForVerbund() {
		loadToDoList(SOURCE_VERBUND);
	}
	
	public void loadToDoListForElement() {
		loadToDoList(SOURCE_ELEMENT);
	}

	public void loadToDoList(int source) {
		setSelectedItVerbund((ITVerbund) itVerbundConverter.getAsObject(null, null, getSelectedItVerbundTitel()));
		
		Integer itVerbundId = (getSelectedItVerbund()==null) ? null : getSelectedItVerbund().getDbId();
		if(itVerbundId!=null || selectedElementId!=null) {
			ICommandService service = (ICommandService) VeriniceContext.get(VeriniceContext.COMMAND_SERVICE);
			int id = (SOURCE_VERBUND==source) ? itVerbundId : selectedElementId;
			LoadChildrenAndMassnahmen command = new LoadChildrenAndMassnahmen(id);
			command.setExecutionSet(createExecutionSet());
			command.setSealSet(createSealSet());
			try {
				service.executeCommand(command);
				getTodoList().clear();
				Set<TodoViewItem> massnahmenList = command.getMassnahmen();			
				getTodoList().addAll(massnahmenList);
				MassnahmenUmsetzung selectedMassnahme = getMassnahmeUmsetzung();
				boolean massnahmeInList = false;
				if(selectedMassnahme!=null) {
					for (TodoViewItem item : massnahmenList) {
						if(selectedMassnahme.getDbId()==item.getdbId()) {
							massnahmeInList = true;
							break;
						}
					}
					if(!massnahmeInList) {
						setSelectedItem(null);
						setMassnahmeUmsetzung(null);
					}
				}
				if (LOG.isDebugEnabled()) {
					LOG.debug("ToDo List size: " + getToDoListSize());
				}
				if(SOURCE_VERBUND==source) {
					setAnwendungList(command.getAnwendungList());
					setClienteList(command.getClienteList());
					setGebaeudeList(command.getGebaeudeList());
					setNetzList(command.getNetzList());
					setPersonList(command.getPersonList());
					setRaumList(command.getRaumList());
					setServerList(command.getServerList());
				}
			} catch (CommandException e) {
				LOG.error("Error while loading todos for id: " + itVerbundId, e);
			}
		} else {
			setSelectedItem(null);
			setMassnahmeUmsetzung(null);
		}
	}
	
	private Set<String> createExecutionSet() {
		Set<String> executionSet = new HashSet<String>(5);
		// Grundschutz
		if(isExecutionDispensable()) {
			executionSet.add(MassnahmenUmsetzung.P_UMSETZUNG_ENTBEHRLICH);
		}
		if(isExecutionNo()) {
			executionSet.add(MassnahmenUmsetzung.P_UMSETZUNG_NEIN);
		}
		if(isExecutionPartly()) {
			executionSet.add(MassnahmenUmsetzung.P_UMSETZUNG_TEILWEISE);
		}
		if(isExecutionUntreated()) {
			executionSet.add(MassnahmenUmsetzung.P_UMSETZUNG_UNBEARBEITET);
		}
		if(isExecutionYes()) {
			executionSet.add(MassnahmenUmsetzung.P_UMSETZUNG_JA);
		}
		// ISO 27001
		if(isExecutionEstablished()) {
			executionSet.add(MassnahmenUmsetzung.P_UMSETZUNG_ESTABLISHED);
		}
		if(isExecutionManaged()) {
			executionSet.add(MassnahmenUmsetzung.P_UMSETZUNG_MANAGED);
		}
		if(isExecutionOptimizing()) {
			executionSet.add(MassnahmenUmsetzung.P_UMSETZUNG_OPTIMIZING);
		}
		if(isExecutionPerformed()) {
			executionSet.add(MassnahmenUmsetzung.P_UMSETZUNG_PERFORMED);
		}
		if(isExecutionPredictable()) {
			executionSet.add(MassnahmenUmsetzung.P_UMSETZUNG_PREDICTABLE);
		}
		
		return executionSet;
	}
	
	private Set<String> createSealSet() {
		Set<String> sealSet = new HashSet<String>(4);
		if(isSealA()) {
			sealSet.add("A");
		}
		if(isSealB()) {
			sealSet.add("B");
		}
		if(isSealC()) {
			sealSet.add("C");
		}
		if(isSealZ()) {
			sealSet.add("Z");
		}
		
		return sealSet;
	}

	public void loadToDo() {
		LOG.debug("loadToDo");
		//ServerInitializer.inheritVeriniceContextState();
		Iterator<Object> iterator = getSelection().getKeys();
        while (iterator.hasNext()) {
            Object key = iterator.next();
            table.setRowKey(key);
            if (table.isRowAvailable()) {
                setSelectedItem( (TodoViewItem) table.getRowData());
            }
        }

		ICommandService service = (ICommandService) VeriniceContext.get(VeriniceContext.COMMAND_SERVICE);
		if(getSelectedItem()!=null) {
			int massnahmeId = getSelectedItem().getdbId();
			LoadCnAElementById command = new LoadCnAElementById(MassnahmenUmsetzung.class,massnahmeId);
			try {
				service.executeCommand(command);
			} catch (CommandException e) {
				LOG.error("Error while loading todos for id: " + massnahmeId, e);
			}
			MassnahmenUmsetzung result = (MassnahmenUmsetzung) command.getFound();
			if(result==null) {
				LOG.warn("No massnahme found with id: " + massnahmeId);
			} else if(LOG.isDebugEnabled()) {
				LOG.debug("Massnahme loaded, id: " + massnahmeId);
			}
			setMassnahmeUmsetzung(result);
			
		}
		else {
			LOG.warn("No todo-item selected. Can not load massnahme.");
		}
	}

	public void save() {
		LOG.debug("save");
		ICommandService service = (ICommandService) VeriniceContext.get(VeriniceContext.COMMAND_SERVICE);
		if(getMassnahmeUmsetzung()!=null) {
			SaveElement<MassnahmenUmsetzung> command = new SaveElement<MassnahmenUmsetzung>(getMassnahmeUmsetzung());
			try {
				service.executeCommand(command);
				if(LOG.isDebugEnabled()) {
					LOG.debug("Massnahme saved, id: " + getMassnahmeUmsetzung().getDbId());
				}
				ToDoBean.addInfo("submit", getMessage("todo.saved"));
				loadToDoListForElement();
			} catch (CommandException e) {
				LOG.error("Error while saving massnahme: " + getMassnahmeUmsetzung().getDbId(), e);
			}		
		}
		else {
			LOG.warn("Massnahme is null. Can not save massnahme.");
		}
	}
	
	public Locale getLocale() {
		return FacesContext.getCurrentInstance().getViewRoot().getLocale();
	}
	
	public TimeZone getTimeZone() {
		return TimeZone.getDefault();
	}
	
	public void setItVerbundList(List<ITVerbund> itVerbundList) {
		this.itVerbundList = itVerbundList;
	}

	public List<ITVerbund> getItVerbundList() {
		return itVerbundList;
	}

	public Selection getSelection()
    {
        return selection;
    }

    public void setSelection(Selection selection)
    {
        this.selection = selection;
    } 
    
    public void setTable(HtmlExtendedDataTable table) {
        this.table = table;
    }

    public HtmlExtendedDataTable getTable() {
        return table;
    }
	
	public void setSelectedItVerbund(ITVerbund selectedItVerbund) {
		this.selectedItVerbund = selectedItVerbund;
	}

	public ITVerbund getSelectedItVerbund() {
		return selectedItVerbund;
	}

	public String getSelectedItVerbundTitel() {
		return selectedItVerbundTitel;
	}

	public void setSelectedItVerbundTitel(String selectedItVerbundId) {
		this.selectedItVerbundTitel = selectedItVerbundId;
	}

	public Integer getSelectedElementId() {
		return selectedElementId;
	}

	public void setSelectedElementId(Integer selectedElementId) {
		this.selectedElementId = selectedElementId;
	}

	public String getUmsetzung() {
		String umsetzung = null;
		if(getMassnahmeUmsetzung()!=null) {
			umsetzung=getMassnahmeUmsetzung().getUmsetzung();
		}
		return umsetzung;
	}
	
	public void setUmsetzung(String umsetzung) {
		if(getMassnahmeUmsetzung()!=null) {
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
	
	public List<CnATreeElement> getGebaeudeList() {
		return gebaeudeList;
	}

	public void setGebaeudeList(List<CnATreeElement> gebaeudeList) {
		this.gebaeudeList = gebaeudeList;
	}

	public List<CnATreeElement> getRaumList() {
		return raumList;
	}

	public void setRaumList(List<CnATreeElement> raumList) {
		this.raumList = raumList;
	}

	public List<CnATreeElement> getClienteList() {
		return clienteList;
	}

	public void setClienteList(List<CnATreeElement> clienteList) {
		this.clienteList = clienteList;
	}

	public List<CnATreeElement> getServerList() {
		return serverList;
	}

	public void setServerList(List<CnATreeElement> serverList) {
		this.serverList = serverList;
	}

	public List<CnATreeElement> getNetzList() {
		return netzList;
	}

	public void setNetzList(List<CnATreeElement> netzList) {
		this.netzList = netzList;
	}

	public List<CnATreeElement> getAnwendungList() {
		return anwendungList;
	}

	public void setAnwendungList(List<CnATreeElement> anwendungList) {
		this.anwendungList = anwendungList;
	}

	public List<CnATreeElement> getPersonList() {
		return personList;
	}

	public void setPersonList(List<CnATreeElement> personList) {
		this.personList = personList;
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

	public Converter getItVerbundConverter() {
		return itVerbundConverter;
	}

	public void setItVerbundConverter(Converter itVerbundConverter) {
		this.itVerbundConverter = itVerbundConverter;
	}

	public List<String> getExecutionList() {
		return executionList;
	}

	public void setExecutionList(List<String> umsetzungList) {
		this.executionList = umsetzungList;
	}

	public boolean isExecutionNo() {
		return executionNo;
	}

	public void setExecutionNo(boolean executionNo) {
		this.executionNo = executionNo;
	}

	public boolean isExecutionYes() {
		return executionYes;
	}

	public void setExecutionYes(boolean executionYes) {
		this.executionYes = executionYes;
	}

	public boolean isExecutionPartly() {
		return executionPartly;
	}

	public void setExecutionPartly(boolean executionPartly) {
		this.executionPartly = executionPartly;
	}

	public boolean isExecutionDispensable() {
		return executionDispensable;
	}

	public void setExecutionDispensable(boolean executionDispensable) {
		this.executionDispensable = executionDispensable;
	}

	public boolean isExecutionUntreated() {
		return executionUntreated;
	}

	public void setExecutionUntreated(boolean executionUntreated) {
		this.executionUntreated = executionUntreated;
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

	public boolean isSealA() {
		return sealA;
	}

	public void setSealA(boolean sealA) {
		this.sealA = sealA;
	}

	public boolean isSealB() {
		return sealB;
	}

	public void setSealB(boolean sealB) {
		this.sealB = sealB;
	}

	public boolean isSealC() {
		return sealC;
	}

	public void setSealC(boolean sealC) {
		this.sealC = sealC;
	}

	public boolean isSealZ() {
		return sealZ;
	}

	public void setSealZ(boolean sealZ) {
		this.sealZ = sealZ;
	}

	public int getSize() {
		return getTodoList()==null ? 0 : getTodoList().size();
	}
	
	public static void english() {
		FacesContext context = FacesContext.getCurrentInstance();
        context.getViewRoot().setLocale(Locale.ENGLISH);
	}
	
	public static void german() {
		FacesContext context = FacesContext.getCurrentInstance();
        context.getViewRoot().setLocale(Locale.GERMAN);
	}
	
	private static void addInfo(String componentId, String text ) {
		addMessage(componentId, text, FacesMessage.SEVERITY_INFO );
	}
	
	private static void addError(String componentId, String text ) {
		addMessage(componentId, text, FacesMessage.SEVERITY_ERROR );
	}
	
	private static void addMessage(String componentId, String text, Severity severity ) {
		 FacesMessage message = new FacesMessage(severity, text, null);
         FacesContext context = FacesContext.getCurrentInstance();
         UIComponent component = findComponent(context.getViewRoot(), componentId);
         context.addMessage(component.getClientId(context), message);

	}
	
	private static UIComponent findComponent(UIComponent parent, String id) {
		UIComponent component = null;
		if (id.equals(parent.getId())) {
			component = parent;
		} else {
			Iterator<UIComponent> kids = parent.getFacetsAndChildren();
			while (kids.hasNext()) {
				UIComponent kid = kids.next();
				UIComponent found = findComponent(kid, id);
				if (found != null) {
					component = found;
					break;
				}
			}
		}
		return component;
	}
	
	protected static ClassLoader getCurrentClassLoader(Object defaultObject){	
		ClassLoader loader = Thread.currentThread().getContextClassLoader();	
		if(loader == null){
			loader = defaultObject.getClass().getClassLoader();
		}
	
		return loader;
	}
	
	public static String getMessage(String key) {
		return getMessage(key, null);
	}

	public static String getMessage(String key,Object params[]){
		String text = null;
		Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
		ResourceBundle bundle = ResourceBundle.getBundle(
				BOUNDLE_NAME, 
				locale);	
		try{
			text = bundle.getString(key);
		} catch(MissingResourceException e){
			text = "? " + key + " ?";
		}
		
		if(params != null){
			MessageFormat mf = new MessageFormat(text, locale);
			text = mf.format(params, new StringBuffer(), null).toString();
		}	
		return text;
	}

}