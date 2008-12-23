package sernet.gs.ui.rcp.main.common.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.hibernate.Transaction;

import sernet.gs.model.Baustein;
import sernet.gs.model.Massnahme;
import sernet.gs.ui.rcp.main.bsi.model.Anwendung;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.Client;
import sernet.gs.ui.rcp.main.bsi.model.ClientsKategorie;
import sernet.gs.ui.rcp.main.bsi.model.Gebaeude;
import sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.bsi.model.LinkKategorie;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.NetzKomponente;
import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.bsi.model.Raum;
import sernet.gs.ui.rcp.main.bsi.model.Server;
import sernet.gs.ui.rcp.main.bsi.model.SonstIT;
import sernet.gs.ui.rcp.main.bsi.model.SonstigeITKategorie;
import sernet.gs.ui.rcp.main.bsi.model.TelefonKomponente;
import sernet.gs.ui.rcp.main.common.model.migration.MigrateDbTo0_92;
import sernet.gs.ui.rcp.main.ds.model.Datenverarbeitung;
import sernet.gs.ui.rcp.main.ds.model.Personengruppen;
import sernet.gs.ui.rcp.main.ds.model.StellungnahmeDSB;
import sernet.gs.ui.rcp.main.ds.model.VerantwortlicheStelle;
import sernet.gs.ui.rcp.main.ds.model.Verarbeitungsangaben;
import sernet.hui.common.connect.Entity;

/**
 * Factory for all model elements. Contains typed factories for sub-elements.
 * 
 * 
 * To add new model types:
 * - add new class with new type-id (String)
 * - add type-id to Hitro-UI XML Config (SNCA.xml)
 * - add a factory for the type-id here
 * - add the type to hibernate's cnatreeelement.hbm.xml
 * - don't forget to change the method canContain() in the parent
 *   to include the new type
 * - add Actions (add, delete) to plugin.xml
 * - create ActionDelegates which use this factory to create new instances
 * - register editor for type in EditorFactory
 * 
 * @author koderman@sernet.de
 *
 */
public class CnAElementFactory {
	

	private static List<IModelLoadListener> listeners = new ArrayList<IModelLoadListener>();

	private static CnAElementFactory instance;

	private HashMap<String, IElementBuilder> bsiElementbuilders = 
		new HashMap<String, IElementBuilder>();

	private CnAElementHome dbHome;

	private static BSIModel loadedModel;
	

	private interface IElementBuilder<T extends CnATreeElement, U> {
		public T build(CnATreeElement container, BuildInput<U> input)
				throws Exception;
	}
	
	public void addLoadListener(IModelLoadListener listener) {
		if (! listeners.contains(listener))
			listeners.add(listener);
	}
	
	public void removeLoadListener(IModelLoadListener listener) {
		if (listeners.contains(listener))
			listeners.remove(listener);
	}

	
	private CnAElementFactory() {
		dbHome = CnAElementHome.getInstance();

		bsiElementbuilders.put(Gebaeude.TYPE_ID, new IElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws Exception {
				Gebaeude child = new Gebaeude(container);
				container.addChild(child);
				dbHome.save(child);
				return child;
			}
		});
		
