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
import sernet.verinice.model.bsi.NetzKomponente;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.bsi.Raum;
import sernet.verinice.model.bsi.Schutzbedarf;
import sernet.verinice.model.bsi.Server;
import sernet.verinice.model.bsi.SonstIT;
import sernet.verinice.model.bsi.TelefonKomponente;

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
        huiComposite2.setInputHelper(Anwendung.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD, showHint);
        huiComposite2.setInputHelper(Client.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD, showHint);
        huiComposite2.setInputHelper(Gebaeude.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD, showHint);
        huiComposite2.setInputHelper(NetzKomponente.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD, showHint);
        huiComposite2.setInputHelper(Person.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD, showHint);
        huiComposite2.setInputHelper(Raum.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD, showHint);
        huiComposite2.setInputHelper(Server.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD, showHint);
        huiComposite2.setInputHelper(SonstIT.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD, showHint);
        huiComposite2.setInputHelper(TelefonKomponente.PROP_TAG, tagHelper, IInputHelper.TYPE_ADD, showHint);

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
