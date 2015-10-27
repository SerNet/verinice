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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import sernet.gs.reveng.importData.ZielobjektTypeResult;

/**
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


    public boolean validate(List<ZielobjektTypeResult> zielobjekte) {
        unknownTypes = new HashSet<String>();
        for (ZielobjektTypeResult zielobjekt : zielobjekte) {
            validate(zielobjekt.type, zielobjekt.subtype);
        }
        if(!unknownTypes.isEmpty()) {
            showCancelDialog();
        }
        return result;
    }
    
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
        StringBuilder sb = new StringBuilder();
        sb.append("Unknown GSTOOL types found. Click OK to use the default type: '");
        sb.append(GstoolTypeMapper.DEFAULT_TYPE_ID);
        sb.append("' and continue import. Click Cancel to cancel the import.\n\n");
        for (String typeLabel : unknownTypes) {
            sb.append(typeLabel).append("\n");
        }
        final String message = sb.toString();
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                result = MessageDialog.openConfirm(getShell(), "Unknown GSTOOL types", message);
            }
        }); 
        return result;
    }

    private void addToUnknownTypes(String gstoolType, String gstoolSubtype) {
        StringBuilder sb = new StringBuilder();
        sb.append(gstoolSubtype).append(" (").append(gstoolType).append(")");
        if(unknownTypes==null) {
            unknownTypes = new HashSet<String>();
        }
        unknownTypes.add(sb.toString());        
    }

    public Shell getShell() {
        if(shell==null) {
            return Display.getCurrent().getActiveShell();
        }
        return shell;
    }
  
}
