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
package sernet.verinice.search;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.scheduling.quartz.JobDetailBean;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class AsyncIndexer {

    private static final Logger LOG = Logger.getLogger(AsyncIndexer.class);
    
    public static final String JOB_NAME_PREFIX = "INDEX_JOB_"; 
    public static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_PATTERN);
    /**
     * Factory to create {@link IndexJob} instances
     * configured in veriniceserver-search.xml
     */
    private ObjectFactory indexJobFactory;

    private Scheduler scheduler;
    
    public void init() {
        run();
    }
    
    public void run() {
        try {
            // creates a new (prototype) instances of the JobDetailBean spring bean
            // see veriniceserver-search.xml and http://static.springsource.org/spring/docs/2.5.x/reference/beans.html#beans-factory-aware-beanfactoryaware
            JobDetailBean job = (JobDetailBean) indexJobFactory.getObject();
            String group = createGroupName();
            String name = createName();        
            job.setGroup(group);
            job.setName(name);
            
            //Register this job to the scheduler           
            
            
            // execute in 60s from now
            /*     
            long startTime = System.currentTimeMillis() + 60000L;
            SimpleTrigger trigger = new SimpleTrigger(name + "-trigger",
                                                      null,
                                                      new Date(startTime),
                                                      null,
                                                      0,
                                                      0L);                                                     
            getScheduler().scheduleJob(job,trigger);
            */
            
            // execute immediately
            getScheduler().addJob(job, true);
            getScheduler().triggerJob(name,group);
        } catch (SchedulerException e) {
            LOG.error("Error while starting index job", e);
            throw new RuntimeException(e);
        }  
    }

    private String createGroupName() {
        return AsyncIndexer.class.getPackage().getName();
    }
    
    private String createName() {
        StringBuilder sb = new StringBuilder();
        sb.append(JOB_NAME_PREFIX);
        sb.append(DATE_FORMAT.format(new Date(System.currentTimeMillis())));
        return sb.toString();
    }

    public ObjectFactory getIndexJobFactory() {
        return indexJobFactory;
    }

    public void setIndexJobFactory(ObjectFactory indexJobFactory) {
        this.indexJobFactory = indexJobFactory;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }
}
