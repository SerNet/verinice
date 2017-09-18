/*******************************************************************************
 * Copyright (c) 2017 Sebastian Hagedorn.
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
 *     Sebastian Hagedorn sh[at]sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.verinice.bp.importer.actions;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.actions.RightsEnabledAction;
import sernet.verinice.bp.importer.preferences.PreferenceConstants;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.service.bp.exceptions.CreateBPElementException;
import sernet.verinice.service.bp.importer.BpImporter;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class ImportBPCompendiumActionDelegate extends RightsEnabledAction {
    
    public static final String ID = "sernet.verinice.bp.importer.importaction"; //$NON-NLS-1$

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.BPIMPORTER;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.rcp.RightsEnabledActionDelegate#doRun(org.eclipse.jface.action.IAction)
     */
    @Override
    public void doRun() {
        String xmlRoot = Activator.getDefault().getPreferenceStore().
                getString(PreferenceConstants.XML_ROOT_DIRECTORY);
        BpImporter importer = new BpImporter(xmlRoot);
        try {
            importer.run();
        } catch (CreateBPElementException e) {
            ExceptionUtil.log(e, "Something went wrong importing BSI BP-Compendium from " + xmlRoot);
        }

    }


}
