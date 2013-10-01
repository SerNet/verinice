/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
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
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.common.model;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import sernet.gs.model.Baustein;
import sernet.gs.service.RuntimeCommandException;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.bsi.model.SubtypenZielobjekte;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.CreateAnwendung;
import sernet.gs.ui.rcp.main.service.crudcommands.CreateITVerbund;
import sernet.gs.ui.rcp.main.service.crudcommands.UpdateMultipleElements;
import sernet.hui.common.connect.HitroUtil;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.iso27k.service.commands.CreateIsoModel;
import sernet.verinice.model.bsi.Anwendung;
import sernet.verinice.model.bsi.AnwendungenKategorie;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.BausteinVorschlag;
import sernet.verinice.model.bsi.Client;
import sernet.verinice.model.bsi.ClientsKategorie;
import sernet.verinice.model.bsi.Gebaeude;
import sernet.verinice.model.bsi.GebaeudeKategorie;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.MassnahmeKategorie;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.NKKategorie;
import sernet.verinice.model.bsi.NetzKomponente;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.bsi.PersonenKategorie;
import sernet.verinice.model.bsi.RaeumeKategorie;
import sernet.verinice.model.bsi.Raum;
import sernet.verinice.model.bsi.Server;
import sernet.verinice.model.bsi.ServerKategorie;
import sernet.verinice.model.bsi.SonstIT;
import sernet.verinice.model.bsi.SonstigeITKategorie;
import sernet.verinice.model.bsi.TKKategorie;
import sernet.verinice.model.bsi.TelefonKomponente;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.ds.Datenverarbeitung;
import sernet.verinice.model.ds.Personengruppen;
import sernet.verinice.model.ds.StellungnahmeDSB;
import sernet.verinice.model.ds.VerantwortlicheStelle;
import sernet.verinice.model.ds.Verarbeitungsangaben;
import sernet.verinice.model.ds.Zweckbestimmung;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.AssetGroup;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.AuditGroup;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.Document;
import sernet.verinice.model.iso27k.DocumentGroup;
import sernet.verinice.model.iso27k.Evidence;
import sernet.verinice.model.iso27k.EvidenceGroup;
import sernet.verinice.model.iso27k.ExceptionGroup;
import sernet.verinice.model.iso27k.Finding;
import sernet.verinice.model.iso27k.FindingGroup;
import sernet.verinice.model.iso27k.IISO27kElement;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.iso27k.Incident;
import sernet.verinice.model.iso27k.IncidentGroup;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.model.iso27k.IncidentScenarioGroup;
import sernet.verinice.model.iso27k.Interview;
import sernet.verinice.model.iso27k.InterviewGroup;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.iso27k.PersonGroup;
import sernet.verinice.model.iso27k.Process;
import sernet.verinice.model.iso27k.ProcessGroup;
import sernet.verinice.model.iso27k.Record;
import sernet.verinice.model.iso27k.RecordGroup;
import sernet.verinice.model.iso27k.Requirement;
import sernet.verinice.model.iso27k.RequirementGroup;
import sernet.verinice.model.iso27k.Response;
import sernet.verinice.model.iso27k.ResponseGroup;
import sernet.verinice.model.iso27k.Threat;
import sernet.verinice.model.iso27k.ThreatGroup;
import sernet.verinice.model.iso27k.Vulnerability;
import sernet.verinice.model.iso27k.VulnerabilityGroup;
import sernet.verinice.model.samt.SamtTopic;
import sernet.verinice.service.commands.CreateElement;
import sernet.verinice.service.iso27k.LoadModel;

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
 * @author koderman[at]sernet[dot]de
 * 
 */
public final class CnAElementFactory {

	private final Logger log = Logger.getLogger(CnAElementFactory.class);
	
	private Object mutex = new Object();
	
	private static List<IModelLoadListener> listeners = new CopyOnWriteArrayList<IModelLoadListener>();

