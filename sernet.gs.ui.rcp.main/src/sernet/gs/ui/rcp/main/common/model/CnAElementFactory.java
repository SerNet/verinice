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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.model.Baustein;
import sernet.gs.ui.rcp.main.Activator;
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
import sernet.gs.ui.rcp.main.service.ICommandService;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.CreateAnwendung;
import sernet.gs.ui.rcp.main.service.crudcommands.CreateITVerbund;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveElement;
import sernet.gs.ui.rcp.main.service.crudcommands.UpdateMultipleElements;
import sernet.verinice.iso27k.model.Asset;
import sernet.verinice.iso27k.model.AssetGroup;
import sernet.verinice.iso27k.model.Audit;
import sernet.verinice.iso27k.model.AuditGroup;
import sernet.verinice.iso27k.model.Control;
import sernet.verinice.iso27k.model.ControlGroup;
import sernet.verinice.iso27k.model.Document;
import sernet.verinice.iso27k.model.DocumentGroup;
import sernet.verinice.iso27k.model.Evidence;
import sernet.verinice.iso27k.model.EvidenceGroup;
import sernet.verinice.iso27k.model.ExceptionGroup;
import sernet.verinice.iso27k.model.Finding;
import sernet.verinice.iso27k.model.FindingGroup;
import sernet.verinice.iso27k.model.IISO27kElement;
import sernet.verinice.iso27k.model.ISO27KModel;
import sernet.verinice.iso27k.model.Incident;
import sernet.verinice.iso27k.model.IncidentGroup;
import sernet.verinice.iso27k.model.IncidentScenario;
import sernet.verinice.iso27k.model.IncidentScenarioGroup;
import sernet.verinice.iso27k.model.Interview;
import sernet.verinice.iso27k.model.InterviewGroup;
import sernet.verinice.iso27k.model.Organization;
import sernet.verinice.iso27k.model.PersonGroup;
import sernet.verinice.iso27k.model.Process;
import sernet.verinice.iso27k.model.ProcessGroup;
import sernet.verinice.iso27k.model.Record;
import sernet.verinice.iso27k.model.RecordGroup;
import sernet.verinice.iso27k.model.Requirement;
import sernet.verinice.iso27k.model.RequirementGroup;
import sernet.verinice.iso27k.model.Response;
import sernet.verinice.iso27k.model.ResponseGroup;
import sernet.verinice.iso27k.model.Threat;
import sernet.verinice.iso27k.model.ThreatGroup;
import sernet.verinice.iso27k.model.Vulnerability;
import sernet.verinice.iso27k.model.VulnerabilityGroup;
import sernet.verinice.iso27k.service.commands.LoadModel;

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
	
	private abstract class ElementBuilder implements IElementBuilder{
	    protected void init( CnATreeElement container,CnATreeElement child ) {
	        container.addChild(child);
            child.setParent(container);
	    }
	}

	public void addLoadListener(IModelLoadListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	public void removeLoadListener(IModelLoadListener listener) {
		if (listeners.contains(listener))
			listeners.remove(listener);
	}

	@SuppressWarnings("unchecked")
	private CnAElementFactory() {
		dbHome = CnAElementHome.getInstance();

		elementbuilders.put(Gebaeude.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				Gebaeude child = dbHome.save(container, Gebaeude.class, Gebaeude.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(Client.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				Client child = dbHome.save(container, Client.class, Client.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(SonstIT.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				SonstIT child = dbHome.save(container, SonstIT.class, SonstIT.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(Server.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				Server child = dbHome.save(container, Server.class, Server.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(TelefonKomponente.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				TelefonKomponente child = dbHome.save(container, TelefonKomponente.class, TelefonKomponente.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(Raum.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				Raum child = dbHome.save(container, Raum.class, Raum.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(NetzKomponente.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				NetzKomponente child = dbHome.save(container, NetzKomponente.class, NetzKomponente.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(Person.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				Person child = dbHome.save(container, Person.class, Person.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(Anwendung.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {

				log.debug("Creating new Anwendung in " + container); //$NON-NLS-1$
				CreateAnwendung saveCommand = new CreateAnwendung(container, Anwendung.class);
				saveCommand = ServiceFactory.lookupCommandService().executeCommand(saveCommand);
				Anwendung child = saveCommand.getNewElement();

				init(container, child);

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

		elementbuilders.put(ITVerbund.TYPE_ID, new ElementBuilder() {
			public ITVerbund build(CnATreeElement container, BuildInput input) throws Exception {

				log.debug("Creating new ITVerbund in " + container); //$NON-NLS-1$
				CreateITVerbund saveCommand = new CreateITVerbund(container, ITVerbund.class);
				saveCommand = ServiceFactory.lookupCommandService().executeCommand(saveCommand);
				ITVerbund verbund = saveCommand.getNewElement();

				verbund.setParent(loadedModel);
				return verbund;
			}
		});

		// ISO 27000 builders
		elementbuilders.put(Organization.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				Organization child = dbHome.save(container, Organization.class, Organization.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		
		elementbuilders.put(AssetGroup.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				AssetGroup child = dbHome.save(container, AssetGroup.class, AssetGroup.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		elementbuilders.put(Asset.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				Asset child = dbHome.save(container, Asset.class, Asset.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		
		elementbuilders.put(PersonGroup.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				PersonGroup child = dbHome.save(container, PersonGroup.class, PersonGroup.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		elementbuilders.put(sernet.verinice.iso27k.model.PersonIso.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				sernet.verinice.iso27k.model.PersonIso child = dbHome.save(container, sernet.verinice.iso27k.model.PersonIso.class, sernet.verinice.iso27k.model.PersonIso.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		
		elementbuilders.put(AuditGroup.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				AuditGroup child = dbHome.save(container, AuditGroup.class, AuditGroup.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		elementbuilders.put(Audit.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				Audit child = dbHome.save(container, Audit.class, Audit.TYPE_ID);
				init(container, child);
				return child;
			}
		});
			
		elementbuilders.put(ControlGroup.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				ControlGroup child = dbHome.save(container, ControlGroup.class, ControlGroup.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		elementbuilders.put(Control.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				Control child = dbHome.save(container, Control.class, Control.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		
		elementbuilders.put(ExceptionGroup.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				ExceptionGroup child = dbHome.save(container, ExceptionGroup.class, ExceptionGroup.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		elementbuilders.put(sernet.verinice.iso27k.model.Exception.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				sernet.verinice.iso27k.model.Exception child = dbHome.save(container, sernet.verinice.iso27k.model.Exception.class, sernet.verinice.iso27k.model.Exception.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		
		elementbuilders.put(RequirementGroup.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				RequirementGroup child = dbHome.save(container, RequirementGroup.class, RequirementGroup.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		elementbuilders.put(Requirement.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				Requirement child = dbHome.save(container, Requirement.class, Requirement.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		
		elementbuilders.put(Incident.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				Incident child = dbHome.save(container, Incident.class, Incident.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		elementbuilders.put(IncidentGroup.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				IncidentGroup child = dbHome.save(container, IncidentGroup.class, IncidentGroup.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		
		elementbuilders.put(IncidentScenario.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				IncidentScenario child = dbHome.save(container, IncidentScenario.class, IncidentScenario.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		elementbuilders.put(IncidentScenarioGroup.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				IncidentScenarioGroup child = dbHome.save(container, IncidentScenarioGroup.class, IncidentScenarioGroup.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		
		elementbuilders.put(Response.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				Response child = dbHome.save(container, Response.class, Response.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		elementbuilders.put(ResponseGroup.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				ResponseGroup child = dbHome.save(container, ResponseGroup.class, ResponseGroup.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		
		elementbuilders.put(Threat.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				Threat child = dbHome.save(container, Threat.class, Threat.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		elementbuilders.put(ThreatGroup.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				ThreatGroup child = dbHome.save(container, ThreatGroup.class, ThreatGroup.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		
		elementbuilders.put(Vulnerability.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				Vulnerability child = dbHome.save(container, Vulnerability.class, Vulnerability.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		elementbuilders.put(VulnerabilityGroup.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				VulnerabilityGroup child = dbHome.save(container, VulnerabilityGroup.class, VulnerabilityGroup.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		
		elementbuilders.put(DocumentGroup.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				DocumentGroup child = dbHome.save(container, DocumentGroup.class, DocumentGroup.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		elementbuilders.put(Document.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				Document child = dbHome.save(container, Document.class, Document.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		
		elementbuilders.put(InterviewGroup.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				InterviewGroup child = dbHome.save(container, InterviewGroup.class, InterviewGroup.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		elementbuilders.put(Interview.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				Interview child = dbHome.save(container, Interview.class, Interview.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		
		elementbuilders.put(FindingGroup.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				FindingGroup child = dbHome.save(container, FindingGroup.class, FindingGroup.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		elementbuilders.put(Finding.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				Finding child = dbHome.save(container, Finding.class, Finding.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		
		elementbuilders.put(EvidenceGroup.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				EvidenceGroup child = dbHome.save(container, EvidenceGroup.class, EvidenceGroup.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		elementbuilders.put(Evidence.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				Evidence child = dbHome.save(container, Evidence.class, Evidence.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		
		elementbuilders.put(ProcessGroup.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				ProcessGroup child = dbHome.save(container, ProcessGroup.class, ProcessGroup.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		elementbuilders.put(Process.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				Process child = dbHome.save(container, Process.class, Process.TYPE_ID);
				init(container, child);
				return child;
			}
		});

		elementbuilders.put(RecordGroup.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				RecordGroup child = dbHome.save(container, RecordGroup.class, RecordGroup.TYPE_ID);
				init(container, child);
				return child;
			}
		});
		elementbuilders.put(Record.TYPE_ID, new ElementBuilder() {
			public CnATreeElement build(CnATreeElement container, BuildInput input) throws Exception {
				Record child = dbHome.save(container, Record.class, Record.TYPE_ID);
				init(container, child);
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
			throw new Exception(Messages.getString("CnAElementFactory.0") + buildableTypeId); //$NON-NLS-1$
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
	
	public static boolean isIsoModelLoaded() {
		return (isoModel != null);
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
		if(element instanceof IISO27kElement) {
			model = CnAElementFactory.getInstance().getISO27kModel();
		} else {
			model = CnAElementFactory.getLoadedModel();
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
		ISO27KModel model = null;
		try {
			LoadModel loadModel = new LoadModel();
			loadModel = getCommandService().executeCommand(loadModel);
			model = loadModel.getModel();
			if(model!=null) {
				fireLoad();
			}
		} catch(Exception e) {
			log.error(Messages.getString("CnAElementFactory.1"), e); //$NON-NLS-1$
		}
		return model;
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
				log.info("ISO27KModel created"); //$NON-NLS-1$
			}
			if(isoModel!=null) {
				fireLoad();
			}
		} catch(Exception e) {
			log.error(Messages.getString("CnAElementFactory.2"), e); //$NON-NLS-1$
		}
		return isoModel;
	}

	public BSIModel loadOrCreateModel(IProgress monitor) throws Exception {
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
			
			if(isModelLoaded()) {
				BSIModel newModel = dbHome.loadModel(new NullMonitor());
				loadedModel.modelReload(newModel);
				loadedModel = newModel;
			}
			if(isIsoModelLoaded()) {
				ISO27KModel newModel = loadIsoModel();
				if (log.isDebugEnabled()) {
					log.debug("reloadModelFromDatabase, ISO-model loaded"); //$NON-NLS-1$
				}
				isoModel.modelReload(newModel);
				isoModel = newModel;
			}
			
			fireLoad();
		} catch (Exception e) {
		    log.error(Messages.getString("CnAElementFactory.5"), e); //$NON-NLS-1$
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
			log.error(Messages.getString("CnAElementFactory.6"), e); //$NON-NLS-1$
			throw new RuntimeException(Messages.getString("CnAElementFactory.7"), e); //$NON-NLS-1$
		}
		commandService = ServiceFactory.lookupCommandService();
		return commandService;
	}

	/**
	 * @param changeLogEntry
	 */
	public static void databaseChildRemoved(ChangeLogEntry changeLogEntry) {
		if(isModelLoaded()) {
			CnAElementFactory.getLoadedModel().databaseChildRemoved(changeLogEntry);
		}
		if(isIsoModelLoaded()) {
			CnAElementFactory.getInstance().getISO27kModel().databaseChildRemoved(changeLogEntry);
		}
	}

}
