/*******************************************************************************
 * Copyright (c) 2015 Daniel Murygin.
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
package sernet.gs.ui.rcp.gsimport;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import sernet.gs.reveng.importData.ZielobjektTypeResult;

/**
 * Checks if a type or subtypes of a GSTOOL Zielobjekt 
 * can be found in the configuration. 
 * 
 * If an unknown type was found
 * the user is asked to cancel the import. If the user cancels the import
 * a {@link GstoolImportCanceledException} is thrown. {@link GstoolImportCanceledException}
 * is a {@link RuntimeException}.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class GstoolTypeValidator {

    private boolean result = true;
    
    private Set<String> unknownTypes;    
    private Shell shell;
    
    public GstoolTypeValidator() {
        super();
    }
    
    public GstoolTypeValidator(Shell shell) {
        super();
        this.shell = shell;
    }

    /**
     * Checks the types and subtypes of a list of GSTOOL Zielobjekte.
     * 
     * @param zielobjektTypeList A {@link List} with GSTOOL Zielobjekt types
     * @return 
     *   True if all types and subtypes can be found in the configuration
     *   False if an unknown type was found and the user canceled the import 
     */
    public boolean validate(List<ZielobjektTypeResult> zielobjektTypeList) {
        unknownTypes = new HashSet<String>();
        for (ZielobjektTypeResult zielobjektType : zielobjektTypeList) {
            validate(zielobjektType.type, zielobjektType.subtype);
        }
        if(!unknownTypes.isEmpty()) {
            showCancelDialog();
        }
        return result;
    }
    
    /**
     * Checks if gstoolType or gstoolSubtype can be found in the configuration.
     * 
     * @param gstoolType A type of a Zielobjekt
     * @param gstoolSubtype A subtype of a Zielobjekt
     * @return True if gstoolType or gstoolSubtype can be found fasle if not
     */
    public boolean validate(String gstoolType, String gstoolSubtype) {
        try {
            GstoolTypeMapper.getVeriniceType(gstoolType, gstoolSubtype);
        } catch (GstoolTypeNotFoundException e) {
            addToUnknownTypes(gstoolType, gstoolSubtype);
            return false;
        }
        return true;
    }
    
    private boolean showCancelDialog() {
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                UnknownTypeDialog dialog = new UnknownTypeDialog(getShell(), unknownTypes);
                result = dialog.open();
            }
        }); 
        return result;
    }

    private void addToUnknownTypes(String gstoolType, String gstoolSubtype) {
        if(unknownTypes==null) {
            unknownTypes = new HashSet<String>();
        }
        unknownTypes.add(gstoolSubtype);
    }

    public Shell getShell() {
        if(shell==null) {
            return Display.getCurrent().getActiveShell();
        }
        return shell;
    }
  
}
