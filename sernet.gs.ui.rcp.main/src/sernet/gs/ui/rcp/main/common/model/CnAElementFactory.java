/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
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
package sernet.gs.ui.rcp.main.common.model;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

import sernet.gs.model.Baustein;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.model.Anwendung;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.BausteinVorschlag;
import sernet.gs.ui.rcp.main.bsi.model.Client;
import sernet.gs.ui.rcp.main.bsi.model.Gebaeude;
import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturElement;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.bsi.model.NetzKomponente;
import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.bsi.model.Raum;
import sernet.gs.ui.rcp.main.bsi.model.Server;
import sernet.gs.ui.rcp.main.bsi.model.SonstIT;
import sernet.gs.ui.rcp.main.bsi.model.SubtypenZielobjekte;
import sernet.gs.ui.rcp.main.bsi.model.TelefonKomponente;
import sernet.gs.ui.rcp.main.bsi.views.actions.BSIModelViewOpenDBAction.StatusResult;
import sernet.gs.ui.rcp.main.service.ICommandService;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.CreateAnwendung;
import sernet.gs.ui.rcp.main.service.crudcommands.CreateITVerbund;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveElement;
import sernet.gs.ui.rcp.main.service.crudcommands.UpdateMultipleElements;
import sernet.verinice.iso27k.command.LoadModel;
import sernet.verinice.iso27k.model.Asset;
import sernet.verinice.iso27k.model.AssetGroup;
import sernet.verinice.iso27k.model.Audit;
import sernet.verinice.iso27k.model.AuditGroup;
import sernet.verinice.iso27k.model.Control;
import sernet.verinice.iso27k.model.ControlGroup;
import sernet.verinice.iso27k.model.ExceptionGroup;
import sernet.verinice.iso27k.model.IISO27kElement;
import sernet.verinice.iso27k.model.IISO27kRoot;
import sernet.verinice.iso27k.model.ISO27KModel;
import sernet.verinice.iso27k.model.Organization;
import sernet.verinice.iso27k.model.PersonGroup;
import sernet.verinice.iso27k.model.Requirement;
import sernet.verinice.iso27k.model.RequirementGroup;

/**
 * Factory for all model elements. Contains typed factories for sub-elements.
 * 
 * 
 * To add new model types see:
 * 
 * http://www.verinice.org/priv/mediawiki-1.6.12/index.php/Entities
 * 
 * - add new class with new type-id (String) 
 * - add  type-id to Hitro-UI XML Config (SNCA.xml) 
 * - add a factory for the type-id here 
 * - add the type to hibernate's cnatreeelement.hbm.xml 
 * - don't forget to change the method canContain() in the parent to include the new type 
 * - add Actions (add, delete) to plugin.xml 
 * - create ActionDelegates which use this factory to create new instances 
 * - register editor for type in EditorFactory
 * 
 * @author koderman@sernet.de
 * 
 */
public class CnAElementFactory {

	private final Logger log = Logger.getLogger(CnAElementFactory.class);
	
	private static List<IModelLoadListener> listeners = new ArrayList<IModelLoadListener>();

	private static CnAElementFactory instance;

	private HashMap<String, IElementBuilder> elementbuilders = new HashMap<String, IElementBuilder>();

	private CnAElementHome dbHome;

	private static BSIModel loadedModel;

	private static ISO27KModel isoModel;

	private ICommandService commandService;
	
	private interface IElementBuilder<T extends CnATreeElement, U> {
		public T build(CnATreeElement container, BuildInput<U> input) throws Exception;
	}

	public void addLoadListener(IModelLoadListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	public void removeLoadListener(IModelLoadListener listener) {
		if (listeners.contains(listener))
			listeners.remove(listener);
	}

	private CnAElementFactory() {
		dbHome = CnAElementHome.getInstance();

		elementbuilders.put(Gebaeude.TYPE_ID, new IElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				Gebaeude child = dbHome.save(container, Gebaeude.class);
				container.addChild(child);
				child.setParent(container);
				return child;
			}
		});

