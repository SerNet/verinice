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

import java.io.File;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;

import sernet.gs.reveng.importData.GSVampire;
import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;

/**
 * Abstract base class for all GSTOOL import tasks.
 * 
 * This class replaces the the context class loader of the 
 * current thread with the Hibernate class loader before 
 * executing the task. After executing the original class 
 * loader is set to thread context again.
 * 
 * This class also provides a GSTOOL dao to all subclasses.
 * With the GSTOOL dao subclasses can load data from the GSTOOL
 * database during import. The GSTOOL dao was formally known as GSVampire
 * or vampire.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public abstract class AbstractGstoolImportTask {

    private static final Logger LOG = Logger.getLogger(AbstractGstoolImportTask.class);
    
    public static final int TYPE_SQLSERVER = 1;
    public static final int TYPE_MDB = 2;
    
    private GSVampire gstoolDao;

    private ClassLoader originalClassLoader;
    
    public void execute(int importType, IProgress monitor) { // throws GstoolImportCanceledException  {
        try {
            initThreadContextClassLoader();
            executeTask(importType, monitor);
            resetThreadContextClassLoader();
            CnAElementFactory.getInstance().reloadModelFromDatabase();
        } catch (GstoolImportCanceledException e) {
            throw e;
        } catch (RuntimeException e) {
            LOG.error("Error while importing data from GSTOOL", e);
            throw e;
        } catch (Exception e) {
            LOG.error("Error while importing data from GSTOOL", e);
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Overwrite this to implement a GSTOOL import task.
     * 
     * @param importType TYPE_SQLSERVER for a SQLServer source db, TYPE_MDB for a MDB source db
     * @param monitor A progress monitor 
     * @throws Exception
     */
    protected abstract void executeTask(int importType, IProgress monitor) throws Exception;
    
    /**
     *  On this thread Hibernate will access Antlr in order to create a lexer.
     *  Hibernate will provide the name of a Hibernate-based class to Antlr.
     *  Antlr will try to load that class. In an OSGi-environment this will
     *   miserably fail since the Antlr bundle's classloader has no access to the
     *   Hibernate bundle's classes. However Antlr will use the context 
     *   classloader if it finds one. For this reason we initialize the context
     *   classloader with a classloader from a Hibernate class. This classloader is able to
     *   resolve Hibernate classes and can be used successfully by Antlr to access.
     */
    protected void initThreadContextClassLoader() {
        originalClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader classLoader = Hibernate.class.getClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
    }    
    
    protected void resetThreadContextClassLoader() {
        Thread.currentThread().setContextClassLoader(originalClassLoader);
    }
    
    protected GSVampire getGstoolDao() {
        if(gstoolDao==null) {
            initGsVampire();
        }
        return gstoolDao;
    }
    
    protected void initGsVampire() {
        File conf = new File(CnAWorkspace.getInstance().getConfDir() + File.separator + "hibernate-vampire.cfg.xml");
        gstoolDao = new GSVampire(conf.getAbsolutePath());
    }

}