	private static volatile CnAElementFactory instance;

	private Map<String, IElementBuilder> elementbuilders = new HashMap<String, IElementBuilder>();

	private CnAElementHome dbHome;

	private static BSIModel loadedModel;

	private static ISO27KModel isoModel;

	private ICommandService commandService;
	
	private static final String WARNING_UNCHECKED = "unchecked";
	private static final String WARNING_RAWTYPES = "rawtypes";

	private interface IElementBuilder<T extends CnATreeElement, U> {
		T build(CnATreeElement container, BuildInput<U> input)
				throws CommandException;
	}

	@SuppressWarnings(WARNING_RAWTYPES)
	private abstract class ElementBuilder implements IElementBuilder {
		protected void init(CnATreeElement container, CnATreeElement child) {
			container.addChild(child);
			child.setParentAndScope(container);
		}
	}

	public void addLoadListener(IModelLoadListener listener) {
		if (log.isDebugEnabled()) {
			log.debug("Adding model load listener.");
		}
		if (!listeners.contains(listener)){
			listeners.add(listener);
		}
		// safety: always fire one event when a loaded model is present,
		// because the model could have been loaded while the listener was in
		// the process of registering
		// himself here (race condition):
		if (loadedModel != null) {
			if (log.isDebugEnabled()) {
				log.debug("Firing safety event: bsi model loaded.");
			}
			listener.loaded(loadedModel);
		}
		if (isoModel != null) {
			if (log.isDebugEnabled()) {
				log.debug("Firing safety event: iso27k model");
			}
			listener.loaded(isoModel);
		}

	}

	public void removeLoadListener(IModelLoadListener listener) {
		if (listeners.contains(listener)){
			listeners.remove(listener);
		}
	}

