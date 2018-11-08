/*******************************************************************************
 * Copyright (c) 2018 Jochen Kemnade.
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
 ******************************************************************************/
package sernet.verinice.service.commands.migration;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;

import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Migration class to fix a typo in the type if for threat groups (VN-2171)
 */
public class MigrateDbTo1_05D extends DbMigration {

    private static final long serialVersionUID = -3477615226809737382L;
    private static final String TYPE_ID_OLD = "bp_treat_group";
    private static final String TYPE_ID_NEW = "bp_threat_group";

    private static final Logger logger = Logger.getLogger(MigrateDbTo1_04D.class);

    @Override
    public void execute() {
        IBaseDao<@NonNull CnATreeElement, Serializable> dao = getDaoFactory()
                .getDAO(CnATreeElement.class);

        int numberOfUpdatedElements = dao.updateByQuery(
                "update CnATreeElement e set e.objectType = ? where e.objectType = ?",
                new Object[] { TYPE_ID_NEW, TYPE_ID_OLD });
        logger.info(numberOfUpdatedElements + " elements updated");
        updateVersion();
    }

    @Override
    public double getVersion() {
        return 1.05D;
    }

}
