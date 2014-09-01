/*******************************************************************************
 * Copyright (c) 2014 Benjamin Weißenfels.
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
 *     Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.common.accountgroup;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import sernet.hui.common.connect.ITypedElement;
import sernet.verinice.model.common.configuration.Configuration;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 * 
 */
@SuppressWarnings("serial")
public class AccountGroup implements ITypedElement, Serializable {

    public final static String TYPE_ID = "user_groups";

    private Integer dbId;

    private Set<Configuration> configurations;

    private String name;

    public Integer getDbId() {
        return dbId;
    }

    public void setDbId(Integer dbId) {
        this.dbId = dbId;
    }

    public Set<Configuration> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(Set<Configuration> configurations) {
        this.configurations = configurations;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getTypeId() {
        return TYPE_ID;
    }

    public void addConfiguration(Configuration configuration) {
        if (configurations == null) {
            configurations = new HashSet<Configuration>();
        }
        configurations.add(configuration);
    }
}
