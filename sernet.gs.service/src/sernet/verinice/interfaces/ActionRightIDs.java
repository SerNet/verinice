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

import java.lang.reflect.Field;
import java.util.ArrayList;

import org.apache.log4j.Logger;

/**
 * List of all possible actions that should be controlable by right-management
 */
public class ActionRightIDs {
    
    private static Logger LOG = Logger.getLogger(ActionRightIDs.class);
    
    public static final String ACCESSCONTROL = "accesscontrol";
    public static final String ACCOUNTSETTINGS = "accountsettings";
    public static final String ADDBSIELEMENT = "addbsielement";
    public static final String ADDCATALOG = "addcatalog";
    public static final String ADDFILE = "addfile";
    public static final String ADDISMELEMENT = "addismelement";
    public static final String ADDISMGROUP = "addismgroup";
    public static final String ADDISMORG = "addismorg";
    public static final String ADDITVERBUND = "additverbund";
    public static final String ADDNOTE = "addnote";
    public static final String ADDSECURITYASSESSMENT = "addsecurityassessment";
    public static final String ADDOWNMODUL = "addownmodul";
    public static final String ASSIGNALLISATOPICS = "assignallisatopics";
    public static final String AUDITVIEW = "auditview";
    public static final String BAUSTEINZUORDNUNG = "bausteinzuordnung";
    public static final String BSIBROWSER = "bsibrowser";
    public static final String BSIMASSNAHMEN = "bsimassnahmen";
    public static final String BSIMODELVIEW = "bsimodelview";
    public static final String BULKEDIT = "bulkedit";
    public static final String CHANGEICON = "changeicon";   
    public static final String CREATEISATASKS = "createisatasks";
    public static final String DELETECATALOG = "deletecatalog";
    public static final String DELETEFILE = "deletefile";
    public static final String DELETEITEM = "deleteitem";
    public static final String DERIVESTATUS = "derivestatus";
    public static final String DOCUMENTVIEW = "documentview";
    public static final String DSMODELVIEW = "dsmodelview";
    public static final String EDITPROFILE = "editprofile";
    public static final String EDITLINKS = "editlinks";
    public static final String FILES = "files";
    public static final String GENERATEORGREPORT = "generateorgreport";
    public static final String GENERATEAUDITREPORT = "generateauditreport";
    public static final String GSNOTESIMPORT = "gsnotesimport";
    public static final String GSTOOLIMPORT = "gstoolimport";
    public static final String IMPORTCSV = "importcsv";
    public static final String IMPORTLDAP = "importldap";
    public static final String ISMCATALOG = "ismcatalog";
    public static final String ISMCUT = "ismcut";
    public static final String ISMCOPY = "ismcopy";
    public static final String ISMVIEW = "ismview";
    public static final String ISMVIEWWEB = "ismviewweb";
    public static final String KONSOLIDATOR = "konsolidator";
    public static final String NATURALIZE = "naturalize";
    public static final String NOTES = "notes";
    public static final String RELATIONS = "relations";
    public static final String RISKANALYSIS = "riskanalysis";
    public static final String SAMTVIEW = "samtview";
    public static final String SHOWCHARTVIEW = "showchartview";
    public static final String SHOWPREFERENCES = "showpreferences";
    public static final String SIMPLEAUDITVIEW = "simpleauditview";
    public static final String TODO = "todo";
    public static final String TASKVIEW = "taskview";
    public static final String TASKDELETE = "taskdelete";
    public static final String TASKSHOWALL = "taskshowall";
    // value "bsidnd" kept due to historical reasons
    public static final String TREEDND = "bsidnd";
    public static final String UNIFY = "unify";
    public static final String XMLEXPORT = "xmlexport";
    public static final String XMLIMPORT = "xmlimport";
    public static final String GROUP_BY_TAG = "groupbytag";
    public static final String CREATE_INDIVIDUAL_TASKS = "createindividualtasks";
    public static final String CNAVALIDATION = "cnavalidation";
    
    public static String[] getAllRightIDs(){
        ArrayList<String> retVal = new ArrayList<String>(0);
        for(Field f : ActionRightIDs.class.getDeclaredFields()){
            try {
                if(f.get(null) instanceof String ) {
                    retVal.add((String)f.get(null));
                }
            } catch (IllegalArgumentException e) {
                LOG.error("Error while getting rightIDs", e);
            } catch (IllegalAccessException e) {
                LOG.error("Error while getting rightIDs", e);
            }
        }
        return retVal.toArray(new String[retVal.size()]);
    }
}
