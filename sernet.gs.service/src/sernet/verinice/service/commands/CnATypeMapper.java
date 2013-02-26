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
package sernet.verinice.service.commands;

import java.util.HashMap;
import java.util.Map;

import sernet.verinice.model.bsi.Anwendung;
import sernet.verinice.model.bsi.AnwendungenKategorie;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.Client;
import sernet.verinice.model.bsi.ClientsKategorie;
import sernet.verinice.model.bsi.Gebaeude;
import sernet.verinice.model.bsi.GebaeudeKategorie;
import sernet.verinice.model.bsi.ITVerbund;
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
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.ds.Datenverarbeitung;
import sernet.verinice.model.ds.Personengruppen;
import sernet.verinice.model.ds.StellungnahmeDSB;
import sernet.verinice.model.ds.VerantwortlicheStelle;
import sernet.verinice.model.ds.Verarbeitungsangaben;
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
import sernet.verinice.model.iso27k.Incident;
import sernet.verinice.model.iso27k.IncidentGroup;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.model.iso27k.IncidentScenarioGroup;
import sernet.verinice.model.iso27k.Interview;
import sernet.verinice.model.iso27k.InterviewGroup;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.iso27k.PersonGroup;
import sernet.verinice.model.iso27k.PersonIso;
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

/**
 * Mapper to find classes by typeID String.
 * Needed during Import (originally refactored out of SyncInsertUpdateCommand)
 * but also needed in many other cases because Hibernate looses marker interfaces (such as IISO27kElement)
 * when loading subclasses over generic queries.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class CnATypeMapper {
    private static Map<String, Class<? extends CnATreeElement>> typeIdClass = new HashMap<String, Class<? extends CnATreeElement>>();
    
    private static Map<String, String> descriptionPropertyMap = new HashMap<String, String>();

  

    static {
        typeIdClass.put(Anwendung.TYPE_ID, Anwendung.class);
        typeIdClass.put(Gebaeude.TYPE_ID, Gebaeude.class);
        typeIdClass.put(Client.TYPE_ID, Client.class);
        typeIdClass.put(Server.TYPE_ID, Server.class);
        typeIdClass.put(SonstIT.TYPE_ID, SonstIT.class);
        typeIdClass.put(TelefonKomponente.TYPE_ID, TelefonKomponente.class);
        typeIdClass.put(Person.TYPE_ID, Person.class);
        typeIdClass.put(NetzKomponente.TYPE_ID, NetzKomponente.class);
        typeIdClass.put(Raum.TYPE_ID, Raum.class);
        typeIdClass.put(AnwendungenKategorie.TYPE_ID, AnwendungenKategorie.class);
        typeIdClass.put(GebaeudeKategorie.TYPE_ID, GebaeudeKategorie.class);
        typeIdClass.put(ClientsKategorie.TYPE_ID, ClientsKategorie.class);
        typeIdClass.put(ServerKategorie.TYPE_ID, ServerKategorie.class);
        typeIdClass.put(SonstigeITKategorie.TYPE_ID, SonstigeITKategorie.class);
        typeIdClass.put(TKKategorie.TYPE_ID, TKKategorie.class);
        typeIdClass.put(PersonenKategorie.TYPE_ID, PersonenKategorie.class);
        typeIdClass.put(NKKategorie.TYPE_ID, NKKategorie.class);
        typeIdClass.put(RaeumeKategorie.TYPE_ID, RaeumeKategorie.class);
        typeIdClass.put(BausteinUmsetzung.TYPE_ID, BausteinUmsetzung.class);
        typeIdClass.put(ITVerbund.TYPE_ID, ITVerbund.class);
        typeIdClass.put(MassnahmenUmsetzung.TYPE_ID, MassnahmenUmsetzung.class);
        typeIdClass.put(Verarbeitungsangaben.TYPE_ID, Verarbeitungsangaben.class);
        typeIdClass.put(Personengruppen.TYPE_ID, Personengruppen.class);
        typeIdClass.put(VerantwortlicheStelle.TYPE_ID, VerantwortlicheStelle.class);
        typeIdClass.put(StellungnahmeDSB.TYPE_ID, StellungnahmeDSB.class);
        typeIdClass.put(Datenverarbeitung.TYPE_ID, Datenverarbeitung.class);
        
        // BSI Risk analyses will not be imported or exported(Bug 194)
        // typeIdClass.put(FinishedRiskAnalysis.TYPE_ID, FinishedRiskAnalysis.class);
        // typeIdClass.put(GefaehrdungsUmsetzung.TYPE_ID, GefaehrdungsUmsetzung.class);
        // typeIdClass.put(RisikoMassnahmenUmsetzung.TYPE_ID, RisikoMassnahmenUmsetzung.class);

        typeIdClass.put(ResponseGroup.TYPE_ID, ResponseGroup.class);
        typeIdClass.put(ExceptionGroup.TYPE_ID, ExceptionGroup.class);
        typeIdClass.put(VulnerabilityGroup.TYPE_ID, VulnerabilityGroup.class);
        typeIdClass.put(PersonGroup.TYPE_ID, PersonGroup.class);
        typeIdClass.put(IncidentGroup.TYPE_ID, IncidentGroup.class);
        typeIdClass.put(ThreatGroup.TYPE_ID, ThreatGroup.class);
        typeIdClass.put(Organization.TYPE_ID, Organization.class);
        typeIdClass.put(ProcessGroup.TYPE_ID, ProcessGroup.class);
        typeIdClass.put(AuditGroup.TYPE_ID, AuditGroup.class);
        typeIdClass.put(IncidentScenarioGroup.TYPE_ID, IncidentScenarioGroup.class);
        typeIdClass.put(RecordGroup.TYPE_ID, RecordGroup.class);
        typeIdClass.put(RequirementGroup.TYPE_ID, RequirementGroup.class);
        typeIdClass.put(ControlGroup.TYPE_ID, ControlGroup.class);
        typeIdClass.put(DocumentGroup.TYPE_ID, DocumentGroup.class);
        typeIdClass.put(AssetGroup.TYPE_ID, AssetGroup.class);
        typeIdClass.put(EvidenceGroup.TYPE_ID, EvidenceGroup.class);
        typeIdClass.put(InterviewGroup.TYPE_ID, InterviewGroup.class);
        typeIdClass.put(FindingGroup.TYPE_ID, FindingGroup.class);
        
        typeIdClass.put(Response.TYPE_ID, Response.class);
        typeIdClass.put(sernet.verinice.model.iso27k.Exception.TYPE_ID, sernet.verinice.model.iso27k.Exception.class);
        typeIdClass.put(Vulnerability.TYPE_ID, Vulnerability.class);
        typeIdClass.put(PersonIso.TYPE_ID, PersonIso.class);
        typeIdClass.put(Incident.TYPE_ID, Incident.class);
        typeIdClass.put(Threat.TYPE_ID, Threat.class);
        typeIdClass.put(sernet.verinice.model.iso27k.Process.TYPE_ID, sernet.verinice.model.iso27k.Process.class);
        typeIdClass.put(Audit.TYPE_ID, Audit.class);
        typeIdClass.put(IncidentScenario.TYPE_ID, IncidentScenario.class);
        typeIdClass.put(Record.TYPE_ID, Record.class);
        typeIdClass.put(Requirement.TYPE_ID, Requirement.class);
        typeIdClass.put(Control.TYPE_ID, Control.class);
        typeIdClass.put(Document.TYPE_ID, Document.class);
        typeIdClass.put(Asset.TYPE_ID, Asset.class);
        typeIdClass.put(Evidence.TYPE_ID, Evidence.class);
        typeIdClass.put(Interview.TYPE_ID, Interview.class);
        typeIdClass.put(Finding.TYPE_ID, Finding.class);

        typeIdClass.put(SamtTopic.TYPE_ID, SamtTopic.class);
        
        
        // map for description properties:
        descriptionPropertyMap.put(Client.TYPE_ID, Client.PROP_ERLAEUTERUNG);
        descriptionPropertyMap.put(Gebaeude.TYPE_ID, Gebaeude.PROP_ERLAEUTERUNG);
        descriptionPropertyMap.put(Server.TYPE_ID, Server.PROP_ERLAEUTERUNG);
        descriptionPropertyMap.put(SonstIT.TYPE_ID, SonstIT.PROP_ERLAEUTERUNG);
        descriptionPropertyMap.put(TelefonKomponente.TYPE_ID, TelefonKomponente.PROP_ERLAEUTERUNG);
        descriptionPropertyMap.put(Person.TYPE_ID, Person.PROP_ERLAEUTERUNG);
        descriptionPropertyMap.put(NetzKomponente.TYPE_ID, NetzKomponente.PROP_ERLAEUTERUNG);
        descriptionPropertyMap.put(Raum.TYPE_ID, Raum.PROP_ERLAEUTERUNG);
        descriptionPropertyMap.put(BausteinUmsetzung.TYPE_ID, BausteinUmsetzung.P_ERLAEUTERUNG);
        descriptionPropertyMap.put(MassnahmenUmsetzung.TYPE_ID, MassnahmenUmsetzung.P_ERLAEUTERUNG);
        
    }
    
 // this is necessary because hibernate returns proxy objects that will not implement the marker interface IBSIStrukturelement
    // TODO akoderman change marker interface to object composition: add adaptable interface for strukturelements to model classes
    private static final String[] STRUKTUR_ELEMENT_TYPES = new String[] {
        Anwendung.TYPE_ID,
        BSIModel.TYPE_ID,
        Client.TYPE_ID,
        Gebaeude.TYPE_ID,
        ITVerbund.TYPE_ID,
        NetzKomponente.TYPE_ID,
        Person.TYPE_ID,
        Raum.TYPE_ID,
        Server.TYPE_ID,
        SonstIT.TYPE_ID,
        TelefonKomponente.TYPE_ID
    };

    private static final String[] IISO27K_ELEMENT_TYPES = new String[] {
        ResponseGroup.TYPE_ID,
        ExceptionGroup.TYPE_ID,
        VulnerabilityGroup.TYPE_ID,
        PersonGroup.TYPE_ID,
        IncidentGroup.TYPE_ID,
        ThreatGroup.TYPE_ID,
        Organization.TYPE_ID,
        ProcessGroup.TYPE_ID,
        AuditGroup.TYPE_ID,
        IncidentScenarioGroup.TYPE_ID,
        RecordGroup.TYPE_ID,
        RequirementGroup.TYPE_ID,
        ControlGroup.TYPE_ID,
        DocumentGroup.TYPE_ID,
        AssetGroup.TYPE_ID,
        EvidenceGroup.TYPE_ID,
        InterviewGroup.TYPE_ID,
        FindingGroup.TYPE_ID
    };
    
    /**
     * @param child
     * @return
     */
    public boolean isStrukturElement(CnATreeElement child) {
        for (String strukturType : STRUKTUR_ELEMENT_TYPES) {
            if (child.getEntityType() != null && child.getEntityType().getId().equals(strukturType)){
                return true;
            }
        }
        return false;
    }
    
    /**
     * @param child
     * @return
     */
    public boolean isIiso27kElement(CnATreeElement child) {
        for (String strukturType : IISO27K_ELEMENT_TYPES) {
            if (child.getEntityType() != null && child.getEntityType().getId().equals(strukturType)){
                return true;
            }
        }
        return false;
    }
    
    /************************************************************
     * getClassFromTypeId()
     * 
     * @param typeId
     * @return the corresponding Class
     ************************************************************/
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getClassFromTypeId(String typeId) {
        Class<T> klass = (Class<T>) typeIdClass.get(typeId);
        if (klass == null) {
            throw new IllegalStateException(String.format("Type ID '%s' was not available in type map.", typeId));
        }

        return klass;
    }
    
    public String getDescriptionPropertyForType(String typeId) {
        return descriptionPropertyMap.get(typeId);
    }
}


