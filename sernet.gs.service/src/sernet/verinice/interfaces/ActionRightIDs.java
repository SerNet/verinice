/*******************************************************************************
 * Copyright (c) 2011 Sebastian Hagedorn <sh@sernet.de>.
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
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.interfaces;

import java.util.stream.Stream;

/**
 * List of all possible actions that should be controllable by the rights
 * management.
 */
public final class ActionRightIDs {

    public static final String ACCESSCONTROL = "accesscontrol";
    public static final String ACCOUNTSETTINGS = "accountsettings";
    public static final String ADDBPGROUP = "addbpgroup";
    public static final String ADDBSIELEMENT = "addbsielement";
    public static final String ADDFILE = "addfile";
    public static final String ADDISMELEMENT = "addismelement";
    public static final String ADDISMGROUP = "addismgroup";
    public static final String ADDISMORG = "addismorg";
    public static final String ADDITNETWORK = "additnetwork";
    public static final String ADDITVERBUND = "additverbund";
    public static final String ADDBPELEMENT = "addbpelement";
    public static final String ADDNOTE = "addnote";
    public static final String ADDSECURITYASSESSMENT = "addsecurityassessment";
    public static final String ADDOWNMODUL = "addownmodul";
    public static final String ASSIGNALLISATOPICS = "assignallisatopics";
    public static final String AUDITVIEW = "auditview";
    public static final String BASEPROTECTIONMODELING = "baseprotectionmodeling";
    public static final String BAUSTEINZUORDNUNG = "bausteinzuordnung";
    public static final String BPIMPORTER = "bpimporter";
    public static final String BSIBROWSER = "bsibrowser";
    public static final String BSIMASSNAHMEN = "bsimassnahmen";
    public static final String BSIMODELVIEW = "bsimodelview";
    public static final String BULKEDIT = "bulkedit";
    public static final String CATALOGDELETE = "catalogdelete";
    public static final String CATALOGIMPORT = "catalogimport";
    public static final String CATALOGVIEW = "catalogview";
    public static final String CHANGEICON = "changeicon";
    public static final String CNAVALIDATION = "cnavalidation";
    public static final String CREATE_GREENBONE_TASKS = "creategreenbonetasks";
    public static final String CREATE_INDIVIDUAL_TASKS = "createindividualtasks";
    public static final String CREATEISATASKS = "createisatasks";
    public static final String DELETEFILE = "deletefile";
    public static final String DELETEITEM = "deleteitem";
    public static final String DERIVESTATUS = "derivestatus";
    public static final String DOCUMENTVIEW = "documentview";
    public static final String EDITPROFILE = "editprofile";
    public static final String EDITLINKS = "editlinks";
    public static final String EXPORT_LINK_TABLE = "exportlinktable";
    public static final String FILES = "files";
    public static final String GENERATEORGREPORT = "generateorgreport";
    public static final String GROUP_BY_TAG = "groupbytag";
    public static final String GSNOTESIMPORT = "gsnotesimport";
    public static final String GSTOOLIMPORT = "gstoolimport";
    public static final String IMPORTCSV = "importcsv";
    public static final String IMPORTLDAP = "importldap";
    public static final String ISMCUT = "ismcut";
    public static final String ISMCOPY = "ismcopy";
    public static final String ISMVIEW = "ismview";
    public static final String ISMVIEWWEB = "ismviewweb";
    public static final String KONSOLIDATOR = "konsolidator";
    public static final String MARKTEMPLATE = "marktemplate";
    public static final String BASEPROTECTIONVIEW = "baseprotectionview";
    public static final String NATURALIZE = "naturalize";
    public static final String NOTES = "notes";
    public static final String CHANGEOWNPASSWORD = "changeownpassword";
    public static final String RELATIONS = "relations";
    public static final String REPORTDEPOSIT = "reportdeposit";
    public static final String REPORTDEPOSITADD = "reportdepositadd";
    public static final String REPORTDEPOSITDELETE = "reportdepositdelete";
    public static final String REPORTDEPOSITEDIT = "reportdepositedit";
    public static final String RISKANALYSIS = "riskanalysis";
    public static final String SAMTVIEW = "samtview";
    public static final String SEARCHEXPORT = "searchexport";
    public static final String SEARCHREINDEX = "searchreindex";
    public static final String SEARCHVIEW = "searchview";
    public static final String SHOWALLFILES = "showallfiles";
    public static final String SHOWCHARTVIEW = "showchartview";
    public static final String SHOWPREFERENCES = "showpreferences";
    public static final String SIMPLEAUDITVIEW = "simpleauditview";
    public static final String TODO = "todo";
    public static final String TASKVIEW = "taskview";
    public static final String TASKDELETE = "taskdelete";
    public static final String TASKSHOWALL = "taskshowall";
    public static final String TASKCHANGEASSIGNEE = "taskchangeassignee";
    public static final String TASKCHANGEDUEDATE = "taskchangeduedate";
    public static final String TASKWITHRELEASEPROCESS = "taskwithreleaseprocess";
    public static final String TEMPLATES = "templates";
    public static final String EDITRISKCONFIGURATION = "editriskconfiguration";
    // value "bsidnd" kept due to historical reasons
    public static final String TREEDND = "bsidnd";
    public static final String UNIFY = "unify";
    public static final String XMLEXPORT = "xmlexport";
    public static final String XMLIMPORT = "xmlimport";
    public static final String MIGRATE_DATA_PROTECTION = "migrate_data_protection";
    public static final String CONVERT_IT_NETWORK = "convertitnetwork";

    private static final String[] ALL_RIGHT_IDS;

    static {
        ALL_RIGHT_IDS = Stream.of(ActionRightIDs.class.getDeclaredFields()).map(field -> {
            try {
                return field.get(null);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new ExceptionInInitializerError(e);
            }
        }).filter(String.class::isInstance).toArray(String[]::new);
    }

    private ActionRightIDs() {
        super();
    }

    public static String[] getAllRightIDs() {
        return ALL_RIGHT_IDS.clone();
    }
}
