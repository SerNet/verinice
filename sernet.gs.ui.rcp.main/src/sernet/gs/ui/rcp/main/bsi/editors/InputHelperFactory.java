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
package sernet.gs.ui.rcp.main.bsi.editors;

import java.util.List;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.PropertyGroup;
import sernet.hui.common.connect.PropertyType;
import sernet.hui.swt.widgets.HitroUIComposite;
import sernet.hui.swt.widgets.IInputHelper;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bsi.Anwendung;
import sernet.verinice.model.bsi.Client;
import sernet.verinice.model.bsi.Gebaeude;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.NetzKomponente;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.bsi.Raum;
import sernet.verinice.model.bsi.Schutzbedarf;
import sernet.verinice.model.bsi.Server;
import sernet.verinice.model.bsi.SonstIT;
import sernet.verinice.model.bsi.TelefonKomponente;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.Document;
import sernet.verinice.model.iso27k.Evidence;
import sernet.verinice.model.iso27k.Exception;
import sernet.verinice.model.iso27k.Finding;
import sernet.verinice.model.iso27k.Incident;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.model.iso27k.Interview;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.model.iso27k.Process;
import sernet.verinice.model.iso27k.Record;
import sernet.verinice.model.iso27k.Requirement;
import sernet.verinice.model.iso27k.Response;
import sernet.verinice.model.iso27k.Threat;
import sernet.verinice.model.iso27k.Vulnerability;

public class InputHelperFactory {

    private static IInputHelper schutzbedarfHelper;
    private static IInputHelper tagHelper;
    private static IInputHelper personHelper;

    public static void setInputHelpers(EntityType entityType, HitroUIComposite huiComposite2) {
        //
        if (personHelper == null) {
            personHelper = new IInputHelper() {
                public String[] getSuggestions() {
                    List<Person> personen;
                    try {
                        personen = CnAElementHome.getInstance().getPersonen();
                        String[] titles = new String[personen.size()];
                        int i = 0;
                        for (Person person : personen) {
                            titles[i++] = person.getTitle();
                        }
                        return titles.length > 0 ? titles : new String[] { Messages.InputHelperFactory_0 };
                    } catch (CommandException e) {
                        ExceptionUtil.log(e, Messages.InputHelperFactory_1);
                        return new String[] { Messages.InputHelperFactory_0 };
                    }
                }
            };
        }

        if (tagHelper == null) {
            tagHelper = new IInputHelper() {
                public String[] getSuggestions() {
                    List<String> tags;
                    try {
                        tags = CnAElementHome.getInstance().getTags();
                        String[] tagArray = tags.toArray(new String[tags.size()]);
                        for (int i = 0; i < tagArray.length; i++) {
                            tagArray[i] = tagArray[i] + " "; //$NON-NLS-1$
                        }
                        return tagArray.length > 0 ? tagArray : new String[] {};
                    } catch (CommandException e) {
                        ExceptionUtil.log(e, Messages.InputHelperFactory_5);
                        return new String[] {};
                    }
                }
            };
        }

        if (schutzbedarfHelper == null) {
            schutzbedarfHelper = new IInputHelper() {
                public String[] getSuggestions() {
                    return new String[] { Schutzbedarf.MAXIMUM, Messages.InputHelperFactory_2, Messages.InputHelperFactory_3 };
                }
            };
        }

        boolean showHint = Activator.getDefault().getPluginPreferences().getBoolean(PreferenceConstants.INPUTHINTS);

        // Tag Helpers:
        // BSI elements
        huiComposite2.setInputHelper(Anwendung.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD, showHint);
        huiComposite2.setInputHelper(Client.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD, showHint);
        huiComposite2.setInputHelper(Gebaeude.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD, showHint);
        huiComposite2.setInputHelper(NetzKomponente.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD, showHint);
        huiComposite2.setInputHelper(Person.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD, showHint);
        huiComposite2.setInputHelper(Raum.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD, showHint);
        huiComposite2.setInputHelper(Server.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD, showHint);
        huiComposite2.setInputHelper(SonstIT.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD, showHint);
        huiComposite2.setInputHelper(TelefonKomponente.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD, showHint);
        huiComposite2.setInputHelper(ITVerbund.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD, showHint);

        // ISO27k elements
        huiComposite2.setInputHelper(Asset.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD, showHint);
        huiComposite2.setInputHelper(Audit.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD, showHint);
        huiComposite2.setInputHelper(Control.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD, showHint);
        huiComposite2.setInputHelper(Document.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD, showHint);
        huiComposite2.setInputHelper(Evidence.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD, showHint);
        huiComposite2.setInputHelper(Exception.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD, showHint);
        huiComposite2.setInputHelper(Finding.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD, showHint);
        huiComposite2.setInputHelper(Incident.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD, showHint);
        huiComposite2.setInputHelper(IncidentScenario.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD, showHint);
        huiComposite2.setInputHelper(Interview.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD, showHint);
        huiComposite2.setInputHelper(PersonIso.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD, showHint);
        huiComposite2.setInputHelper(Process.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD, showHint);
        huiComposite2.setInputHelper(Record.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD, showHint);
        huiComposite2.setInputHelper(Requirement.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD, showHint);
        huiComposite2.setInputHelper(Response.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD, showHint);
        huiComposite2.setInputHelper(Threat.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD, showHint);
        huiComposite2.setInputHelper(Vulnerability.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD, showHint);
        huiComposite2.setInputHelper(Organization.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD, showHint);

        setSchutzbedarfHelpers(entityType, huiComposite2, showHint);

    }

    private static void setSchutzbedarfHelpers(EntityType entityType, HitroUIComposite huiComposite2, boolean showHint) {
        for (PropertyGroup group : entityType.getPropertyGroups()) {
            for (PropertyType type : group.getPropertyTypes()) {
                if (Schutzbedarf.isIntegritaetBegruendung(type.getId()) || Schutzbedarf.isVerfuegbarkeitBegruendung(type.getId()) || Schutzbedarf.isVertraulichkeitBegruendung(type.getId())) {
                    huiComposite2.setInputHelper(type.getId(), schutzbedarfHelper, IInputHelper.TYPE_REPLACE, showHint);
                }
            }
        }

    }
}
