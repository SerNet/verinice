/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
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
package sernet.verinice.bpm;

import java.util.Set;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.scheduling.quartz.QuartzJobBean;

import sernet.hui.common.VeriniceContext;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 * 
 */
public class ProcessJob extends QuartzJobBean implements StatefulJob {

    private static VeriniceContext.State state;

    private Set<IProcessCreater> processCreaterSet;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.scheduling.quartz.QuartzJobBean#executeInternal(org
     * .quartz.JobExecutionContext)
     */
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        VeriniceContext.setState(ProcessJob.state);
        for (IProcessCreater creater : processCreaterSet) {
            creater.create();
        }
    }

    public Set<IProcessCreater> getProcessCreaterSet() {
        return processCreaterSet;
    }

    public void setProcessCreaterSet(Set<IProcessCreater> processCreaterSet) {
        this.processCreaterSet = processCreaterSet;
    }

    public void setWorkObjects(VeriniceContext.State workObjects) {
        ProcessJob.state = workObjects;
    }

    public VeriniceContext.State getWorkObjects() {
        return ProcessJob.state;
    }

}
