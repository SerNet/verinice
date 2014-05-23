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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class DbUpdate97To98 implements IDBUpdate {

    private static final Logger LOG = Logger.getLogger(DbUpdate97To98.class);
    
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
        
        // select note orphans
        List<Note> orphanNoteList = template.query( 
                "select note.dbid,note.entity_id from note left join cnatreeelement on note.cnatreeelement_id = cnatreeelement.dbid where cnatreeelement.dbid IS NULL",
                new NoteMapper());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Number of note orphans: " + orphanNoteList.size());
        }
        
        for (Note note : orphanNoteList) {
            List<Integer> propertyIdList = template.queryForList( 
                    "SELECT PROPERTYLIST.DBID FROM PROPERTYLIST WHERE TYPEDLIST_ID = ?",
                    new Object[]{note.getEntitiyId()},
                    Integer.class);
            // delete row(s) from table PROPERTIES
            for (Integer id : propertyIdList) {
                template.update("DELETE FROM PROPERTIES WHERE PROPERTIES.PROPERTIES_ID = ?",
                        new Object[] {id});
            }
            
            // delete row(s) from table PROPERTYLIST
            template.update("DELETE FROM PROPERTYLIST WHERE TYPEDLIST_ID = ?",
                    new Object[] {note.getEntitiyId()});
            
            // delete row from table NOTE
            template.update("DELETE FROM NOTE WHERE NOTE.DBID = ?",
                    new Object[] {note.getId()});
            
            // delete row from table ENTITY
            template.update("DELETE FROM ENTITY WHERE ENTITY.DBID = ?",
                    new Object[] {note.getEntitiyId()});
 
            if (LOG.isDebugEnabled()) {
                LOG.debug("Note orphan deleted, dbid: " + note.getId());
            }
        }
        
        if (LOG.isInfoEnabled()) {
            LOG.info("Number of deleted note orphans: " + orphanNoteList.size());
        }
        
    }
    
    public class Note {
        private Integer id;
        private Integer entitiyId;
        public Note() {
            super();
        }
        public Integer getId() {
            return id;
        }
        public void setId(Integer id) {
            this.id = id;
        }
        public Integer getEntitiyId() {
            return entitiyId;
        }
        public void setEntitiyId(Integer entitiyId) {
            this.entitiyId = entitiyId;
        }
        
    }
    
    public final class NoteMapper implements RowMapper {

        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            Note actor = new Note();
            actor.setEntitiyId(rs.getInt("entity_id"));
            actor.setId(rs.getInt("dbid"));
            return actor;
        }
    }
}
