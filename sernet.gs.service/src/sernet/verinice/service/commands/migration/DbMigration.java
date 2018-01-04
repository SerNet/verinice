/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.commands.migration;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.engine.SessionFactoryImplementor;
import org.springframework.orm.hibernate3.HibernateCallback;

import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.bsi.Attachment;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.service.commands.SaveElement;
import sernet.verinice.service.model.LoadModel;

@SuppressWarnings("serial")
public abstract class DbMigration extends GenericCommand {
    public abstract double getVersion();

    protected static final String HIBERNATE_DIALECT_POSTGRSQL = "org.hibernate.dialect.PostgreSQLDialect";
    protected static final String HIBERNATE_DIALECT_ORACLE = "sernet.verinice.hibernate.Oracle10gNclobDialect";
    protected static final String HIBERNATE_DIALECT_DERBY = "sernet.verinice.hibernate.ByteArrayDerbyDialect";

    protected void updateVersion() {
        Logger.getLogger(this.getClass()).debug("Setting DB version to " + getVersion());
        try {
            LoadModel<BSIModel> command2 = new LoadModel<>(BSIModel.class);
            command2 = getCommandService().executeCommand(command2);
            BSIModel model = command2.getModel();
            model.setDbVersion(getVersion());
            SaveElement<BSIModel> command4 = new SaveElement<BSIModel>(model);
            getCommandService().executeCommand(command4);
        } catch (CommandException e) {
            throw new RuntimeCommandException(e);
        }
    }

    protected boolean isOracle() {
        return HIBERNATE_DIALECT_ORACLE.equals(getHibernateDialect());
    }

    protected boolean isPostgres() {
        return HIBERNATE_DIALECT_POSTGRSQL.equals(getHibernateDialect());
    }

    protected boolean isDerby() {
        return HIBERNATE_DIALECT_DERBY.equals(getHibernateDialect());
    }

    /**
     * reads in hibernate session configured database dialog
     */
    protected String getHibernateDialect() {
        return (String) getDaoFactory().getDAO(Attachment.class).executeCallback(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                return ((SessionFactoryImplementor) session.getSessionFactory()).getDialect().toString();
            }
        });
    }
}
