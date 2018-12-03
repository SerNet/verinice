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
 * This class provides methods to generate a primeface menu model to access
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
        ClientInformation ci = ClientInformation.fromFileName(clientName);
        String menuTitle = String.format("verinice (%s)", ci.getInformation());
        DefaultMenuItem item = new DefaultMenuItem(menuTitle);
        item.setUrl(ResourcesChecker.BASE_CLIENT_DIR + "/" + clientName);
        item.setIcon(iconForClient(ci));
        return item;
    }

    public String iconForClient(ClientInformation ci) {
        switch (ci.getOs()) {
            case Linux:
                return "fa fa-fw fa-linux";
            case macOS:
                return "fa fa-fw fa-apple";
            case Windows:
                return "fa fa-fw fa-windows";
            default:
                return "fa fa-fw fa-archive";
        }
    }

    public void setResourceChecker(ResourcesChecker resourcesChecker) {
        this.resourceChecker = resourcesChecker;
    }

    public static class ClientInformation {
        private OS os = OS.unknown;
        private boolean is64bits = false;

        public static ClientInformation fromFileName(String fileName) {
            ClientInformation ci = new ClientInformation();
            if (fileName.contains("linux")) {
                ci.os = ClientInformation.OS.Linux;
            }
            if (fileName.contains("mac")) {
                ci.os = ClientInformation.OS.macOS;
            }
            if (fileName.contains("windows")) {
                ci.os = ClientInformation.OS.Windows;
            }
            if (fileName.contains("x86_64")) {
                ci.is64bits = true;
            }
            return ci;
        }

        public String getInformation() {
            switch (os) {
                case Linux:
                    return is64bits ? "Linux, 64 bit" : "Linux, 32 bit";
                case Windows:
                    return is64bits ? "Windows, 64 bit" : "Windows, 32 bit";
                default:
                    return os.toString();
            }
        }

        @Override
        public String toString() {
            switch (os) {
                case Linux:
                    return is64bits ? "Linux, 64 bit" : "Linux, 32 bit";
                case Windows:
                    return is64bits ? "Windows, 64 bit" : "Windows, 32 bit";
                default:
                    return os.toString();
            }
        }

        public OS getOs() {
            return os;
        }

        public enum OS {
            Linux,
            Windows,
            macOS,
            unknown
        }
    }
}
