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
package sernet.verinice.service.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.junit.After;
import org.junit.Test;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.search.IElementSearchDao;
import sernet.verinice.search.Indexer;
import sernet.verinice.search.JsonBuilder;
import sernet.verinice.service.commands.SyncParameter;
import sernet.verinice.service.commands.SyncParameterException;
import sernet.verinice.service.test.helper.vnaimport.BeforeEachVNAImportHelper;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ElasticsearchTest extends BeforeEachVNAImportHelper {

    private static final Logger LOG = Logger.getLogger(ElasticsearchTest.class);
    
    private static final String VNA_FILENAME = "ElasticsearchTest.vna";
    
    @Resource(name="searchIndexer")
    protected Indexer searchIndexer;
    
    @Resource(name="searchElementDao")
    protected IElementSearchDao searchDao;
    
    
    @Test
    public void testIndex()  {
        searchIndexer.index();
        List elementList = elementDao.findByQuery("select e.uuid from CnATreeElement e where e.sourceId = '1460b5'", new String[]{});              
        for (Object uuid : elementList) {
            testElement((String) uuid); 
        }
        
    }
    
    @Test
    public void testUpdate()  {
        searchIndexer.index();
        String name = "SerNet";
        SearchHits result = searchDao.find(name).getHits();
        assertTrue("Element found with string: " + name, result.getTotalHits()==0);
        result = searchDao.find("title", "Cryptography").getHits();
        assertTrue("No element found with 'Cryptography' in title", result.getTotalHits()>0);
        SearchHit hit = result.getHits()[0];
        CnATreeElement element = elementDao.findByUuid(hit.getId(), RetrieveInfo.getPropertyInstance().setPermissions(true));
        assertNotNull("No element found with uuid: " + hit.getId(), element);
        element.setTitel(name);
        String json = JsonBuilder.getJson(element);
        assertTrue("JSON does not contain " + name + ": " + json, json.contains(name));
        ActionResponse response = searchDao.update(hit.getId(), json);
        result = searchDao.find("title", name).getHits();
        assertTrue("No element found with string: " + name, result.getTotalHits()>0);
    }
    
    @Test
    public void testDelete()  {
        searchIndexer.index();
        SearchHits result = searchDao.find("title", "Cryptography").getHits();
        assertTrue("No element found with 'Cryptography' in title", result.getTotalHits()>0);
        delete(0);
        result = searchDao.find("title", "Cryptography").getHits();
        assertTrue("Element found with 'Cryptography' in title", result.getTotalHits()==0);
    }

    private void delete(int n) {
        SearchHits result = searchDao.find("title", "Cryptography").getHits();
        SearchHit[] hits = result.getHits();
        if(hits.length==0) {
            return;
        }
        LOG.debug( n + ". results: " + hits.length);
        for (SearchHit hit : hits) {
            searchDao.delete(hit.getId());
        }
        assertTrue("More than 10 calls of delete.", n<10);
        n++;
        delete(n);
    }
    
    @Test
    public void findAndGroupByType() {
        searchIndexer.index();
        SearchResponse response = searchDao.findAndGroupByType("Network");  
        
        Terms terms = response.getAggregations().get("byType");
        for (Terms.Bucket entry : terms.getBuckets()) {
            String key = entry.getKey();                    // bucket key
            long number = entry.getDocCount(); 
            if (LOG.isDebugEnabled()) {
                LOG.debug(key + ": " + number);
            }
        }
    }
    
    @Test
    public void testClear() {
        searchIndexer.index();
        SearchHits result = searchDao.find("Network").getHits();
        assertTrue("No element found with 'Network'", result.getTotalHits()>0);
        searchDao.clear();
        result = searchDao.find("Network").getHits();
        assertTrue("Element found with 'Network' after clearing index.", result.getTotalHits()==0);
        result = searchDao.findAll().getHits();
        assertTrue("Element found after clearing index.", result.getTotalHits()==0);
    }
    
    @After
    public void tearDown() throws CommandException {
        searchDao.clear();
        super.tearDown();
    }
    
    private Object[] getScopeIdArray() {
        return getScopeIds().toArray();
    }

    private void testElement(String uuid) {
        CnATreeElement element = elementDao.findByUuid(uuid, RetrieveInfo.getPropertyInstance());
        //testFindByUuid(element, 0);
        if(!(element instanceof Group)) {
            testFindByTitle(element);
        }
    }

    private void testFindByUuid(CnATreeElement element, int n) {
        SearchHits hits = searchDao.find(element.getUuid()).getHits();
        LOG.debug(n + " " + element.getUuid() + ", hits: " + hits.getTotalHits());
        boolean found = false;
        for (SearchHit hit : hits) {
            Map<String, Object> source = hit.getSource();
            LOG.debug("Element found, uuid: " + source.get("uuid"));
            if(element.getUuid().equals((String) source.get("uuid"))) {
                found = true;
                break;
            }
        }
        if(!found) {
            LOG.debug(element.getUuid() + " not found");       
            int i = n + 1;
            assertTrue("More than 10 tries", i<10);
            testFindByUuid(element, i);
        }
        assertTrue("Element not found, uuid: " + element.getUuid(), found);
    }
    
    private void testFindByTitle(CnATreeElement element) {
        SearchHits hits = searchDao.findByPhrase(element.getTitle()).getHits();
        boolean found = false;
        for (SearchHit hit : hits) {
            Map<String, Object> source = hit.getSource();
            if(element.getUuid().equals((String) source.get("uuid"))) {
                found = true;
                break;
            }
        }
        assertTrue("Element not found, title: " + element.getTitle() + " hits: " + hits.getTotalHits(), found);
    }

    @Override
    protected String getFilePath() {
        return this.getClass().getResource(VNA_FILENAME).getPath();
    }

    @Override
    protected SyncParameter getSyncParameter() throws SyncParameterException {
        return new SyncParameter(true, true, true, false, SyncParameter.EXPORT_FORMAT_VERINICE_ARCHIV);
    }
}
