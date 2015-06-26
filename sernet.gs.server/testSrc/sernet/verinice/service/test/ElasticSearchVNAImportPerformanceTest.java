/*******************************************************************************
 * Copyright (c) 2015 Benjamin Weißenfels.
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
package sernet.verinice.service.test;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.search.ISearchService;
import sernet.verinice.search.IElementSearchDao;
import sernet.verinice.service.commands.SyncParameter;
import sernet.verinice.service.commands.SyncParameterException;
import sernet.verinice.service.test.helper.vnaimport.BeforeEachVNAImportHelper;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 */
public class ElasticSearchVNAImportPerformanceTest extends BeforeEachVNAImportHelper {

    /**
     *
     */
    private static final String VNA_FILE = "risk_catalog.vna";

    final Logger LOG = Logger.getLogger(ElasticSearchVNAImportPerformanceTest.class);

    @Resource(name = "searchService")
    protected ISearchService searchService;

    @Resource(name = "searchElementDao")
    protected IElementSearchDao searchDao;

    @Before
    @Override
    public void setUp() throws Exception {

    }

    @After
    @Override
    public void tearDown() throws CommandException {
        searchDao.clear();
        super.tearDown();
    }

    @Test
    public void logVNAImportDuration() throws Exception {
//        int ITERATIONS = 20;
//        LongStream.Builder longStream = LongStream.builder();
//        for (int i = 0; i < ITERATIONS; i++) {
//            searchDao.clear();
//            LOG.debug("start import of " + VNA_FILE);
//            long startTime = System.currentTimeMillis();
//            super.setUp();
//            long endTime = System.currentTimeMillis();
//            LOG.debug("import of " + VNA_FILE + " takes " + TimeFormatter.getHumanRedableTime(endTime - startTime));
//            longStream.accept(endTime - startTime);
//        }
//
//        long quota = longStream.build().reduce((x, y) -> x + y).orElse(0) / ITERATIONS;
//        LOG.debug("quota time of " + ITERATIONS  + " imports: " + TimeFormatter.getHumanRedableTime((long) quota));
    }

    /*
     * @see
     * sernet.verinice.service.test.helper.vnaimport.AbstractVNAImportHelper
     * #getFilePath()
     */
    @Override
    protected String getFilePath() {
        return this.getClass().getResource(VNA_FILE).getPath();
    }

    /*
     * @see
     * sernet.verinice.service.test.helper.vnaimport.AbstractVNAImportHelper
     * #getSyncParameter()
     */
    @Override
    protected SyncParameter getSyncParameter() throws SyncParameterException {
        return new SyncParameter(true, true, true, false, SyncParameter.EXPORT_FORMAT_VERINICE_ARCHIV);
    }

}
