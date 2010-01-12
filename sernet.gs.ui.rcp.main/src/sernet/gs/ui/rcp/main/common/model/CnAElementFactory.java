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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.model.Baustein;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.model.Anwendung;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.BausteinVorschlag;
import sernet.gs.ui.rcp.main.bsi.model.Client;
import sernet.gs.ui.rcp.main.bsi.model.Gebaeude;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.bsi.model.NetzKomponente;
import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.bsi.model.Raum;
import sernet.gs.ui.rcp.main.bsi.model.Server;
import sernet.gs.ui.rcp.main.bsi.model.SonstIT;
import sernet.gs.ui.rcp.main.bsi.model.SubtypenZielobjekte;
import sernet.gs.ui.rcp.main.bsi.model.TelefonKomponente;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.CreateAnwendung;
import sernet.gs.ui.rcp.main.service.crudcommands.CreateITVerbund;
import sernet.gs.ui.rcp.main.service.crudcommands.UpdateMultipleElements;
import sernet.gs.ui.rcp.main.service.migrationcommands.DbVersion;

/**
 * Factory for all model elements. Contains typed factories for sub-elements.
 * 
 * 
 * To add new model types: - add new class with new type-id (String) - add
 * type-id to Hitro-UI XML Config (SNCA.xml) - add a factory for the type-id
 * here - add the type to hibernate's cnatreeelement.hbm.xml - don't forget to
 * change the method canContain() in the parent to include the new type - add
 * Actions (add, delete) to plugin.xml - create ActionDelegates which use this
 * factory to create new instances - register editor for type in EditorFactory
 * 
 * @author koderman@sernet.de
 * 
 */
public class CnAElementFactory {

	private static List<IModelLoadListener> listeners = new ArrayList<IModelLoadListener>();

	private static CnAElementFactory instance;

	private HashMap<String, IElementBuilder> bsiElementbuilders = new HashMap<String, IElementBuilder>();

	private CnAElementHome dbHome;

	private static BSIModel loadedModel;

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

		bsiElementbuilders.put(Gebaeude.TYPE_ID, new IElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				Gebaeude child = dbHome.save(container, Gebaeude.class);
				container.addChild(child);
				child.setParent(container);
				return child;
			}
		});

		bsiElementbuilders.put(Client.TYPE_ID, new IElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				Client child = dbHome.save(container, Client.class);
				container.addChild(child);
				child.setParent(container);
				return child;
			}
		});

		bsiElementbuilders.put(SonstIT.TYPE_ID, new IElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				SonstIT child = dbHome.save(container, SonstIT.class);
				container.addChild(child);
				child.setParent(container);
				return child;
			}
		});

		bsiElementbuilders.put(Server.TYPE_ID, new IElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				Server child = dbHome.save(container, Server.class);
				container.addChild(child);
				child.setParent(container);
				return child;
			}
		});

		bsiElementbuilders.put(TelefonKomponente.TYPE_ID, new IElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				TelefonKomponente child = dbHome.save(container, TelefonKomponente.class);
				container.addChild(child);
				child.setParent(container);
				return child;
			}
		});

		bsiElementbuilders.put(Raum.TYPE_ID, new IElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				Raum child = dbHome.save(container, Raum.class);
				container.addChild(child);
				child.setParent(container);
				return child;
			}
		});

		bsiElementbuilders.put(NetzKomponente.TYPE_ID, new IElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				NetzKomponente child = dbHome.save(container, NetzKomponente.class);
				container.addChild(child);
				child.setParent(container);
				return child;
			}
		});

		bsiElementbuilders.put(Person.TYPE_ID, new IElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				Person child = dbHome.save(container, Person.class);
				container.addChild(child);
				child.setParent(container);
				return child;
			}
		});

		bsiElementbuilders.put(Anwendung.TYPE_ID, new IElementBuilder() {
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

		bsiElementbuilders.put(BausteinUmsetzung.TYPE_ID, new IElementBuilder<BausteinUmsetzung, Baustein>() {
			public BausteinUmsetzung build(CnATreeElement container, BuildInput<Baustein> input) throws Exception {

				BausteinUmsetzung bu = dbHome.save(container, input.getInput());
				if (bu == null)
					return null;

				container.addChild(bu);
				bu.setParent(container);
				return bu;
			}
		});

		bsiElementbuilders.put(ITVerbund.TYPE_ID, new IElementBuilder() {
			public ITVerbund build(CnATreeElement container, BuildInput input) throws Exception {

				Logger.getLogger(this.getClass()).debug("Creating new ITVerbund in " + container);
				CreateITVerbund saveCommand = new CreateITVerbund(container, ITVerbund.class);
				saveCommand = ServiceFactory.lookupCommandService().executeCommand(saveCommand);
				ITVerbund verbund = saveCommand.getNewElement();

				verbund.setParent(loadedModel);
				return verbund;
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
		IElementBuilder builder = bsiElementbuilders.get(buildableTypeId);
		if (builder == null)
			throw new Exception("Konnte Element nicht erzeugen.");
		CnATreeElement child = builder.build(container, input);

		// notify all listeners:
		if (fireUpdates) {
			getLoadedModel().childAdded(container, child);
			CnAElementFactory.getLoadedModel().databaseChildAdded(child);
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

	public BSIModel loadOrCreateModel(IProgress monitor) throws Exception {
		if (!dbHome.isOpen()) {
			dbHome.open(monitor);
		}

		monitor.setTaskName("Überprüfe / Aktualisiere DB-Version.");
		checkDbVersion();

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

	private void checkDbVersion() throws CommandException {
		final boolean[] done = new boolean[1];
		done[0] = false;
		Thread timeout = new Thread() {
			@Override
			public void run() {
				long startTime = System.currentTimeMillis();
				while (!done[0]) {
					try {
						sleep(1000);
						long now = System.currentTimeMillis();
						if (now - startTime > 30000) {
							ExceptionUtil.log(new Exception("Das hier dauert und dauert..."), 
									"Die Migration der Datenbank auf einen neue Version kann einige Zeit in Anspruch nehmen. Wenn diese Aktion länger als 5 " 
									+ "Minuten dauert, sollten Sie allerdings ihre Datenbank von Derby nach Postgres migrieren. Falls das " 
									+ "schon geschehen ist, sollten Sie ihre Postgres / MySQL-DB tunen. In der FAQ auf http://verinice.org/ finden "
									+ "Sie weitere Hinweise. Ab einer gewissen Größe des IT-Verbundes wird der Einsatz des Verinice-Servers " 
									+ "unverzichtbar. Auch hierzu finden Sie weitere Informationen auf unserer Webseite.");
							return;
						}
					} catch (InterruptedException e) {
					}
				}
			}
		};
		timeout.start();
		try {
			DbVersion command = new DbVersion(DbVersion.COMPATIBLE_CLIENT_VERSION);
			command = ServiceFactory.lookupCommandService().executeCommand(command);
			done[0] = true;
		} catch (CommandException e) {
			done[0] = true;
			throw e;
		} catch (RuntimeException re) {
			done[0] = true;
			throw re;
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

}
