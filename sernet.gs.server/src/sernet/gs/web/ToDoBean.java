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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.apache.log4j.Logger;
import org.richfaces.component.html.HtmlExtendedDataTable;
import org.richfaces.model.selection.Selection;
import org.richfaces.model.selection.SimpleSelection;

import sernet.gs.common.ApplicationRoles;
import sernet.gs.service.GSServiceException;
import sernet.gs.ui.rcp.main.bsi.model.GSScraperUtil;
import sernet.gs.ui.rcp.main.bsi.model.TodoViewItem;
import sernet.gs.ui.rcp.main.service.AuthenticationHelper;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementById;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCurrentUserConfiguration;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveElement;
import sernet.gs.ui.rcp.main.service.taskcommands.LoadChildrenAndMassnahmen;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Permission;
import sernet.verinice.model.common.configuration.Configuration;

/**
 * JSF managed bean for view ToDoList, template: todo/todo.xhtml
 * Asset-navigation data and methods are located in {@link AssetNavigationBean}.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ToDoBean {

	final static Logger LOG = Logger.getLogger(ToDoBean.class);
	
	public static final String BOUNDLE_NAME = "sernet.gs.web.Messages";
	
	AssetNavigationBean assetNavigation;
	
	private Selection selection = new SimpleSelection(); 
	
	private HtmlExtendedDataTable table;
	
	List<TodoViewItem> todoList = new ArrayList<TodoViewItem>();
	
	TodoViewItem selectedItem;
	
	MassnahmenUmsetzung massnahmeUmsetzung;
	
	Converter umsetzungConverter = new UmsetzungConverter();
	
	List<String> executionList;
	
	boolean showDescription = false;
	
	private Set<String> roles = null;
	
	private ICommandService commandService;
	
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
	}
	
	public void loadToDoList() {
		if(getAssetNavigation().getSelectedElementId()!=null && getAssetNavigation().getSelectedElementId()>0) {
			loadToDoListForElement();
		} else {
			loadToDoListForVerbund();
		}
	}
	
	public void loadToDoListForVerbund() {
		loadToDoList(AssetNavigationBean.SOURCE_VERBUND);
	}
	
	public void loadToDoListForElement() {
		loadToDoList(AssetNavigationBean.SOURCE_ELEMENT);
	}
	
	public void loadToDoList(int source) {
		// create command
		LoadChildrenAndMassnahmen command = new LoadChildrenAndMassnahmen();
		command.setExecutionSet(createExecutionSet());
		command.setSealSet(createSealSet());
		
		// execute command in AssetNavigationBean
		// to load children together with measures
		getAssetNavigation().loadChildren(command,source);
		
		// extract measures from command
		Set<TodoViewItem> massnahmenList = command.getMassnahmen();
		getTodoList().clear();	
		getTodoList().addAll(massnahmenList);
		Collections.sort(getTodoList());
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
		int massnahmeId = -1;
		try {
			Iterator<Object> iterator = getSelection().getKeys();
	        while (iterator.hasNext()) {
	            Object key = iterator.next();
	            table.setRowKey(key);
	            if (table.isRowAvailable()) {
	                setSelectedItem( (TodoViewItem) table.getRowData());
	            }
	        }
	
			if(getSelectedItem()!=null) {
				massnahmeId = getSelectedItem().getdbId();
				LoadCnAElementById command = new LoadCnAElementById(MassnahmenUmsetzung.TYPE_ID, massnahmeId);
				
					getCommandService().executeCommand(command);
					MassnahmenUmsetzung result = (MassnahmenUmsetzung) command.getFound();
					if(result==null) {
						LOG.warn("No massnahme found with id: " + massnahmeId);
					} else if(LOG.isDebugEnabled()) {
						LOG.debug("Massnahme loaded, id: " + massnahmeId);
					}
					setMassnahmeUmsetzung(result);
					showDescription = false;
			}
			else {
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
			// causes NoClassDefFoundError: org/eclipse/ui/plugin/AbstractUIPlugin
			// FIXME: fix this dependency to eclipse related classes.
			enabled = isWriteAllowed(getMassnahmeUmsetzung());
		}
		
		return enabled;
	}
	
	public boolean isWriteAllowed(CnATreeElement cte) {
        // Server implementation of CnAElementHome.isWriteAllowed
	    try {
            // Short cut: If no permission handling is needed than all objects are
            // writable.
            if (!ServiceFactory.isPermissionHandlingNeeded()) {
                return true;
            } 
            // Short cut 2: If we are the admin, then everything is writable as
            // well.
            if (AuthenticationHelper.getInstance().currentUserHasRole(new String[] { ApplicationRoles.ROLE_ADMIN })) {
                return true;
            }
    
            if (roles == null) {
                LoadCurrentUserConfiguration lcuc = new LoadCurrentUserConfiguration();       
                lcuc = getCommandService().executeCommand(lcuc);
                
                Configuration c = lcuc.getConfiguration();
                // No configuration for the current user (anymore?). Then nothing is
                // writable.
                if (c == null) {
                    return false;
                }
                roles = c.getRoles();
            }
    
            for (Permission p : cte.getPermissions()) {
                if (p.isWriteAllowed() && roles.contains(p.getRole())) {
                    return true;
                }
            }
	    } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
        return false;
    }

	public void save() {
		LOG.debug("save called...");
		int massnahmeId = -1;
		try {
			if(getMassnahmeUmsetzung()!=null) {
				if (!writeEnabled())
				{
					throw new SecurityException("write is not allowed" );
				}
				massnahmeId = getMassnahmeUmsetzung().getDbId();
				SaveElement<MassnahmenUmsetzung> command = new SaveElement<MassnahmenUmsetzung>(getMassnahmeUmsetzung());							
				getCommandService().executeCommand(command);
				if(LOG.isDebugEnabled()) {
					LOG.debug("Massnahme saved, id: " + massnahmeId);
				}
				loadToDoListForElement();	
				Util.addInfo("submit", Util.getMessage("todo.saved"));					
			}
			else {
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
		if(massnahme!=null) {
			try {
				text = GSScraperUtil.getInstanceWeb().getModel().getMassnahmeHtml(massnahme.getUrl(), massnahme.getStand());
			} catch (GSServiceException e) {
				LOG.error("Error while loading massnahme description.", e);
				Util.addError("submit", Util.getMessage("todo.load.failed"));
			}
		}
		if(text!=null) {
    		int start = text.indexOf("<div id=\"content\">");
    		int end = text.lastIndexOf("</body>");
    		if(start==-1 || end==-1) {
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
	
	public boolean getShowDescription() {
		return showDescription;
	}
	
	private ICommandService getCommandService() {
        if(commandService==null) {
            commandService = createCommandService();
        }
        return commandService;
    }
    
    private ICommandService createCommandService() {
        return(ICommandService) VeriniceContext.get(VeriniceContext.COMMAND_SERVICE);
    }

}