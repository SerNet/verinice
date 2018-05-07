/*******************************************************************************
 * Copyright (c) 2018 Alexander Ben Nasrallah <an@sernet.de>
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
 *     Alexander Ben Nasrallah <an@sernet.de>
 ******************************************************************************/
package sernet.verinice.web.poseidon;

import java.io.File;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;

import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;

/**
 * This class privides methods to generate a primeface menu model to access
 * downloadable client archives stored on the server.
 */
@ManagedBean(name = "clientMenuProvider")
public class ClientMenuProvider {

    @ManagedProperty("#{resourcesChecker}")
    private ResourcesChecker resourceChecker;

    public DefaultMenuModel getMenu() {
        DefaultMenuModel model = new DefaultMenuModel();
        File[] files = resourceChecker.getClientFiles();
        for (File file : files) {
            DefaultMenuItem item = menuItemForClientFile(file);
            model.addElement(item);
        }
        return model;
    }

    private DefaultMenuItem menuItemForClientFile(File clientFile) {
        String clientName = clientFile.getName();
        DefaultMenuItem item = new DefaultMenuItem(nameForClient(clientName));
        item.setUrl(ResourcesChecker.BASE_CLIENT_DIR + "/" + clientName);
        item.setIcon(iconForClient(clientName));
        return item;
    }

    public String iconForClient(String client) {
        if (client.contains("linux")) {
            return "fa fa-fw fa-linux";
        }
        if (client.contains("mac")) {
            return "fa fa-fw fa-apple";
        }
        if (client.contains("windows")) {
            return "fa fa-fw fa-windows";
        }
        return "fa fa-fw fa-archive";
    }
    
    public String nameForClient(String client) {
        if (client.contains("linux") && client.contains("x86_64")) {
            return "verinice (Linux, 64 bit)";
        }
        if (client.contains("linux")) {
            return "verinice (Linux, 32 bit)";
        }
        if (client.contains("mac")) {
            return "verinice (macOS)";
        }
        if (client.contains("windows") && client.contains("x86_64")) {
            return "verinice (Windows, 64 bit)";
        }
        if (client.contains("windows")) {
            return "verinice (Windows, 32 bit)";
        }
        return "verinice.";
    }
    
    public void setResourceChecker(ResourcesChecker resourcesChecker) {
        this.resourceChecker = resourcesChecker;
    }
}