	@SuppressWarnings(WARNING_RAWTYPES)
	private CnAElementFactory() {
		dbHome = CnAElementHome.getInstance();

		// Datenschutz Elemente
		elementbuilders.put(StellungnahmeDSB.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException{
				StellungnahmeDSB child = dbHome.save(container,
						StellungnahmeDSB.class, StellungnahmeDSB.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(Personengruppen.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				Personengruppen child = dbHome.save(container,
						Personengruppen.class, Personengruppen.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(Datenverarbeitung.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				Datenverarbeitung child = dbHome.save(container,
						Datenverarbeitung.class, Datenverarbeitung.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(Verarbeitungsangaben.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				Verarbeitungsangaben child = dbHome.save(container,
						Verarbeitungsangaben.class,
						Verarbeitungsangaben.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(Zweckbestimmung.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				Zweckbestimmung child = dbHome.save(container,
						Zweckbestimmung.class, Zweckbestimmung.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(VerantwortlicheStelle.TYPE_ID,
				new ElementBuilder() {
					public CnATreeElement build(CnATreeElement container,
							BuildInput input) throws CommandException {
						VerantwortlicheStelle child = dbHome.save(container,
								VerantwortlicheStelle.class,
								VerantwortlicheStelle.TYPE_ID);
						init(container, child);
						return child;
					}
				});

		// BSI Grundschutz elements

		elementbuilders.put(NKKategorie.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				NKKategorie child = dbHome.save(container, NKKategorie.class,
						NKKategorie.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(SonstigeITKategorie.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				SonstigeITKategorie child = dbHome.save(container,
						SonstigeITKategorie.class, SonstigeITKategorie.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(PersonenKategorie.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				PersonenKategorie child = dbHome.save(container,
						PersonenKategorie.class, PersonenKategorie.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(AnwendungenKategorie.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				AnwendungenKategorie child = dbHome.save(container,
						AnwendungenKategorie.class,
						AnwendungenKategorie.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(GebaeudeKategorie.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				GebaeudeKategorie child = dbHome.save(container,
						GebaeudeKategorie.class, GebaeudeKategorie.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(RaeumeKategorie.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				RaeumeKategorie child = dbHome.save(container,
						RaeumeKategorie.class, RaeumeKategorie.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(TKKategorie.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				TKKategorie child = dbHome.save(container, TKKategorie.class,
						TKKategorie.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(ServerKategorie.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				ServerKategorie child = dbHome.save(container,
						ServerKategorie.class, ServerKategorie.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(ClientsKategorie.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				ClientsKategorie child = dbHome.save(container,
						ClientsKategorie.class, ClientsKategorie.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(MassnahmeKategorie.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				MassnahmeKategorie child = dbHome.save(container,
						MassnahmeKategorie.class, MassnahmeKategorie.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(Gebaeude.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				Gebaeude child = dbHome.save(container, Gebaeude.class,
						Gebaeude.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(Client.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				Client child = dbHome.save(container, Client.class,
						Client.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(SonstIT.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				SonstIT child = dbHome.save(container, SonstIT.class,
						SonstIT.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(Server.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				Server child = dbHome.save(container, Server.class,
						Server.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(TelefonKomponente.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				TelefonKomponente child = dbHome.save(container,
						TelefonKomponente.class, TelefonKomponente.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(Raum.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				Raum child = dbHome.save(container, Raum.class, Raum.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(NetzKomponente.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				NetzKomponente child = dbHome.save(container,
						NetzKomponente.class, NetzKomponente.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(Person.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				Person child = dbHome.save(container, Person.class,
						Person.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(Anwendung.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {

				log.debug("Creating new Anwendung in " + container); //$NON-NLS-1$
				CreateAnwendung saveCommand = new CreateAnwendung(container,
						Anwendung.class);
				saveCommand = ServiceFactory.lookupCommandService()
						.executeCommand(saveCommand);
				Anwendung child = saveCommand.getNewElement();

				init(container, child);

				return child;
			}
		});

		elementbuilders.put(BausteinUmsetzung.TYPE_ID,
				new IElementBuilder<BausteinUmsetzung, Baustein>() {
					public BausteinUmsetzung build(CnATreeElement container,
							BuildInput<Baustein> input) throws CommandException {

						if (input == null) {
							BausteinUmsetzung child = dbHome.save(container,
									BausteinUmsetzung.class,
									BausteinUmsetzung.TYPE_ID);
							return child;
						} else {
							BausteinUmsetzung bu = dbHome.save(container, input.getInput());
							container.addChild(bu);
							bu.setParentAndScope(container);
							return bu;
							}
					}

		});

		elementbuilders.put(MassnahmenUmsetzung.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				MassnahmenUmsetzung child = dbHome.save(container,
						MassnahmenUmsetzung.class, MassnahmenUmsetzung.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(ITVerbund.TYPE_ID, new ElementBuilder() {
			public ITVerbund build(CnATreeElement container, BuildInput input)
					throws CommandException {

				log.debug("Creating new ITVerbund in " + container); //$NON-NLS-1$
				boolean createChildren = true;
				if (input != null) {
					createChildren = (Boolean) input.getInput();
				}
				CreateITVerbund saveCommand = new CreateITVerbund(container,
						ITVerbund.class, createChildren);
				saveCommand = ServiceFactory.lookupCommandService()
						.executeCommand(saveCommand);
				ITVerbund verbund = saveCommand.getNewElement();

				verbund.setParent(loadedModel);
				return verbund;
			}
		});

		// ISO 27000 builders
		elementbuilders.put(Organization.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				Organization child = dbHome.save(container, Organization.class,
						Organization.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(AssetGroup.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				AssetGroup child = dbHome.save(container, AssetGroup.class,
						AssetGroup.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		elementbuilders.put(Asset.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				Asset child = dbHome
						.save(container, Asset.class, Asset.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(PersonGroup.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				PersonGroup child = dbHome.save(container, PersonGroup.class,
						PersonGroup.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		elementbuilders.put(sernet.verinice.model.iso27k.PersonIso.TYPE_ID,
				new ElementBuilder() {
					public CnATreeElement build(CnATreeElement container,
							BuildInput input) throws CommandException {
						sernet.verinice.model.iso27k.PersonIso child = dbHome
								.save(container,
										sernet.verinice.model.iso27k.PersonIso.class,
										sernet.verinice.model.iso27k.PersonIso.TYPE_ID);
						init(container, child);
						return child;
					}
				});

		elementbuilders.put(AuditGroup.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				AuditGroup child = dbHome.save(container, AuditGroup.class,
						AuditGroup.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		elementbuilders.put(Audit.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				Audit child = dbHome
						.save(container, Audit.class, Audit.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(ControlGroup.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				ControlGroup child = dbHome.save(container, ControlGroup.class,
						ControlGroup.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		elementbuilders.put(Control.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				Control child = dbHome.save(container, Control.class,
						Control.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(ExceptionGroup.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				ExceptionGroup child = dbHome.save(container,
						ExceptionGroup.class, ExceptionGroup.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		elementbuilders.put(sernet.verinice.model.iso27k.Exception.TYPE_ID,
				new ElementBuilder() {
					public CnATreeElement build(CnATreeElement container,
							BuildInput input) throws CommandException {
						sernet.verinice.model.iso27k.Exception child = dbHome
								.save(container,
										sernet.verinice.model.iso27k.Exception.class,
										sernet.verinice.model.iso27k.Exception.TYPE_ID);
						init(container, child);
						return child;
					}
				});

		elementbuilders.put(RequirementGroup.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				RequirementGroup child = dbHome.save(container,
						RequirementGroup.class, RequirementGroup.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		elementbuilders.put(Requirement.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				Requirement child = dbHome.save(container, Requirement.class,
						Requirement.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(Incident.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				Incident child = dbHome.save(container, Incident.class,
						Incident.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		elementbuilders.put(IncidentGroup.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				IncidentGroup child = dbHome.save(container,
						IncidentGroup.class, IncidentGroup.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(IncidentScenario.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				IncidentScenario child = dbHome.save(container,
						IncidentScenario.class, IncidentScenario.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		elementbuilders.put(IncidentScenarioGroup.TYPE_ID,
				new ElementBuilder() {
					public CnATreeElement build(CnATreeElement container,
							BuildInput input) throws CommandException {
						IncidentScenarioGroup child = dbHome.save(container,
								IncidentScenarioGroup.class,
								IncidentScenarioGroup.TYPE_ID);
						init(container, child);
						return child;
					}
				});

		elementbuilders.put(Response.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				Response child = dbHome.save(container, Response.class,
						Response.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		elementbuilders.put(ResponseGroup.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				ResponseGroup child = dbHome.save(container,
						ResponseGroup.class, ResponseGroup.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(Threat.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				Threat child = dbHome.save(container, Threat.class,
						Threat.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		elementbuilders.put(ThreatGroup.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				ThreatGroup child = dbHome.save(container, ThreatGroup.class,
						ThreatGroup.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(Vulnerability.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				Vulnerability child = dbHome.save(container,
						Vulnerability.class, Vulnerability.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		elementbuilders.put(VulnerabilityGroup.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				VulnerabilityGroup child = dbHome.save(container,
						VulnerabilityGroup.class, VulnerabilityGroup.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(DocumentGroup.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				DocumentGroup child = dbHome.save(container,
						DocumentGroup.class, DocumentGroup.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		elementbuilders.put(Document.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				Document child = dbHome.save(container, Document.class,
						Document.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(InterviewGroup.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				InterviewGroup child = dbHome.save(container,
						InterviewGroup.class, InterviewGroup.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		elementbuilders.put(Interview.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				Interview child = dbHome.save(container, Interview.class,
						Interview.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(FindingGroup.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				FindingGroup child = dbHome.save(container, FindingGroup.class,
						FindingGroup.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		elementbuilders.put(Finding.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				Finding child = dbHome.save(container, Finding.class,
						Finding.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(EvidenceGroup.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				EvidenceGroup child = dbHome.save(container,
						EvidenceGroup.class, EvidenceGroup.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		elementbuilders.put(Evidence.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				Evidence child = dbHome.save(container, Evidence.class,
						Evidence.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(ProcessGroup.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				ProcessGroup child = dbHome.save(container, ProcessGroup.class,
						ProcessGroup.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		elementbuilders.put(Process.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				Process child = dbHome.save(container, Process.class,
						Process.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(RecordGroup.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				RecordGroup child = dbHome.save(container, RecordGroup.class,
						RecordGroup.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		elementbuilders.put(Record.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				Record child = dbHome.save(container, Record.class,
						Record.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		// Self Assessment (SAMT) builders

		elementbuilders.put(SamtTopic.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container,
					BuildInput input) throws CommandException {
				SamtTopic child = dbHome.save(container, SamtTopic.class,
						SamtTopic.TYPE_ID);
				init(container, child);
				return child;
			}
		});

	}

	public static CnAElementFactory getInstance() {
		if (instance == null){
			instance = new CnAElementFactory();
		}
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
	public CnATreeElement saveNewOrganisation(CnATreeElement container,
			boolean createChildren, boolean fireUpdates) throws CommandException {
		String title = HitroUtil.getInstance().getTypeFactory()
				.getMessage(Organization.TYPE_ID);
		CreateElement<Organization> saveCommand = new CreateElement<Organization>(
				container, Organization.class, title, false, createChildren);
		saveCommand = getCommandService().executeCommand(saveCommand);
		CnATreeElement child = saveCommand.getNewElement();
		container.addChild(child);
		child.setParentAndScope(container);
		// notify all listeners:
		if (fireUpdates) {
			CnAElementFactory.getModel(child).childAdded(container, child);
			CnAElementFactory.getModel(child).databaseChildAdded(child);
		}
		return child;
	}

	/**
	 * Create new BSI element with new HUI Entity. The HUI Entity will be added
	 * to the given container.
	 * 
	 * @param container
	 * @return the newly added element
	 * @throws Exception
	 */
	@SuppressWarnings({ WARNING_RAWTYPES, WARNING_UNCHECKED })
	public CnATreeElement saveNewAudit(CnATreeElement container,
			boolean createChildren, boolean fireUpdates) throws CommandException, CnATreeElementBuildException {
		IElementBuilder builder = elementbuilders.get(Audit.TYPE_ID);
		if (builder == null) {
			throw new CnATreeElementBuildException(
					Messages.getString("CnAElementFactory.0") + Audit.TYPE_ID); //$NON-NLS-1$
		}
		CnATreeElement child = builder.build(container, null);

		// notify all listeners:
		if (fireUpdates) {
			CnAElementFactory.getModel(child).childAdded(container, child);
			CnAElementFactory.getModel(child).databaseChildAdded(child);
		}
		if (createChildren) {
			CnAElementFactory.getInstance().saveNew(child, AssetGroup.TYPE_ID,
					null);
			CnAElementFactory.getInstance().saveNew(child,
					ControlGroup.TYPE_ID, null);
			CnAElementFactory.getInstance().saveNew(child, PersonGroup.TYPE_ID,
					null);
			CnAElementFactory.getInstance().saveNew(child,
					FindingGroup.TYPE_ID, null);
			CnAElementFactory.getInstance().saveNew(child,
					EvidenceGroup.TYPE_ID, null);
			CnAElementFactory.getInstance().saveNew(child,
					InterviewGroup.TYPE_ID, null);
		}
		return child;
	}

	/**
	 * Create new BSI element with new HUI Entity. The HUI Entity will be added
	 * to the given container.
	 * 
	 * @param container
	 * @return the newly added element
	 * @throws Exception
	 */
	@SuppressWarnings({WARNING_RAWTYPES, WARNING_UNCHECKED})
	public CnATreeElement saveNew(CnATreeElement container,
			String buildableTypeId, BuildInput input, boolean fireUpdates)
			throws CnATreeElementBuildException, CommandException {
		IElementBuilder builder = elementbuilders.get(buildableTypeId);
		if (builder == null) {
			log.error(Messages.getString("CnAElementFactory.0")
					+ buildableTypeId);
			throw new CnATreeElementBuildException(
					Messages.getString("CnAElementFactory.0") + buildableTypeId); //$NON-NLS-1$
		}
		CnATreeElement child = builder.build(container, input);

		// notify all listeners:
		if (fireUpdates) {
			CnAElementFactory.getModel(child).childAdded(container, child);
			CnAElementFactory.getModel(child).databaseChildAdded(child);
		}
		return child;
	}

	@SuppressWarnings(WARNING_RAWTYPES)
	public CnATreeElement saveNew(CnATreeElement container,
			String buildableTypeId, BuildInput input) throws CommandException, CnATreeElementBuildException {
		return saveNew(container, buildableTypeId, input, true);
	}

	public static BSIModel getLoadedModel() {
		return loadedModel;
	}

	public static boolean isModelLoaded() {
		return (loadedModel != null);
	}

	public static boolean isIsoModelLoaded() {
		return (isoModel != null);
	}

	public void closeModel() {
		dbHome.close();
		fireClosed();
		CnAElementFactory.dereferenceModel();
	}

    private static void dereferenceModel() {
        loadedModel = null;
    }

	private void fireClosed() {
		for (IModelLoadListener listener : listeners) {
			listener.closed(loadedModel);
		}
	}

	/**
	 * Method is called to inform listener when an {@link BSIModel} is loaded or
	 * created
	 */
	private void fireLoad() {
		for (IModelLoadListener listener : listeners) {
			listener.loaded(loadedModel);
		}
	}

	/**
	 * Method is called to inform listener when an ISO27KModel is loaded or
	 * created
	 * 
	 * If an {@link BSIModel} is created method fireLoad() is called
	 * 
	 * @param model
	 *            a new loaded or created {@link ISO27KModel}
	 */
	private void fireLoad(ISO27KModel model) {
		for (IModelLoadListener listener : listeners) {
			listener.loaded(model);
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
	 * @param element
	 *            returned model belongs to this element
	 * @return the model for an element
	 */
	public static CnATreeElement getModel(CnATreeElement element) {
		CnATreeElement model = null;
		if (element instanceof ISO27KModel || element instanceof IISO27kElement) {
			model = CnAElementFactory.getInstance().getISO27kModel();
		} else {
			model = CnAElementFactory.getLoadedModel();
		}
		return model;
	}

	public ISO27KModel getISO27kModel() {
		synchronized (mutex) {
			if (isoModel == null) {
				isoModel = loadIsoModel();
				if (isoModel == null) {
					createIsoModel();
				}
			}
			return isoModel;
		}
	}

	/**
	 * @return
	 */
	private ISO27KModel loadIsoModel() {
		ISO27KModel model = null;
		try {
			LoadModel loadModel = new LoadModel();
			loadModel = getCommandService().executeCommand(loadModel);
			model = loadModel.getModel();
			if (model != null) {
				fireLoad(model);
			}
		} catch (Exception e) {
			log.error(Messages.getString("CnAElementFactory.1"), e); //$NON-NLS-1$
			throw new RuntimeException(
					Messages.getString("CnAElementFactory.1"), e);
		}
		return model;
	}

	/**
	 * @return
	 */
	private void createIsoModel() {
		try {
			CreateIsoModel command = new CreateIsoModel();
			command = getCommandService().executeCommand(command);
			isoModel = command.getElement();
			if (log.isInfoEnabled()) {
				log.info("ISO27KModel created"); //$NON-NLS-1$
			}
			if (isoModel != null) {
				fireLoad(isoModel);
			}
		} catch (CommandException e) {
			log.error(Messages.getString("CnAElementFactory.2"), e); //$NON-NLS-1$
		}
	}

	public BSIModel loadOrCreateModel(IProgress monitor) throws MalformedURLException, CommandException, CnATreeElementBuildException {
		if (!dbHome.isOpen()) {
			dbHome.open(monitor);
		}

		monitor.setTaskName(Messages.getString("CnAElementFactory.3")); //$NON-NLS-1$
		Activator.checkDbVersion();

		loadedModel = dbHome.loadModel(monitor);
		if (loadedModel != null) {

			fireLoad();
			return loadedModel;
		}

		// none found, create new model:
		log.debug("Creating new model in DB."); //$NON-NLS-1$
		monitor.setTaskName(Messages.getString("CnAElementFactory.4")); //$NON-NLS-1$
		loadedModel = new BSIModel();

		createBausteinVorschlaege();

		loadedModel = dbHome.save(loadedModel);
		ITVerbund verbund = (ITVerbund)CnAElementFactory.getInstance().saveNew(loadedModel, ITVerbund.TYPE_ID, null);
		loadedModel.addChild(verbund);

		fireLoad();
		return loadedModel;
	}

	private void createBausteinVorschlaege() {
		SubtypenZielobjekte mapping = new SubtypenZielobjekte();
		List<BausteinVorschlag> list = mapping.getMapping();
		UpdateMultipleElements<BausteinVorschlag> command = new UpdateMultipleElements<BausteinVorschlag>(
				list, ChangeLogEntry.STATION_ID, ChangeLogEntry.TYPE_INSERT);
		try {
			ServiceFactory.lookupCommandService().executeCommand(
					command);
		} catch (CommandException e) {
			throw new RuntimeCommandException(e);
		}
	}

	public void reloadModelFromDatabase() {
		try {
			fireClosed();
			if (isModelLoaded()) {
				BSIModel newModel = dbHome.loadModel(new NullMonitor());
				loadedModel.modelReload(newModel);
				loadedModel.moveListener(newModel);
				loadedModel = newModel;
				fireLoad();
			}
			if (isIsoModelLoaded()) {
				ISO27KModel newModel = loadIsoModel();
				if (log.isDebugEnabled()) {
					log.debug("reloadModelFromDatabase, ISO-model loaded"); //$NON-NLS-1$
				}
				isoModel.modelReload(newModel);
				isoModel.moveListener(newModel);
				isoModel = newModel;
				fireLoad(isoModel);
			}
		} catch (Exception e) {
			log.error(Messages.getString("CnAElementFactory.5"), e); //$NON-NLS-1$
		}
	}

	private ICommandService getCommandService() {
		if (commandService == null) {
			commandService = createCommandService();
		}
		return commandService;
	}

	private ICommandService createCommandService() {
		try {
			ServiceFactory.openCommandService();
		} catch (MalformedURLException e) {
			log.error(Messages.getString("CnAElementFactory.6"), e); //$NON-NLS-1$
			throw new RuntimeException(
					Messages.getString("CnAElementFactory.7"), e); //$NON-NLS-1$
		}
		commandService = ServiceFactory.lookupCommandService();
		return commandService;
	}

	/**
	 * @param changeLogEntry
	 */
	public static void databaseChildRemoved(ChangeLogEntry changeLogEntry) {
		if (isModelLoaded()) {
			CnAElementFactory.getLoadedModel().databaseChildRemoved(
					changeLogEntry);
		}
		if (isIsoModelLoaded()) {
			CnAElementFactory.getInstance().getISO27kModel()
					.databaseChildRemoved(changeLogEntry);
		}
	}
}
