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
package sernet.verinice.licensemanagement;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * This should run once a day, at 00:02 (not configurable)
 * to remove license assignments of users of licenses that are not valid 
 * anymore (to prevent users havin invalid licenses assigned)
 *  
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class RemoveInvalidLicensesJob extends QuartzJobBean implements StatefulJob {

    private LicenseRemover remover;
    
    /*
     * @see org.springframework.scheduling.quartz.
     * QuartzJobBean#executeInternal(org.quartz.JobExecutionContext)
     */
    @Override
    protected void executeInternal(JobExecutionContext arg0) throws JobExecutionException {
        remover.runNonBlocking();

    }

    /**
     * @return the remover
     */
    public LicenseRemover getRemover() {
        return remover;
    }

    /**
     * @param remover the remover to set
     */
    public void setRemover(LicenseRemover remover) {
        this.remover = remover;
    }

}