		elementbuilders.put(Client.TYPE_ID, new IElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				Client child = dbHome.save(container, Client.class);
				container.addChild(child);
				child.setParent(container);
				return child;
			}
		});

		elementbuilders.put(SonstIT.TYPE_ID, new IElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				SonstIT child = dbHome.save(container, SonstIT.class);
				container.addChild(child);
				child.setParent(container);
				return child;
			}
		});

		elementbuilders.put(Server.TYPE_ID, new IElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				Server child = dbHome.save(container, Server.class);
				container.addChild(child);
				child.setParent(container);
				return child;
			}
		});

		elementbuilders.put(TelefonKomponente.TYPE_ID, new IElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				TelefonKomponente child = dbHome.save(container, TelefonKomponente.class);
				container.addChild(child);
				child.setParent(container);
				return child;
			}
		});

		elementbuilders.put(Raum.TYPE_ID, new IElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				Raum child = dbHome.save(container, Raum.class);
				container.addChild(child);
				child.setParent(container);
				return child;
			}
		});

		elementbuilders.put(NetzKomponente.TYPE_ID, new IElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				NetzKomponente child = dbHome.save(container, NetzKomponente.class);
				container.addChild(child);
				child.setParent(container);
				return child;
			}
		});

		elementbuilders.put(Person.TYPE_ID, new IElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				Person child = dbHome.save(container, Person.class);
				container.addChild(child);
				child.setParent(container);
				return child;
			}
		});

		elementbuilders.put(Anwendung.TYPE_ID, new IElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {

				Logger.getLogger(this.getClass()).debug("Creating new Anwendung in " + container);
				CreateAnwendung saveCommand = new CreateAnwendung(container, Anwendung.class);
				saveCommand = ServiceFactory.lookupCommandService().executeCommand(saveCommand);
				Anwendung child = saveCommand.getNewElement();

				container.addChild(child);
				child.setParent(container);

				return child;
			}
		});

		elementbuilders.put(BausteinUmsetzung.TYPE_ID, new IElementBuilder<BausteinUmsetzung, Baustein>() {
			public BausteinUmsetzung build(CnATreeElement container, BuildInput<Baustein> input) throws Exception {

				BausteinUmsetzung bu = dbHome.save(container, input.getInput());
				if (bu == null)
					return null;

				container.addChild(bu);
				bu.setParent(container);
				return bu;
			}
		});

		elementbuilders.put(ITVerbund.TYPE_ID, new IElementBuilder() {
			public ITVerbund build(CnATreeElement container, BuildInput input) throws Exception {

				Logger.getLogger(this.getClass()).debug("Creating new ITVerbund in " + container);
				CreateITVerbund saveCommand = new CreateITVerbund(container, ITVerbund.class);
				saveCommand = ServiceFactory.lookupCommandService().executeCommand(saveCommand);
				ITVerbund verbund = saveCommand.getNewElement();

				verbund.setParent(loadedModel);
				return verbund;
			}
		});

		// ISO 27000 builders
		elementbuilders.put(Organization.TYPE_ID, new IElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				Organization child = dbHome.save(container, Organization.class);
				container.addChild(child);
				child.setParent(container);
				return child;
			}
		});
		
		elementbuilders.put(AssetGroup.TYPE_ID, new IElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				AssetGroup child = dbHome.save(container, AssetGroup.class);
				container.addChild(child);
				child.setParent(container);
				return child;
			}
		});
		elementbuilders.put(Asset.TYPE_ID, new IElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				Asset child = dbHome.save(container, Asset.class);
				container.addChild(child);
				child.setParent(container);
				return child;
			}
		});
		
		elementbuilders.put(PersonGroup.TYPE_ID, new IElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				PersonGroup child = dbHome.save(container, PersonGroup.class);
				container.addChild(child);
				child.setParent(container);
				return child;
			}
		});
		elementbuilders.put(sernet.verinice.iso27k.model.PersonIso.TYPE_ID, new IElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				sernet.verinice.iso27k.model.PersonIso child = dbHome.save(container, sernet.verinice.iso27k.model.PersonIso.class);
				container.addChild(child);
				child.setParent(container);
				return child;
			}
		});
		
		elementbuilders.put(AuditGroup.TYPE_ID, new IElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				AuditGroup child = dbHome.save(container, AuditGroup.class);
				container.addChild(child);
				child.setParent(container);
				return child;
			}
		});
		elementbuilders.put(Audit.TYPE_ID, new IElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				Audit child = dbHome.save(container, Audit.class);
				container.addChild(child);
				child.setParent(container);
				return child;
			}
		});
			
		elementbuilders.put(ControlGroup.TYPE_ID, new IElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				ControlGroup child = dbHome.save(container, ControlGroup.class);
				container.addChild(child);
				child.setParent(container);
				return child;
			}
		});
		elementbuilders.put(Control.TYPE_ID, new IElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				Control child = dbHome.save(container, Control.class);
				container.addChild(child);
				child.setParent(container);
				return child;
			}
		});
		
		elementbuilders.put(ExceptionGroup.TYPE_ID, new IElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				ExceptionGroup child = dbHome.save(container, ExceptionGroup.class);
				container.addChild(child);
				child.setParent(container);
				return child;
			}
		});
		elementbuilders.put(sernet.verinice.iso27k.model.Exception.TYPE_ID, new IElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				sernet.verinice.iso27k.model.Exception child = dbHome.save(container, sernet.verinice.iso27k.model.Exception.class);
				container.addChild(child);
				child.setParent(container);
				return child;
			}
		});
		
		elementbuilders.put(RequirementGroup.TYPE_ID, new IElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				RequirementGroup child = dbHome.save(container, RequirementGroup.class);
				container.addChild(child);
				child.setParent(container);
				return child;
			}
		});
		elementbuilders.put(Requirement.TYPE_ID, new IElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				Requirement child = dbHome.save(container, Requirement.class);
				container.addChild(child);
				child.setParent(container);
				return child;
			}
		});
		
	}

	public static CnAElementFactory getInstance() {
		if (instance == null)
			instance = new CnAElementFactory();
		return instance;
	}

	/**
	 * Create new BSI element with new HUI Entity. The HUI Entity will be added
	 * to the given container.
	 * 
	 * @param container
	 * @return the newly added element
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public CnATreeElement saveNew(CnATreeElement container, String buildableTypeId, BuildInput input, boolean fireUpdates) throws Exception {
		IElementBuilder builder = elementbuilders.get(buildableTypeId);
		if (builder == null) {
			throw new Exception("Konnte Element nicht erzeugen.");
		}
		CnATreeElement child = builder.build(container, input);

		// notify all listeners:
		if (fireUpdates) {
			CnAElementFactory.getModel(child).childAdded(container, child);
			CnAElementFactory.getModel(child).databaseChildAdded(child);
		}
		return child;
	}

	public CnATreeElement saveNew(CnATreeElement container, String buildableTypeId, BuildInput input) throws Exception {
		return saveNew(container, buildableTypeId, input, true);
	}

	public static BSIModel getLoadedModel() {
		return loadedModel;
	}

	public static boolean isModelLoaded() {
		return (loadedModel != null);
	}

	public void closeModel() {
		dbHome.close();
		fireClosed();
		loadedModel = null;
	}

	private void fireClosed() {
		for (IModelLoadListener listener : listeners) {
			listener.closed(loadedModel);
		}
	}

	private void fireLoad() {
		for (IModelLoadListener listener : listeners) {
			listener.loaded(loadedModel);
		}
	}

	/**
	 * Returns whether there is an active database connection.
	 * 
	 * @return
	 */
	public boolean isDbOpen() {
		return dbHome != null && dbHome.isOpen();
	}
	
	/**
	 * Returns the model for a {@link CnATreeElement}
	 * 
	 * @param element returned model belongs to this element
	 * @return the model for an element
	 */
	public static CnATreeElement getModel(CnATreeElement element) {
		CnATreeElement model = null;
		if(element instanceof IBSIStrukturElement) {
			model = CnAElementFactory.getLoadedModel();
		}
		if(element instanceof IISO27kElement) {
			model = CnAElementFactory.getInstance().getISO27kModel();
		}
		return model;
	}
	
	public ISO27KModel getISO27kModel() {
		if(isoModel==null) {
			isoModel = loadIsoModel();
			if(isoModel==null) {
				isoModel = createIsoModel();
			}
		}
		return isoModel;
	}

	/**
	 * @return
	 */
	private ISO27KModel loadIsoModel() {
		try {
			CnAWorkspace.getInstance().createDatabaseConfig();
			
			Activator.inheritVeriniceContextState();
			Activator.checkDbVersion();
			
			LoadModel loadModel = new LoadModel();
			loadModel = getCommandService().executeCommand(loadModel);
			isoModel = loadModel.getModel();			
		} catch(Exception e) {
			log.error("Error while loading ISO27KModel", e);
		}
		return isoModel;
	}
	
	/**
	 * @return
	 */
	private ISO27KModel createIsoModel() {
		try {
			isoModel=new ISO27KModel();
			SaveElement<ISO27KModel> saveCommand = new SaveElement<ISO27KModel>(isoModel);
			saveCommand = getCommandService().executeCommand(saveCommand);
			isoModel = saveCommand.getElement();
			if (log.isInfoEnabled()) {
				log.info("ISO27KModel created");
			}
		} catch(Exception e) {
			log.error("Error while creating ISO27KModel", e);
		}
		return isoModel;
	}

	public BSIModel loadOrCreateModel(IProgress monitor) throws Exception {
		if (!dbHome.isOpen()) {
			dbHome.open(monitor);
		}

		monitor.setTaskName("Überprüfe / Aktualisiere DB-Version.");
		Activator.checkDbVersion();

		loadedModel = dbHome.loadModel(monitor);
		if (loadedModel != null) {

			fireLoad();
			return loadedModel;
		}

		// none found, create new model:
		Logger.getLogger(this.getClass()).debug("Creating new model in DB.");
		monitor.setTaskName("Erzeuge neues Modell...");
		loadedModel = new BSIModel();

		ITVerbund verbund = new ITVerbund(loadedModel);
		loadedModel.addChild(verbund);

		verbund.createNewCategories();

		createBausteinVorschlaege(loadedModel);

		loadedModel = dbHome.save(loadedModel);

		fireLoad();
		return loadedModel;
	}

	private void createBausteinVorschlaege(BSIModel newModel) {
		SubtypenZielobjekte mapping = new SubtypenZielobjekte();
		List<BausteinVorschlag> list = mapping.getMapping();
		UpdateMultipleElements<BausteinVorschlag> command = new UpdateMultipleElements<BausteinVorschlag>(list, ChangeLogEntry.STATION_ID, ChangeLogEntry.TYPE_INSERT);
		try {
			command = ServiceFactory.lookupCommandService().executeCommand(command);
		} catch (CommandException e) {
			throw new RuntimeCommandException(e);
		}
	}

	public void reloadModelFromDatabase() {
		try {
			fireClosed();
			BSIModel newModel = dbHome.loadModel(new NullMonitor());
			loadedModel.modelReload(newModel);
			loadedModel = newModel;
			fireLoad();
		} catch (Exception e) {
			ExceptionUtil.log(e, "Konnte Modell nicht aus der Datenbank erneuern..");
		}
	}
	
	private ICommandService getCommandService() {
		if(commandService==null) {
			commandService = createCommandService();
		}
		return commandService;
	}
	
	private ICommandService createCommandService() {
		try {
			ServiceFactory.openCommandService();
		} catch (MalformedURLException e) {
			log.error("Error while opening command service", e);
			throw new RuntimeException("Error while opening command service", e);
		}
		commandService = ServiceFactory.lookupCommandService();
		return commandService;
	}

}
