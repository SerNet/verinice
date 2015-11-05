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

import java.util.List;

import org.eclipse.swt.widgets.Shell;

import sernet.gs.reveng.importData.ZielobjektTypeResult;

/**
 * Checks if all types and subtypes of the Zielobjekte in GSTOOL
 * can be found in the configuration. If an unknown type was found
 * the user is asked to cancel the import. If the user cancels the import
 * a {@link GstoolImportCanceledException} is thrown. {@link GstoolImportCanceledException}
 * is a {@link RuntimeException}.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ValidateTask extends AbstractGstoolImportTask {

    private IProgress monitor;
    private Shell shell;
    
    public ValidateTask(Shell shell) {
        this.shell = shell;
    }


    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.gsimport.AbstractGstoolImportTask#executeTask(int, sernet.gs.ui.rcp.gsimport.IProgress)
     */
    @Override
    protected void executeTask(int importType, IProgress monitor) throws Exception {
        this.monitor = monitor;
        validate();
    }


    private void validate() throws GstoolImportCanceledException {
        List<ZielobjektTypeResult> zielobjekte = getGstoolDao().findZielobjektTypAll();
        if(zielobjekte==null || zielobjekte.isEmpty()) {
            return;
        }
        monitor.beginTask("Prüfe Typen und Subtypen der Zielobjekte", zielobjekte.size());
        GstoolTypeValidator validator = new GstoolTypeValidator(shell);
        boolean valid = validator.validate(zielobjekte);
        if(!valid) {
            throw new GstoolImportCanceledException();
        }
    }

}
