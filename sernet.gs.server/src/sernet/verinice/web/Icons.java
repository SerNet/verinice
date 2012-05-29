/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.web;

import java.util.Hashtable;
import java.util.Map;

import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.Document;
import sernet.verinice.model.iso27k.Evidence;
import sernet.verinice.model.iso27k.Exception;
import sernet.verinice.model.iso27k.Finding;
import sernet.verinice.model.iso27k.ImportIsoGroup;
import sernet.verinice.model.iso27k.Incident;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.model.iso27k.Interview;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.model.iso27k.Record;
import sernet.verinice.model.iso27k.Requirement;
import sernet.verinice.model.iso27k.Response;
import sernet.verinice.model.iso27k.Threat;
import sernet.verinice.model.iso27k.Vulnerability;
import sernet.verinice.model.samt.SamtTopic;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public abstract class Icons {
    
    public static final String FOLDER                   = "/image/icon/elements/folder.png";
    
    public static final String ISO27K_ASSET             = "/image/icon/elements/16-asset-grey.png";
    public static final String ISO27K_THREAT            = "/image/icon/elements/16-lightening.png";
    public static final String ISO27K_VULNERABILITY     = "/image/icon/elements/16-shield-blue-broken.png";
    public static final String ISO27K_INCIDENT_SCENARIO = "/image/icon/elements/dialog-warning.png";
    public static final String ISO27K_INCIDENT          = "/image/icon/elements/16-arrow-incident-red.png";
    public static final String ISO27K_REQUIREMENT       = "/image/icon/elements/16-paper-gavel-alt.png";
    public static final String ISO27K_EXCEPTION         = "/image/icon/elements/16-paper-excerpt-yellow.png";
    public static final String ISO27K_AUDIT             = "/image/icon/elements/16-clipboard-audit.png";
    public static final String ISO27K_INTERVIEW         = "/image/icon/elements/16-clipboard-comment.png";
    public static final String ISO27K_IMRPOVEMENT_NOTE  = "/image/icon/elements/16-clipboard-report-bar.png";
    public static final String ISO27K_EVIDENCE          = "/image/icon/elements/16-clipboard-eye.png";
    public static final String ISO27K_RESPONSE          = "/image/icon/elements/16-paper-arrow-green.png";
    public static final String ISO27K_DOCUMENT          = "/image/icon/elements/mime-document.png";
    public static final String ISO27K_RECORD            = "/image/icon/elements/mime-text.png";
    public static final String ISO27K_SCOPE             = "/image/icon/elements/tree_explorer.gif";
    public static final String ISO27K_PERSON            = "/image/icon/elements/user_suit.png";
    public static final String ISO27K_FOLDER            = "/image/icon/elements/folder.png";
    public static final String ISO27K_PROCESS           = "/image/icon/elements/16-paper-workplan.png";
    public static final String ISO27K_IMPORT            = "/image/icon/elements/import.gif";
    public static final String ISO27K_CONTROL           = "/image/icon/elements/stufe_none.png";   
    public static final String ISA_TOPIC                = "/image/icon/elements/isa-topic.png";
    
    public static final Map<String, String> ICONS;
    
    static {
        ICONS = new Hashtable<String, String>();
        ICONS.put(Organization.TYPE_ID, Icons.ISO27K_SCOPE);
        ICONS.put(Asset.TYPE_ID, Icons.ISO27K_ASSET);
        ICONS.put(Threat.TYPE_ID, Icons.ISO27K_THREAT);
        ICONS.put(Vulnerability.TYPE_ID, Icons.ISO27K_VULNERABILITY);
        ICONS.put(IncidentScenario.TYPE_ID,Icons.ISO27K_INCIDENT_SCENARIO);
        ICONS.put(Incident.TYPE_ID, Icons.ISO27K_INCIDENT);
        ICONS.put(Requirement.TYPE_ID, Icons.ISO27K_REQUIREMENT);
        ICONS.put(Exception.TYPE_ID, Icons.ISO27K_EXCEPTION);
        ICONS.put(Audit.TYPE_ID, Icons.ISO27K_AUDIT);
        ICONS.put(Interview.TYPE_ID, Icons.ISO27K_INTERVIEW);
        ICONS.put(Finding.TYPE_ID, Icons.ISO27K_IMRPOVEMENT_NOTE);
        ICONS.put(Evidence.TYPE_ID, Icons.ISO27K_EVIDENCE);
        ICONS.put(Document.TYPE_ID, Icons.ISO27K_DOCUMENT);
        ICONS.put(PersonIso.TYPE_ID, Icons.ISO27K_PERSON);
        ICONS.put(Control.TYPE_ID,   Icons.ISO27K_CONTROL);
        ICONS.put(Response.TYPE_ID,   Icons.ISO27K_RESPONSE);
        ICONS.put(sernet.verinice.model.iso27k.Process.TYPE_ID,Icons.ISO27K_PROCESS);
        ICONS.put(Record.TYPE_ID,   Icons.ISO27K_RECORD);
        ICONS.put(SamtTopic.TYPE_ID,   Icons.ISO27K_CONTROL);
        ICONS.put(ImportIsoGroup.TYPE_ID, Icons.ISO27K_IMPORT);
    }
}
