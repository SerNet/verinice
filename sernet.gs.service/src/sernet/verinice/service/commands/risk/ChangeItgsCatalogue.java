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
package sernet.verinice.service.commands.risk;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBSIConfig;
import sernet.verinice.service.parser.BSIMassnahmenModel;
import sernet.verinice.service.parser.GSScraperUtil;

/**
 * Changes the configuration of the IT baseline protection
 * catalogue configuration on the server side.
 * 
 *  This command is used in operation mode "Standalone"
 *  because you have two instances of BSIMassnahmenModel
 *  one on the clioent one in the embedded jetty container.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ChangeItgsCatalogue extends GenericCommand {

    private static final long serialVersionUID = -6490570039086565624L;

    private IBSIConfig itgsCatalogueConfiguration;

    public ChangeItgsCatalogue(IBSIConfig config) {
        super();
        this.itgsCatalogueConfiguration = config;
    }

    @Override
    public void execute() {
        BSIMassnahmenModel model = GSScraperUtil.getInstance().getModel();
        // Using a new configuration object makes it throw
        // already loaded data.
        if (itgsCatalogueConfiguration != null) {
            model.setBSIConfig(itgsCatalogueConfiguration);
        }
    }

}
