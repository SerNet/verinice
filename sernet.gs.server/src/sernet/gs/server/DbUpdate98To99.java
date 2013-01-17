/*******************************************************************************
 * Copyright (c) 2011 Daniel Murygin.
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
package sernet.gs.server;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Removes illegal entries from table permission.
 * 
 * Rows in permission must be unique for columns cte_id and role. 
 * Because of Bug 380 there are some non unique rows.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class DbUpdate98To99 implements IDBUpdate {

    private static final Logger LOG = Logger.getLogger(DbUpdate98To99.class);
    
    private DataSource dataSource;
    
    /* (non-Javadoc)
     * @see sernet.gs.server.IDBUpdate#getDataSource()
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /* (non-Javadoc)
     * @see sernet.gs.server.IDBUpdate#setDataSource(javax.sql.DataSource)
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /* (non-Javadoc)
     * @see sernet.gs.server.IDBUpdate#update()
     */
    @SuppressWarnings("unchecked")
    @Override
    public void update() {
        JdbcTemplate template = new JdbcTemplate(getDataSource());
        
        List<Map<String, Object>> resultList = template.queryForList("SELECT cte_id,role FROM permission GROUP BY cte_id,role HAVING  COUNT(cte_id)>1 ORDER BY cte_id,role");
        
        if(resultList!=null && !resultList.isEmpty()) {
            LOG.warn("Illegel entries found in table permissons, number of element, role touples: " + resultList.size() + ". Illegel entries will be deleted...");
            
            for (Map<String, Object> objects : resultList) {
                List<Map<String, Object>> dbIdList = template.queryForList("SELECT dbid FROM permission WHERE cte_id=? AND role=? ORDER BY dbid", new Object[]{objects.get("cte_id"),objects.get("role")});
                for (int i = 0; i < (dbIdList.size()-1); i++) {
                    Map<String, Object> map = dbIdList.get(i);
                    template.update("DELETE FROM permission WHERE dbid=?", new Object[]{map.get("dbid")});
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Illegel entries deleted, element-id: " + objects.get("cte_id") + ", role: " + objects.get("role") );
                }
            }
            
        }
        try {
            template.update("ALTER TABLE permission ADD CONSTRAINT uc_permission UNIQUE (role,cte_id)");     
        } catch(Exception e) {
            LOG.warn("Error while creating unique contraint for table permission: " + e.getMessage());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Stacktrace: ", e);
            }
        }
    }

}