		bsiElementbuilders.put(Client.TYPE_ID, new IElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws Exception {
				Client child = new Client(container);
				container.addChild(child);
				dbHome.save(child);
				return child;
			}
		});
		
		bsiElementbuilders.put(SonstIT.TYPE_ID, new IElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws Exception {
				SonstIT child = new SonstIT(container);
				container.addChild(child);
				dbHome.save(child);
				return child;
			}
		});
		
		bsiElementbuilders.put(Server.TYPE_ID, new IElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws Exception {
				Server child = new Server(container);
				container.addChild(child);
				dbHome.save(child);
				return child;
			}
		});
		
		bsiElementbuilders.put(TelefonKomponente.TYPE_ID, new IElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws Exception {
				TelefonKomponente child = new TelefonKomponente(container);
				container.addChild(child);
				dbHome.save(child);
				return child;
			}
		});
		
		bsiElementbuilders.put(Raum.TYPE_ID, new IElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws Exception {
				Raum child = new Raum(container);
				container.addChild(child);
				dbHome.save(child);
				return child;
			}
		});
		
		bsiElementbuilders.put(NetzKomponente.TYPE_ID, new IElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws Exception {
				NetzKomponente child = new NetzKomponente(container);
				container.addChild(child);
				dbHome.save(child);
				return child;
			}
		});

		bsiElementbuilders.put(Person.TYPE_ID, new IElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws Exception {
				Person child = new Person(container);
				container.addChild(child);
				dbHome.save(child);
				return child;
			}
		});
		
		bsiElementbuilders.put(Anwendung.TYPE_ID, new IElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws Exception {
				Anwendung child = new Anwendung(container);
				container.addChild(child);
				
				
				// add datenschutz elements:
				saveNew(child, Verarbeitungsangaben.TYPE_ID, null);
				saveNew(child, VerantwortlicheStelle.TYPE_ID, null);
				saveNew(child, Personengruppen.TYPE_ID, null);
				saveNew(child, Datenverarbeitung.TYPE_ID, null);
				saveNew(child, StellungnahmeDSB.TYPE_ID, null);
				
				dbHome.save(child);
				return child;
			}
		});

		bsiElementbuilders.put(StellungnahmeDSB.TYPE_ID, new IElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws Exception {
				StellungnahmeDSB child = new StellungnahmeDSB(container);
				container.addChild(child);
				//dbHome.save(child);
				return child;
			}
		});
		
		bsiElementbuilders.put(Datenverarbeitung.TYPE_ID, new IElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws Exception {
				Datenverarbeitung child = new Datenverarbeitung(container);
				container.addChild(child);
				//dbHome.save(child);
				return child;
			}
		});
		
		bsiElementbuilders.put(Personengruppen.TYPE_ID, new IElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws Exception {
				Personengruppen child = new Personengruppen(container);
				container.addChild(child);
				//dbHome.save(child);
				return child;
			}
		});
		
		bsiElementbuilders.put(Verarbeitungsangaben.TYPE_ID, new IElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws Exception {
				Verarbeitungsangaben child = new Verarbeitungsangaben(container);
				container.addChild(child);
				//dbHome.save(child);
				return child;
			}
		});
		
		bsiElementbuilders.put(VerantwortlicheStelle.TYPE_ID, new IElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws Exception {
				VerantwortlicheStelle child = new VerantwortlicheStelle(container);
				container.addChild(child);
				//dbHome.save(child);
				return child;
			}
		});

		bsiElementbuilders.put(BausteinUmsetzung.TYPE_ID,
				new IElementBuilder<BausteinUmsetzung, Baustein>() {
					public BausteinUmsetzung build(CnATreeElement container,
							BuildInput<Baustein> input) throws Exception {
						
						if (container.containsBausteinUmsetzung(input.getInput().getId()))
							return null;
						
						BausteinUmsetzung bu = new BausteinUmsetzung(container);
						bu.setKapitel(input.getInput().getId());
						bu.setName(input.getInput().getTitel());
						bu.setUrl(input.getInput().getUrl());
						bu.setStand(input.getInput().getStand());
						container.addChild(bu);

						List<Massnahme> massnahmen = input.getInput()
								.getMassnahmen();
						for (Massnahme mn : massnahmen) {
							saveNew(bu, MassnahmenUmsetzung.TYPE_ID,
									new BuildInput<Massnahme>(mn));
						}
						dbHome.save(bu);
						return bu;
					}
				});

		bsiElementbuilders.put(MassnahmenUmsetzung.TYPE_ID,
				new IElementBuilder<MassnahmenUmsetzung, Massnahme>() {
					public MassnahmenUmsetzung build(CnATreeElement container,
							BuildInput<Massnahme> input) throws Exception {
						
						MassnahmenUmsetzung mu = new MassnahmenUmsetzung(container);
						mu.setKapitel(input.getInput().getId());
						mu.setUrl(input.getInput().getUrl());
						mu.setName(input.getInput().getTitel());
						mu.setLebenszyklus(input.getInput().getLZAsString());
						mu.setStufe(input.getInput().getSiegelstufe());
						mu.setStand(input.getInput().getStand());
						mu.setVerantwortlicheRollenInitiierung(input.getInput().getVerantwortlichInitiierung());
						mu.setVerantwortlicheRollenUmsetzung(input.getInput().getVerantwortlichUmsetzung());
						container.addChild(mu);
						//dbHome.save(mu); // do not save on its own, instead bulk insert using parent
						return mu;
					}
				});
		
		bsiElementbuilders.put(ITVerbund.TYPE_ID,
				new IElementBuilder() {
					public ITVerbund build(CnATreeElement container,
							BuildInput input) throws Exception {
						
						ITVerbund verbund = new ITVerbund(loadedModel);
						loadedModel.addChild(verbund);
						dbHome.save(verbund);
						
						verbund.createNewCategories();
						
						for (CnATreeElement kategorie : verbund.getChildren()) {
							dbHome.save(kategorie);
						}
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
	public CnATreeElement saveNew(CnATreeElement container,
			String buildableTypeId, BuildInput input) throws Exception {
		IElementBuilder builder = bsiElementbuilders.get(buildableTypeId);
		if (builder == null)
			throw new Exception("Konnte Element nicht erzeugen.");
		CnATreeElement child = builder.build(container, input);
		return child;
	}
	
	public static BSIModel getLoadedModel() {
		return loadedModel;
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

	public BSIModel loadOrCreateModel(IProgress monitor) throws Exception {
		if (!dbHome.isOpen()) {
			dbHome.open(monitor);
		}
		loadedModel = dbHome.loadModel(monitor);
		if (loadedModel != null) {
			monitor.setTaskName("Überprüfe / Aktualisiere DB-Version.");
			DbVersion version = new DbVersion(loadedModel, dbHome);
			version.updateDBVersion(monitor);
			fireLoad();
			return loadedModel;
		}

		// none found, create new model:
		Logger.getLogger(this.getClass()).debug("Creating new model in DB.");
		monitor.setTaskName("Erzeuge neues Modell...");
		loadedModel = new BSIModel();
		dbHome.save(loadedModel);

		ITVerbund verbund = new ITVerbund(loadedModel);
		loadedModel.addChild(verbund);
		dbHome.save(verbund);

		verbund.createNewCategories();

		for (CnATreeElement kategorie : verbund.getChildren()) {
			dbHome.save(kategorie);
		}

		fireLoad();
		return loadedModel;
	}

	

	public void create(String typeId, CnATreeElement itverbund) {
		
	}

}
